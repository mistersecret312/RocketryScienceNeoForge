package net.mistersecret312.rocketry_science.client.screen.widgets;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.mistersecret312.rocketry_science.client.screen.SpaceMapScreen;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import org.joml.Vector2d;

import java.util.function.Consumer;

public class WidgetCelestialBody extends AbstractWidget implements Renderable
{
	private final CelestialBody body;
	private final ClientLevel level;
	private final SpaceMapScreen screen;

	public WidgetCelestialBody(int width, int height, SpaceMapScreen screen, CelestialBody body)
	{
		super(0, 0, width, height, Component.empty());
		this.body = body;
		this.level = screen.getLevel();
		this.screen = screen;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		PoseStack poseStack = graphics.pose();
		Vector2d position = new Vector2d();
		if(body.getOrbit() != null)
			position = OrbitUtil.getHighestOrderPosition(level.getGameTime(), body.getOrbit(), level.registryAccess());

		poseStack.translate(position.x, position.y, 0);
		graphics.blit(body.getIcon(), -16, -16, 0, 0, 32, 32, 32, 32);

		double bounds = 8;
		if(mouseX >= position.x-bounds && mouseY >= position.y-bounds)
			if(mouseX <= position.x+bounds && mouseY <= position.y+bounds)
				graphics.drawString(Minecraft.getInstance().font, Component.literal(body.getName()), -Minecraft.getInstance().font.width(body.getName())/2, -16, -1, true);
		poseStack.translate(-position.x, -position.y, 0);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		Vector2d position = new Vector2d();
		if(body.getOrbit() != null)
			position = OrbitUtil.getHighestOrderPosition(level.getGameTime(), body.getOrbit(), level.registryAccess());

		double bounds = 8;
		if(mouseX >= position.x-bounds && mouseY >= position.y-bounds)
			if(mouseX <= position.x+bounds && mouseY <= position.y+bounds)
			{
				this.playDownSound(Minecraft.getInstance().getSoundManager());
				if(screen.selectedBody != null && screen.selectedBody.equals(body))
				{
					screen.panX = -position.x*screen.getZoom();
					screen.panY = -position.y*screen.getZoom();
				}
				else screen.selectedBody = body;
			}
		return false;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
	{
		defaultButtonNarrationText(narrationElementOutput);
	}
}
