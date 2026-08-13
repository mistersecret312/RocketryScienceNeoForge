package net.mistersecret312.rocketry_science.client.vessel.block_data;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.client.renderer.PlumeRenderer;
import net.mistersecret312.rocketry_science.client.vessel.AbstractBlockDataRenderer;
import net.mistersecret312.rocketry_science.vessel.block_data.RocketEngineData;
import net.neoforged.neoforge.client.model.data.ModelData;

public class RocketEngineDataRenderer extends AbstractBlockDataRenderer<RocketEngineData>
{
	@Override
	public void render(RocketEngineData data, Level level, BlockPos.MutableBlockPos mutablePos, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
					   int packedLight)
	{
		boolean isInUI = data.stage.getVessel().isInUI();
		if(isInUI)
			packedLight = LightTexture.FULL_BRIGHT;

		super.render(data, level, mutablePos, partialTick, poseStack, buffer, packedLight);
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		PlumeRenderer plume = new PlumeRenderer(RocketryScience.ClientModEvents.plumeModel);
		poseStack.pushPose();

		int thrust = 0;
		if(data.enabled)
		{
			thrust = Math.max(1, Math.min((int) (data.thrustPercentage*15), 15));
			plume.renderPlume(data.frame, thrust, data.getBlockState(), poseStack, buffer, OverlayTexture.NO_OVERLAY);
		}
		poseStack.translate(0, -1, 0);
		packedLight = LevelRenderer.getLightColor(level, mutablePos.move(0, -1, 0));
		if(isInUI)
			packedLight = LightTexture.FULL_BRIGHT;

		BakedModel model = dispatcher.getBlockModel(data.nozzleState);
		for (RenderType rt : model.getRenderTypes(data.nozzleState, RandomSource.create(42), ModelData.EMPTY))
		{
			if(isInUI)
				dispatcher.renderSingleBlock(data.nozzleState, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
						model.getModelData(level, data.pos, data.nozzleState, ModelData.EMPTY), rt);
			else
			{
				dispatcher.renderSingleBlock(data.nozzleState, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
								  model.getModelData(level, mutablePos, data.nozzleState, ModelData.EMPTY), rt);
			}
		}
		mutablePos.move(0, 1, 0);
		poseStack.popPose();
	}
}
