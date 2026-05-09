package net.mistersecret312.rocketry_science.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.blocks.CombustionChamberBlock;
import net.mistersecret312.rocketry_science.client.model.PlumeModel;

public class PlumeRenderer
{
	private PlumeModel model;
	public  PlumeRenderer(PlumeModel model)
	{
		this.model = model;
	}

	public void renderPlume(int frame, int throttle, BlockState state, PoseStack poseStack, MultiBufferSource buffer,
							int overlay)
	{
		int length = Math.max(0, Math.min(7, throttle-2));
		int offset = 0;

		ResourceLocation textureStart = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/misc/plume/hydrolox/atmosphere/start/"+frame+".png");
		ResourceLocation textureMiddle = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/misc/plume/hydrolox/atmosphere/middle/"+frame+".png");
		ResourceLocation textureEnd = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/misc/plume/hydrolox/atmosphere/end/"+frame+".png");

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.mulPose(state.getValue(CombustionChamberBlock.FACING).getOpposite().getRotation());
		poseStack.translate(0f, 1f, 0f);

		poseStack.scale(1.375f, 1, 1.375f);

		model.renderToBuffer(poseStack, buffer.getBuffer(RocketRenderTypes.plume(textureStart)),
				255, 255, 255);
		poseStack.popPose();

		for (int segment = 0; segment < length; segment++)
		{
			offset++;
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.5f, 0.5f);
			poseStack.mulPose(state.getValue(CombustionChamberBlock.FACING).getOpposite().getRotation());
			poseStack.translate(0f, 2+segment, 0f);

			poseStack.scale(1.375f, 1, 1.375f);
			this.model.renderToBuffer(poseStack, buffer.getBuffer(RocketRenderTypes.plume(textureMiddle)),
					255, 255, 255);
			poseStack.popPose();
		}

		poseStack.pushPose();
		poseStack.translate(0.5f, 0.5f, 0.5f);
		poseStack.mulPose(state.getValue(CombustionChamberBlock.FACING).getOpposite().getRotation());
		poseStack.translate(0f, 2+offset, 0f);

		poseStack.scale(1.375f, 1, 1.375f);
		this.model.renderToBuffer(poseStack, buffer.getBuffer(RocketRenderTypes.plume(textureEnd)),
				255, 255, 255);
		poseStack.popPose();
	}

}
