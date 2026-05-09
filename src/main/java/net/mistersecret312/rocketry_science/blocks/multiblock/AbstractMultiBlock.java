package net.mistersecret312.rocketry_science.blocks.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.rocketry_science.block_entities.multiblock.AbstractMultiBlockEntity;

public abstract class AbstractMultiBlock extends BaseEntityBlock
{
    public AbstractMultiBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide && !state.is(oldState.getBlock()))
        {
            if (level.getBlockEntity(pos) instanceof AbstractMultiBlockEntity be)
                be.formNetwork();
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (!state.is(newState.getBlock()))
        {
            if (level.getBlockEntity(pos) instanceof AbstractMultiBlockEntity be)
                be.breakNetwork();

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }
}