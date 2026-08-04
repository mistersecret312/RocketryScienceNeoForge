package net.mistersecret312.rocketry_science.client.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.client.screen.RocketAssemblyScreen;
import net.mistersecret312.rocketry_science.network.packets.ServerBoundRequestRocketEntityPacket;
import net.mistersecret312.rocketry_science.network.packets.ServerBoundStartRocketAssemblyPacket;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class WidgetAssembleRocket extends AbstractWidget implements Renderable
{
	public static final ResourceLocation ASSEMBLE_BUTTON = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/gui/rocket_assembler/assemble_button.png");
	public boolean isClicked = false;

	public RocketAssemblyScreen screen;

	public WidgetAssembleRocket(int x, int y, int width, int height, RocketAssemblyScreen screen)
	{
		super(x, y, width, height, Component.empty());
		this.screen = screen;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		PoseStack poseStack = graphics.pose();
		float x = width/2f + this.getX();
		float y = height/2f + this.getY();

		int bound = 8;
		boolean isActive = screen.blockEntity.started;

		graphics.blit(RocketAssemblyScreen.TEXTURE, (int) (x-bound), (int) (y-bound), isActive ? 240 : 223, 240, 16, 16, 256, 256);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		int bounds = 8;
		float x = width/2f + this.getX();
		float y = height/2f + this.getY();

		if(mouseX >= x-bounds && mouseX <= x+bounds)
			if(mouseY >= y-bounds && mouseY <= y+bounds)
			{
				this.isClicked = true;
				PacketDistributor.sendToServer(new ServerBoundRequestRocketEntityPacket());
				if(screen.constructionMessage.isEmpty())
					PacketDistributor.sendToServer(new ServerBoundStartRocketAssemblyPacket());
			}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		this.isClicked = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
	{
		defaultButtonNarrationText(narrationElementOutput);
	}
}
