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

public class WidgetAssembleRocket extends AbstractWidget implements Renderable
{
	public static final ResourceLocation ASSEMBLE_BUTTON = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/gui/rocket_assembler/assemble_button.png");
	public boolean isClicked = false;

	public WidgetAssembleRocket(int x, int y, int width, int height)
	{
		super(x, y, width, height, Component.empty());
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		PoseStack poseStack = graphics.pose();
		float x = width/2f + this.getX();
		float y = height/2f + this.getY();

		int bound = 8;

		graphics.blit(ASSEMBLE_BUTTON, (int) (x-bound), (int) (y-bound), isClicked ? 16 : 0, 0, 16, 16, 32, 16);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		int bounds = 8;
		float x = width/2f + this.getX();
		float y = height/2f + this.getY();

		if(mouseX >= x-bounds && mouseX <= x+bounds)
			if(mouseY >= y-bounds && mouseX <= y+bounds)
				this.isClicked = true;

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
