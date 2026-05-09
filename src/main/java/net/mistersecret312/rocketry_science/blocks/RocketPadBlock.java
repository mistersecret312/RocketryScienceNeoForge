package net.mistersecret312.rocketry_science.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.blocks.multiblock.AbstractMultiBlock;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public class RocketPadBlock extends AbstractMultiBlock
{
	public static final MapCodec<RocketPadBlock> CODEC = simpleCodec(RocketPadBlock::new);

	public RocketPadBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
											   BlockHitResult hitResult)
	{
		if(!level.isClientSide() && level.getBlockEntity(pos) instanceof RocketPadBlockEntity pad)
		{
			if(player.isShiftKeyDown())
			{
				AABB box = pad.getOnPadBox();
				for(double x = box.minX; x < box.maxX; x++)
					for(double y = box.minY; y < box.maxY; y++)
						for(double z = box.minZ; z < box.maxZ; z++)
						{
							level.setBlock(new BlockPos((int) x, (int) y, (int) z), Blocks.GLASS.defaultBlockState(), 3);
						}
			}

			if(pad.isComplete())
			{
				Vector2i size = pad.getDimensions();
				System.out.println("Pad Dimensions - " + size);
				System.out.println("Pad Launch Tower Height - " + pad.getTower().getTotalHeight());
			}
			else System.out.println("Rocket Pad is incomplete");
		}

		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		return BlockEntityInit.ROCKET_PAD.get().create(blockPos, blockState);
	}
}
