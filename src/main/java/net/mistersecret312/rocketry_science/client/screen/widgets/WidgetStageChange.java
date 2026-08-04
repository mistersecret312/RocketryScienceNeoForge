package net.mistersecret312.rocketry_science.client.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.mistersecret312.rocketry_science.client.screen.RocketAssemblyScreen;
import net.mistersecret312.rocketry_science.network.packets.ServerBoundRequestRocketEntityPacket;
import net.neoforged.neoforge.network.PacketDistributor;

public class WidgetStageChange extends AbstractWidget
{
	public RocketAssemblyScreen screen;
	public int change;

	public WidgetStageChange(int x, int y, int width, int height, RocketAssemblyScreen screen, int change)
	{
		super(x, y, width, height, Component.empty());
		this.screen = screen;
		this.change = change;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		int xBounds = width/2;
		int yBounds = height/2;
		float x = width/2f + this.getX();
		float y = height/2f + this.getY();

		if(mouseX >= x-xBounds && mouseX <= x+xBounds)
			if(mouseY >= y-yBounds && mouseY <= y+yBounds)
			{
				PacketDistributor.sendToServer(new ServerBoundRequestRocketEntityPacket());
				screen.stage += change;
				if(screen.stage < -1)
					screen.stage = -1;
				if(screen.stage > screen.rocketEntity.getRocket().stages.size()-1)
					screen.stage = screen.rocketEntity.getRocket().stages.size()-1;
			}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		PoseStack poseStack = graphics.pose();
		float x = width/2f + this.getX();
		float y = height/2f + this.getY();

		int xBounds = width/2;
		int yBounds = height/2;

		graphics.blit(RocketAssemblyScreen.TEXTURE, (int) (x-xBounds), (int) (y-yBounds), change == 1 ? 144 : 150, 230, 5, 7, 256, 256);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
	{

	}
}
