package net.mistersecret312.rocketry_science.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.client.screen.widgets.WidgetAssembleRocket;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPadData;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.menu.RocketAssemblyMenu;

public class RocketAssemblyScreen extends AbstractContainerScreen<RocketAssemblyMenu>
{
	public RocketAssemblerBlockEntity blockEntity;
	public RocketPad pad;
	public RocketEntity rocketEntity;
	public RocketAssemblyScreen(RocketAssemblyMenu menu, Inventory playerInventory, Component title)
	{
		super(menu, playerInventory, title);
		this.blockEntity = menu.blockEntity;
		this.pad = menu.pad;
	}

	@Override
	protected void init()
	{
		super.init();
		this.clearWidgets();

		this.addRenderableWidget(new WidgetAssembleRocket(100, 180,16,16));

		if(pad == null)
			return;

		BlockPos padPos = pad.getPos();
		RocketPadBlockEntity pad = (RocketPadBlockEntity) menu.level.getBlockEntity(padPos);
		if(pad != null)
			rocketEntity = blockEntity.assembleRocket(pad);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
	{
		PoseStack poseStack = graphics.pose();
		int x = width/2;
		int y = height/2;
		graphics.fillRenderType(RenderType.endGateway(), x, y-160,x+90,y,0);

		if (rocketEntity != null) {
			float eWidth = (float) Math.max(0.1f, rocketEntity.getBoundingBox().getXsize());
			float eHeight = (float) Math.max(0.1f, rocketEntity.getBoundingBox().getYsize());

			float availableWidth = 80f;
			float availableHeight = 150f;

			float scale = Math.min(availableWidth / eWidth, availableHeight / eHeight);

			poseStack.pushPose();

			poseStack.translate(x + (90 / 2f), y - 5f, 50.0);

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
