package net.mistersecret312.rocketry_science.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.client.screen.widgets.WidgetAssembleRocket;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.menu.RocketAssemblyMenu;
import net.mistersecret312.rocketry_science.network.packets.ServerBoundRequestRocketEntityPacket;
import net.neoforged.neoforge.network.PacketDistributor;

public class RocketAssemblyScreen extends AbstractContainerScreen<RocketAssemblyMenu>
{
	public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
			"textures/gui/rocket_assembler_gui_prototype.png");

	public RocketAssemblerBlockEntity blockEntity;
	public RocketPad pad;
	public RocketEntity rocketEntity;

	public String constructionMessage;

	public RocketAssemblyScreen(RocketAssemblyMenu menu, Inventory playerInventory, Component title)
	{
		super(menu, playerInventory, title);
		this.blockEntity = menu.blockEntity;
		this.pad = menu.pad;
		this.rocketEntity = new RocketEntity(blockEntity.getLevel());
	}

	@Override
	protected void init()
	{
		super.init();
		this.clearWidgets();

		PacketDistributor.sendToServer(new ServerBoundRequestRocketEntityPacket());

		this.addRenderableWidget(new WidgetAssembleRocket(100, 180,16,16, this));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
	{
		PoseStack poseStack = graphics.pose();
		int x = width/2;
		int y = height/2;

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int imgX = (width - 192) / 2;
		int imgY = (height - 192) / 2;

		graphics.blit(TEXTURE, imgX, imgY, 0, 0, 192, 192);

		graphics.fillRenderType(RenderType.endGateway(), imgX+18, imgY+18,imgX+94, imgY+158,0);

		if (rocketEntity != null) {
			float eWidth = (float) Math.max(0.1f, rocketEntity.makeBoundingBox().getXsize());
			float eHeight = (float) Math.max(0.1f, rocketEntity.makeBoundingBox().getYsize());

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

			RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(rocketEntity, 0.0, 0.0, 0.0, 0.0F, 1.0F, poseStack, graphics.bufferSource(), 15728880));

			graphics.flush();
			entityrenderdispatcher.setRenderShadow(true);
			poseStack.popPose();
			Lighting.setupFor3DItems();
		}
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{

	}
}
