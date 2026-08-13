package net.mistersecret312.rocketry_science.client.screen.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.mistersecret312.rocketry_science.client.screen.LaunchControllerScreen;
import net.mistersecret312.rocketry_science.network.packets.ServerBoundRocketTakeoffPacket;
import net.neoforged.neoforge.network.PacketDistributor;

public class WidgetLaunchRocket extends AbstractWidget implements Renderable
{
	public LaunchControllerScreen screen;

	public WidgetLaunchRocket(int x, int y, int width, int height, LaunchControllerScreen screen)
	{
		super(x, y, width, height, Component.empty());
		this.screen = screen;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		graphics.blit(LaunchControllerScreen.TEXTURE, getX(), getY(), 1, 240, 62, 15);
		graphics.drawString(Minecraft.getInstance().font, "Takeoff", getX()+6, getY()+4, -1, false);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		int xBounds = 31;
		int yBounds = 8;
		float x = width / 2f + this.getX();
		float y = height / 2f + this.getY();

		if(mouseX >= x - xBounds && mouseX <= x + xBounds)
			if(mouseY >= y - yBounds && mouseY <= y + yBounds)
			{
				PacketDistributor.sendToServer(new ServerBoundRocketTakeoffPacket());
			}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
	{
		defaultButtonNarrationText(narrationElementOutput);
	}
}
