package net.mistersecret312.rocketry_science.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.LaunchControllerBlockEntity;
import net.mistersecret312.rocketry_science.client.screen.widgets.WidgetLaunchRocket;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.menu.LaunchControllerMenu;
import net.mistersecret312.rocketry_science.network.packets.ServerBoundRequestRocketEntityPacket;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.neoforged.neoforge.network.PacketDistributor;

import java.math.RoundingMode;
import java.text.NumberFormat;

import static net.mistersecret312.rocketry_science.client.screen.RocketAssemblyScreen.renderRocket;

public class LaunchControllerScreen extends AbstractContainerScreen<LaunchControllerMenu>
{
	public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
			"textures/gui/launch_controller_prototype.png");

	public LaunchControllerBlockEntity blockEntity;
	public Level level;
	public RocketPad pad;

	public int id;

	public LaunchControllerScreen(LaunchControllerMenu menu, Inventory playerInventory, Component title)
	{
		super(menu, playerInventory, title);
		this.blockEntity = menu.blockEntity;
		this.pad = menu.pad;
		this.level = menu.level;
	}

	@Override
	protected void init()
	{
		super.init();
		this.clearWidgets();

		PacketDistributor.sendToServer(new ServerBoundRequestRocketEntityPacket());
		int imgX = (width - 192) / 2;
		int imgY = (height - 192) / 2;

		this.addRenderableWidget(new WidgetLaunchRocket(imgX+124, imgY+164, 64, 17, this));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
	{
		RocketEntity renderRocket = (RocketEntity) level.getEntity(id);
		PoseStack poseStack = graphics.pose();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int imgX = (width - 192) / 2;
		int imgY = (height - 192) / 2;

		poseStack.pushPose();
		poseStack.translate(0, 0, 0);
		graphics.blit(TEXTURE, imgX, imgY, 0, 0, 192, 192);
		graphics.fillRenderType(RenderType.endGateway(), imgX + 18, imgY + 18, imgX + 94, imgY + 158, 0);
		if(renderRocket != null)
		{
			renderRocket.getRocket().isInUI = true;
			renderRocket(renderRocket, graphics, poseStack, imgX, imgY);
			renderRocket.getRocket().isInUI = false;
		}
		poseStack.popPose();

		if(renderRocket != null)
		{
			Rocket rocket = renderRocket.getRocket();
			if(rocket.getStages().isEmpty())
				return;

			Font font = Minecraft.getInstance().font;
			graphics.drawCenteredString(font, "Entire Rocket", imgX + 145, imgY + 21, -1);

			double deltaV = 0;
			int stageIndex = 0;
			rocket.isInUI = true;
			for(Stage stageI : rocket.getStages())
			{
				deltaV += stageI.calculateDeltaV();
				stageIndex++;
			}
			rocket.isInUI = false;

			double twr = rocket.getMaxTWR();

			NumberFormat format = NumberFormat.getNumberInstance();
			format.setMaximumFractionDigits(1);
			format.setRoundingMode(RoundingMode.HALF_UP);

			poseStack.pushPose();
			poseStack.translate(imgX + 102, imgY + 35, 0);
			poseStack.scale(0.8f, 0.8f, 0.8f);
			graphics.drawString(font, "DeltaV: " + format.format(deltaV) + " m/s", 0, 0, -1);
			poseStack.translate(0, 9, 0);
			graphics.drawString(font, "TWR: " + format.format(twr), 0, 0, -1);

			poseStack.translate(0, 18, 0);
			graphics.drawString(font, "Mass: " + rocket.getMassKilogram() + " kg", 0, 0, -1);
			poseStack.translate(0, 9, 0);
			graphics.drawString(font, "Thrust: " + format.format(rocket.getMaxThrustKiloNewtons()) + " kN", 0, 0, -1);



			poseStack.popPose();
		}
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{

	}
}
