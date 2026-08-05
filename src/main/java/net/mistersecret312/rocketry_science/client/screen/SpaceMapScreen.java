package net.mistersecret312.rocketry_science.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.mistersecret312.rocketry_science.client.screen.widgets.WidgetCelestialBody;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.datapack.SolarSystem;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.environment.*;
import net.mistersecret312.rocketry_science.util.EnvironmentUtil;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.Stage;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.UUID;

public class SpaceMapScreen extends Screen
{
	public SolarSystem solarSystem;
	public CelestialBody star;

	public CelestialBody selectedBody;
	public CelestialBody focusBody;

	public UUID selectedCraft;

	private final ClientLevel level;
	public final List<Renderable> staticRenderables = Lists.newArrayList();
	public RocketEntity spaceCraftRocket = null;

	private double zoom = 1;
	public double panX = 0;
	public double panY = 0;

	private boolean shouldRebuild = false;

	public SpaceMapScreen(SolarSystem solarSystem, ClientLevel level)
	{
		super(Component.empty());
		this.solarSystem = solarSystem;
		this.level = level;

		this.star = OrbitUtil.getCelestialBody(solarSystem.getStar(), level);
		this.selectedBody = star;
	}

	@Override
	protected void init()
	{
		this.shouldRebuild = false;
		super.init();
		this.clearWidgets();

		addRenderableWidget(new WidgetCelestialBody(32, 32, this, selectedBody), true);
		for(CelestialBody child : OrbitUtil.getChildren(selectedBody, getLevel()))
			addRenderableWidget(new WidgetCelestialBody(16, 16, this, child), true);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		if(shouldRebuild)
			init();

		this.renderBackground(graphics, mouseX, mouseY, partialTick);
		renderCelestials(graphics, mouseX, mouseY, partialTick);
		renderPlanetData(graphics, mouseX, mouseY, partialTick);
	}

	public void renderCelestials(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		PoseStack poseStack = graphics.pose();

		int trueX = (int) ((mouseX - width / 2f - panX) / zoom);
		int trueY = (int) ((mouseY - height / 2f - panY) / zoom);

		poseStack.pushPose();

		poseStack.translate(width / 2f, height / 2f, 0);
		poseStack.translate(panX, panY, 0);

		poseStack.scale((float) (zoom), (float) (zoom), 1);

		for(Renderable renderable : this.renderables)
			renderable.render(graphics, trueX, trueY, partialTick);

		poseStack.popPose();
	}

	public void renderPlanetData(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		Font font = Minecraft.getInstance().font;

		for(Renderable renderable : this.staticRenderables)
			renderable.render(graphics, mouseX, mouseY, partialTick);

		int startX = (int) (width / 1.5f);
		int currentY = 14;

		if(focusBody != null)
		{
			graphics.drawString(font,"Selected : " + focusBody.getName(), startX, currentY, -1);
			currentY += 18;

			EnvironmentData environment = EnvironmentUtil.getEnvironment(focusBody);
			double pressure = environment.getPressureSeaLevel();
			double minTemp = environment.getTemperatureGradient().getMinTemp();
			double maxTemp = environment.getTemperatureGradient().getMaxTemp();
			double averageTemp = environment.getTemperatureGradient().getAverageTemperature();
			double radiation = environment.getRadiation();
			double gravity = environment.getGravity();

			graphics.drawString(font, "Pressure: " + PressureRating.getRating(pressure) + " (" + pressure + " atm)", startX, currentY, -1);
			currentY += 9;

			graphics.drawString(font, "Temperature: " + TemperatureRating.getRating(averageTemp) + " (" + minTemp + " K, " + maxTemp + " K)", startX, currentY, -1);
			currentY += 9;

			graphics.drawString(font, "Radiation: " + RadiationRating.getRating(radiation) + " (" + radiation + " mSv/h)", startX, currentY, -1);
			currentY += 9;

			graphics.drawString(font, "Gravity: " + GravityRating.getRating(gravity) + " (" + gravity + " m/s²)", startX, currentY, -1);
			currentY += 9;
		}

		if(selectedCraft != null)
		{
			NumberFormat format = NumberFormat.getNumberInstance();
			format.setMaximumFractionDigits(1);
			format.setRoundingMode(RoundingMode.HALF_UP);

			// Add a little padding if the celestial body data was also drawn
			if (focusBody != null) currentY += 9;

			SpaceCraft craft = OrbitUtil.SPACECRAFT.get(selectedCraft);
			graphics.drawString(font, "Spacecraft name : " + craft.getName(), startX, currentY, -1);
			currentY += 9;

			double deltaV = 0;
			for(Stage stage : craft.getStages())
				deltaV += stage.calculateDeltaV();
			double currentDeltaV = craft.getCurrentStage().calculateDeltaV();

			graphics.drawString(font, "Current Stage DeltaV: " + format.format(currentDeltaV) + " m/s", startX, currentY, -1);
			currentY += 9;

			graphics.drawString(font, "Total DeltaV: " + format.format(deltaV) + " m/s", startX, currentY, -1);
			currentY += 9;

			renderRocket(graphics, graphics.pose(), startX, currentY);
		}
	}

	public void renderRocket(GuiGraphics graphics, PoseStack poseStack, int imgX, int imgY)
	{
		if(spaceCraftRocket != null)
		{
			graphics.fillRenderType(RenderType.endGateway(), imgX+18, imgY+18,imgX+94, imgY+158,0);
			graphics.enableScissor(imgX+19, imgY+19, imgX+94, imgY+158);
			if(spaceCraftRocket.getRocket().getStages().isEmpty())
			{
				graphics.disableScissor();
				return;
			}
			float eWidth = (float) Math.max(0.1f, spaceCraftRocket.makeBoundingBox().getXsize());
			float eHeight = (float) Math.max(0.1f, spaceCraftRocket.makeBoundingBox().getYsize());

			float availableWidth = 76;
			float availableHeight = 130;

			float scale = Math.min(availableWidth / eWidth, availableHeight / eHeight);

			poseStack.pushPose();
			poseStack.translate(imgX + (109 / 2f), imgY + 155f, 50.0);

			poseStack.scale(scale, -scale, scale);

			float rotation = Minecraft.getInstance().levelRenderer.getTicks();
			poseStack.mulPose(Axis.YP.rotationDegrees(rotation % 360));

			Lighting.setupForEntityInInventory();
			EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
			entityrenderdispatcher.setRenderShadow(false);

			RenderSystem.runAsFancy(
					() -> entityrenderdispatcher.render(spaceCraftRocket, 0.0, 0.0, 0.0, 0.0F, 1.0F, poseStack,
							graphics.bufferSource(), 15728880));

			graphics.flush();
			entityrenderdispatcher.setRenderShadow(true);
			poseStack.popPose();
			Lighting.setupFor3DItems();
			graphics.disableScissor();
		}
	}


	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
	{
		double zoomFactor = 1.1;
		double oldZoom = this.zoom;

		if (scrollY > 0)
			this.zoom *= zoomFactor;
		else if (scrollY < 0)
			this.zoom /= zoomFactor;

		double min = 0.1D;
		double max = 5D;
		this.zoom = Mth.clamp(this.zoom, min, max);

		double f = (this.zoom / oldZoom) - 1.0;
		this.panX -= (0 - panX) * f;
		this.panY -= (0 - panY) * f;

		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
	{
		if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY))
			return true;

		if (button == 0 || button == 2)
		{
			this.panX += dragX;
			this.panY += dragY;

			this.panX = Mth.clamp(this.panX, -2000 * zoom, 2000 * zoom);
			this.panY = Mth.clamp(this.panY, -2000 * zoom, 2000 * zoom);
			return true;
		}

		return false;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		this.init();
		double trueX = (mouseX - width / 2f - panX) / zoom;
		double trueY = (mouseY - height / 2f - panY) / zoom;
		return super.mouseClicked(trueX, trueY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		double trueX = (mouseX - width / 2f - panX) / zoom;
		double trueY = (mouseY - height / 2f - panY) / zoom;
		return super.mouseReleased(trueX, trueY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(keyCode == GLFW.GLFW_KEY_ESCAPE && !selectedBody.equals(star))
		{
			selectedBody = selectedBody.getParent().isPresent() ? OrbitUtil.getCelestialBody(selectedBody.getParentKey(), level) : star;
			init();
			Vector2d position = new Vector2d();
			if(selectedBody.getOrbit() != null)
				position = OrbitUtil.getOrderPosition(level.getGameTime(), selectedBody.getOrbit(), level.registryAccess(), selectedBody);

			panX = -position.x*getZoom();
			panY = -position.y*getZoom();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public CelestialBody getSelectedBody()
	{
		return selectedBody;
	}

	public CelestialBody getFocusBody()
	{
		return focusBody;
	}

	public SolarSystem getSolarSystem()
	{
		return solarSystem;
	}

	public ClientLevel getLevel()
	{
		return level;
	}

	public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget, boolean moves)
	{
		if(moves)
			this.renderables.add(widget);
		else this.staticRenderables.add(widget);
		return (T)this.addWidget(widget);
	}

	public void markForRebuild()
	{
		this.shouldRebuild = true;
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	public double getZoom()
	{
		return zoom;
	}
}
