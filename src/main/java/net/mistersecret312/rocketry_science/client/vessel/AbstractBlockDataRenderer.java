package net.mistersecret312.rocketry_science.client.vessel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.neoforged.neoforge.client.model.data.ModelData;

public abstract class AbstractBlockDataRenderer<T extends BlockData> implements IBlockDataRenderer<T>
{
	@Override
	public void render(T data, Level level, BlockPos.MutableBlockPos mutablePos, float partialTick, PoseStack poseStack,
					   MultiBufferSource buffer, int packedLight)
	{
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
					BlockEntity blockEntity = BlockEntity.loadStatic(mutablePos.move(data.pos), data.getBlockState(), data.extraData, Minecraft.getInstance().level.registryAccess());
					mutablePos.move(-data.pos.getX(), -data.pos.getY(), -data.pos.getZ());
					if (blockEntity != null)
					{
						blockEntity.setLevel(level);
						if(blockEntity.getBlockState().getBlock() instanceof BaseEntityBlock baseEntity)
						{
							BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) baseEntity.getTicker(level, data.getBlockState(), blockEntity.getType());
							if(ticker != null)
							{
								ticker.tick(level, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity);
							}
						}
						poseStack.pushPose();
						blockDispatcher.render(blockEntity, partialTick, poseStack, buffer);
						poseStack.popPose();
					}
				}
			}
			if(data.getBlockState().getRenderShape() == RenderShape.MODEL)
			{
				dispatcher.renderBatched(data.getBlockState(), mutablePos.move(data.pos), level, poseStack, buffer.getBuffer(rt), true, RandomSource.create(42), model.getModelData(level, data.pos, data.getBlockState(), ModelData.EMPTY), null);
				mutablePos.move(-data.pos.getX(), -data.pos.getY(), -data.pos.getZ());
			}
		}
	}
}
