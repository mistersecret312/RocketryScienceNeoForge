package net.mistersecret312.rocketry_science.block_entities.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import org.joml.Vector2i;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class RocketPadBlockEntity extends AbstractMultiBlockEntity
{
    public RocketPadBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockEntityInit.ROCKET_PAD.get(), pos, state);
    }

    @Override
    protected boolean isValidConnection(BlockPos otherPos, BlockState otherState)
    {
        return this.worldPosition.getY() == otherPos.getY();
    }

    public Vector2i getDimensions()
    {
        Set<BlockPos> network = getAllNetworkPositions();

        int minX = network.stream().mapToInt(BlockPos::getX).min().orElse(this.worldPosition.getX());
        int maxX = network.stream().mapToInt(BlockPos::getX).max().orElse(this.worldPosition.getX());

        int minZ = network.stream().mapToInt(BlockPos::getZ).min().orElse(this.worldPosition.getZ());
        int maxZ = network.stream().mapToInt(BlockPos::getZ).max().orElse(this.worldPosition.getZ());

        int sizeX = (maxX - minX) + 1;
        int sizeZ = (maxZ - minZ) + 1;

        return new Vector2i(sizeX, sizeZ);
    }


    public boolean isComplete()
    {
        Set<BlockPos> network = getAllNetworkPositions();
        if (network.isEmpty() || getTower() == null)
            return false;

        Vector2i dims = getDimensions();

        int expectedBlockCount = dims.x * dims.y;
        return network.size() == expectedBlockCount;
    }

    public AABB getOnPadBox()
    {
        LaunchTowerBlockEntity tower = getTower();
        if (!isComplete())
            return null;

        int towerHeight = tower.getTotalHeight();

        Set<BlockPos> padNetwork = getAllNetworkPositions();

        int minX = padNetwork.stream().mapToInt(BlockPos::getX).min().orElse(this.worldPosition.getX());
        int maxX = padNetwork.stream().mapToInt(BlockPos::getX).max().orElse(this.worldPosition.getX());

        int minZ = padNetwork.stream().mapToInt(BlockPos::getZ).min().orElse(this.worldPosition.getZ());
        int maxZ = padNetwork.stream().mapToInt(BlockPos::getZ).max().orElse(this.worldPosition.getZ());

        int padY = this.worldPosition.getY();

        return new AABB(
                minX, padY + 1, minZ,
                maxX + 1, padY + towerHeight, maxZ + 1
        );
    }

    public LaunchTowerBlockEntity getTower()
    {
        Level level = this.getLevel();
        if(level == null)
            return null;

        for (BlockPos pos : this.getAllNetworkPositions())
        {
            for(Direction direction : Direction.values())
            {
                BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));
                if(blockEntity instanceof LaunchTowerBlockEntity tower)
                    return tower;
            }
        }

        return null;
    }
}