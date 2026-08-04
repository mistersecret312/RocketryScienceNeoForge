package net.mistersecret312.rocketry_science.client.vessel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.neoforged.neoforge.client.model.data.ModelData;

public abstract class AbstractBlockDataRenderer<T extends BlockData> implements IBlockDataRenderer<T>
{
	@Override
	public void render(T data, Level level, BlockPos.MutableBlockPos mutablePos, float partialTick, PoseStack poseStack,
					   MultiBufferSource buffer, int packedLight)
	{
		boolean isInUI = data.stage.getVessel().isInUI();
		packedLight = LevelRenderer.getLightColor(level, mutablePos);
		if(isInUI)
			packedLight = LightTexture.FULL_BRIGHT;

		BlockEntityRenderDispatcher blockDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

		BakedModel model = dispatcher.getBlockModel(data.getBlockState());
		for (net.minecraft.client.renderer.RenderType rt : model.getRenderTypes(data.getBlockState(), RandomSource.create(42), ModelData.EMPTY))
		{
			if((data.getBlockState().getRenderShape() == RenderShape.MODEL
						&& data.getBlockState().hasBlockEntity())
					   || data.getBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED)
			{
				if (!data.extraData.isEmpty() && data.extraData != null)
				{
					BlockEntity blockEntity = BlockEntity.loadStatic(mutablePos, data.getBlockState(), data.extraData, Minecraft.getInstance().level.registryAccess());
					if (blockEntity != null)
					{
						blockEntity.setLevel(level);
						poseStack.pushPose();
						BlockEntityRenderer<BlockEntity> renderer = blockDispatcher.getRenderer(blockEntity);
						if(renderer != null)
							renderer.render(blockEntity, partialTick, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
						poseStack.popPose();
					}
				}
			}
			if(data.getBlockState().getRenderShape() == RenderShape.MODEL)
			{
				if(isInUI)
					dispatcher.renderSingleBlock(data.getBlockState(), poseStack, buffer,
							packedLight, OverlayTexture.NO_OVERLAY, model.getModelData(level, mutablePos,
									data.getBlockState(), ModelData.EMPTY), rt);
				else
				{
					dispatcher.getModelRenderer()
							  .tesselateBlock(level, model, data.getBlockState(), mutablePos, poseStack,
									  buffer.getBuffer(rt), false, RandomSource.create(42),
									  data.getBlockState().getSeed(mutablePos), OverlayTexture.NO_OVERLAY,
									  model.getModelData(level, mutablePos, data.getBlockState(), ModelData.EMPTY), rt);
				}
			}
		}
	}
}
