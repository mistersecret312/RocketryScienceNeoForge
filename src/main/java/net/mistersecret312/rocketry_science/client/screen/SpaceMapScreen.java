package net.mistersecret312.rocketry_science.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.mistersecret312.rocketry_science.client.screen.widgets.WidgetCelestialBody;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.datapack.SolarSystem;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import org.joml.Vector2d;

public class SpaceMapScreen extends Screen
{
	public SolarSystem solarSystem;
	public CelestialBody star;

	public CelestialBody selectedBody = null;

	private final ClientLevel level;

	private double zoom = 1;
	public double panX = 0;
	public double panY = 0;

	public SpaceMapScreen(SolarSystem solarSystem, ClientLevel level)
	{
		super(Component.empty());
		this.solarSystem = solarSystem;
		this.level = level;

		this.star = OrbitUtil.getCelestialBody(solarSystem.getStar(), level);
	}

	@Override
	protected void init()
	{
		super.init();
		this.clearWidgets();

		addRenderableWidget(new WidgetCelestialBody(32, 32, this, star));
		for(CelestialBody child : OrbitUtil.getAllChildren(star, getLevel()))
			addRenderableWidget(new WidgetCelestialBody(16, 16, this, child));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		PoseStack poseStack = graphics.pose();
		this.renderBackground(graphics, mouseX, mouseY, partialTick);

		int trueX = (int) ((mouseX - width / 2f - panX) / zoom);
		int trueY = (int) ((mouseY - height / 2f - panY) / zoom);

		poseStack.translate(width / 2f, height / 2f, 0);
		poseStack.translate(panX, panY, 0);

		poseStack.scale((float) (zoom), (float) (zoom), 1);

		for(Renderable renderable : this.renderables)
			renderable.render(graphics, trueX, trueY, partialTick);

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

	public CelestialBody getSelectedBody()
	{
		return selectedBody;
	}

	public SolarSystem getSolarSystem()
	{
		return solarSystem;
	}

	public ClientLevel getLevel()
	{
		return level;
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
