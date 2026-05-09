package net.mistersecret312.rocketry_science.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.rocketry_science.block_entities.multiblock.LaunchTowerBlockEntity;
import net.mistersecret312.rocketry_science.blocks.multiblock.AbstractMultiBlock;
import net.mistersecret312.rocketry_science.blocks.states.VerticalConnection;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;

public class LaunchTowerBlock extends AbstractMultiBlock
{
	public static final MapCodec<LaunchTowerBlock> CODEC = simpleCodec(LaunchTowerBlock::new);
	public static final EnumProperty<VerticalConnection> CONNECTION = EnumProperty.create("connection", VerticalConnection.class);

	public LaunchTowerBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(CONNECTION, VerticalConnection.NONE));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
											   BlockHitResult hitResult)
	{
		if(!level.isClientSide() && level.getBlockEntity(pos) instanceof LaunchTowerBlockEntity launchTower)
			System.out.println("Launch Tower Height - " + launchTower.getTotalHeight());

		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
								  LevelAccessor level, BlockPos pos, BlockPos neighborPos)
	{
		if (level.getBlockState(pos.below()).getBlock() instanceof LaunchTowerBlock && level.getBlockState(pos.above()).getBlock() instanceof LaunchTowerBlock)
			level.setBlock(pos, state.setValue(CONNECTION, VerticalConnection.MIDDLE), 2);
		else if (level.getBlockState(pos.below()).getBlock() instanceof LaunchTowerBlock)
			level.setBlock(pos, state.setValue(CONNECTION, VerticalConnection.BOTTOM), 2);
		else if (level.getBlockState(pos.above()).getBlock() instanceof LaunchTowerBlock)
			level.setBlock(pos, state.setValue(CONNECTION, VerticalConnection.UP), 2);
		else level.setBlock(pos, state.setValue(CONNECTION, VerticalConnection.NONE), 2);

		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
	{
		pBuilder.add(CONNECTION);
		super.createBlockStateDefinition(pBuilder);
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
	{
		return true;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		return BlockEntityInit.LAUNCH_TOWER.get().create(blockPos, blockState);
	}
}
