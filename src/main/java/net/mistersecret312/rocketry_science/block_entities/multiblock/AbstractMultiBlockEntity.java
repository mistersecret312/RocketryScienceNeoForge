package net.mistersecret312.rocketry_science.block_entities.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public abstract class AbstractMultiBlockEntity extends BlockEntity
{
    private boolean isMaster = true;
    private BlockPos masterPos;
    private final Set<BlockPos> connections = new HashSet<>();

    public AbstractMultiBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.masterPos = pos;
    }

    protected abstract boolean isValidConnection(BlockPos otherPos, BlockState otherState);

    public void formNetwork()
    {
        if (level == null || level.isClientSide) return;

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(this.worldPosition);
        visited.add(this.worldPosition);

        while (!queue.isEmpty())
        {
            BlockPos current = queue.poll();
            
            for (Direction dir : Direction.values())
            {
                BlockPos neighborPos = current.relative(dir);
                if (!visited.contains(neighborPos)) {
                    BlockState neighborState = level.getBlockState(neighborPos);
                    
                    if (neighborState.is(this.getBlockState().getBlock()) && isValidConnection(neighborPos, neighborState))
                    {
                        visited.add(neighborPos);
                        queue.add(neighborPos);
                    }
                }
            }
        }

        BlockPos newMaster = this.worldPosition;
        if(visited.stream().findFirst().isPresent())
            newMaster = visited.stream().findFirst().get();

        for (BlockPos pos : visited)
        {
            if (level.getBlockEntity(pos) instanceof AbstractMultiBlockEntity be)
                be.setNetworkData(newMaster, visited);
        }
    }

    public void breakNetwork()
    {
        if (level == null || level.isClientSide) return;
        
        Set<BlockPos> networkToReset = new HashSet<>();
        
        if (isMaster)
            networkToReset.addAll(connections);
        else if (masterPos != null && level.getBlockEntity(masterPos) instanceof AbstractMultiBlockEntity masterBe)
        {
            networkToReset.addAll(masterBe.connections);
            networkToReset.add(masterPos);
        }
        
        networkToReset.remove(this.worldPosition);
        for (BlockPos pos : networkToReset)
        {
            if (level.getBlockEntity(pos) instanceof AbstractMultiBlockEntity be)
                be.resetData();
        }

        for (BlockPos pos : networkToReset)
        {
            if (level.getBlockEntity(pos) instanceof AbstractMultiBlockEntity be)
                if (be.masterPos.equals(be.getBlockPos()))
                    be.formNetwork();
        }
    }

    private void setNetworkData(BlockPos newMasterPos, Set<BlockPos> allBlocks)
    {
        this.masterPos = newMasterPos;
        this.isMaster = this.worldPosition.equals(newMasterPos);
        this.connections.clear();
        
        if (this.isMaster)
        {
            this.connections.addAll(allBlocks);
            this.connections.remove(this.worldPosition);
        }
        this.setChanged();
    }

    private void resetData()
    {
        this.isMaster = true;
        this.masterPos = this.worldPosition;
        this.connections.clear();
        this.setChanged();
    }

    public boolean isMaster()
    {
        return isMaster;
    }

    public BlockPos getMasterPos()
    {
        return masterPos;
    }

    public Set<BlockPos> getAllNetworkPositions()
    {
        if (!isMaster())
        {
            if (level != null && level.getBlockEntity(masterPos) instanceof AbstractMultiBlockEntity masterBe)
                return masterBe.getAllNetworkPositions();
            return Set.of(this.worldPosition);
        }

        Set<BlockPos> allBlocks = new HashSet<>(connections);
        allBlocks.add(this.worldPosition);
        return allBlocks;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.putBoolean("is_master", isMaster);
        
        if (masterPos != null)
            tag.putLong("master_pos", masterPos.asLong());
        
        if (isMaster && !connections.isEmpty())
        {
            long[] array = connections.stream().mapToLong(BlockPos::asLong).toArray();
            tag.putLongArray("connections", array);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        isMaster = tag.getBoolean("is_master");
        
        if (tag.contains("master_pos"))
            masterPos = BlockPos.of(tag.getLong("master_pos"));
        
        connections.clear();
        if (isMaster && tag.contains("connections"))
        {
            long[] array = tag.getLongArray("connections");
            for (long posLong : array)
                connections.add(BlockPos.of(posLong));
        }
    }
}