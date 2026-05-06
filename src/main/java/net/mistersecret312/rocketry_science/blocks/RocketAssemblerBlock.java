package net.mistersecret312.rocketry_science.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;

public class RocketAssemblerBlock extends BaseEntityBlock
{
	public static final MapCodec<RocketAssemblerBlock> CODEC = simpleCodec(RocketAssemblerBlock::new);

	public RocketAssemblerBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
											   BlockHitResult hitResult)
	{
		if(level.getBlockEntity(pos) instanceof RocketAssemblerBlockEntity assembler)
		{
			if(player.isShiftKeyDown())
				assembler.stopTriggeredAnim("spin", "spin");
			else assembler.triggerAnim("spin", "spin");
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
		return BlockEntityInit.ROCKET_ASSEMBLER.get().create(blockPos, blockState);
	}
}
