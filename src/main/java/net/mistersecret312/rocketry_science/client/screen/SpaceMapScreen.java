package net.mistersecret312.rocketry_science.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.mistersecret312.rocketry_science.client.screen.widgets.WidgetCelestialBody;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.datapack.SolarSystem;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SpaceMapScreen extends Screen
{
	public SolarSystem solarSystem;
	public CelestialBody star;

	public CelestialBody selectedBody;
	public CelestialBody focusBody;

	private final ClientLevel level;
	public final List<Renderable> staticRenderables = Lists.newArrayList();

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

		poseStack.translate(width / 2f, height / 2f, 0);
		poseStack.translate(panX, panY, 0);

		poseStack.scale((float) (zoom), (float) (zoom), 1);

		for(Renderable renderable : this.renderables)
			renderable.render(graphics, trueX, trueY, partialTick);
	}

	public void renderPlanetData(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		for(Renderable renderable : this.staticRenderables)
			renderable.render(graphics, mouseX, mouseY, partialTick);
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

	protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget, boolean moves)
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
