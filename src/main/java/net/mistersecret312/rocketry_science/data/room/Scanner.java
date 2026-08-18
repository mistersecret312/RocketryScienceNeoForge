package net.mistersecret312.rocketry_science.data.room;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.mistersecret312.rocketry_science.environment.EnvironmentData;
import net.mistersecret312.rocketry_science.util.EnvironmentUtil;

import java.util.UUID;

public class Scanner
{
    private final ServerLevel level;
    private final BlockPos startPos;
    private final LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
    private final LongOpenHashSet visited = new LongOpenHashSet();
    private final LongOpenHashSet boundaries = new LongOpenHashSet();
    private final LongOpenHashSet foundNodes = new LongOpenHashSet();
    private final float initialConcentration;
    private UUID expandingRoomId = null;

    private final boolean isProportional;
    private BlockPos breachPos = null;

    private int maxVolume;
    private int blocksPerTick;
    private EnvironmentData capturedEnvironment = null;
    private boolean isFinished = false;
    private boolean isFailed = false;

    public Scanner(ServerLevel level, BlockPos startPos, int maxVolume,
                   boolean isProportional, int blocksPerTick, float initialConcentration, BlockPos breachPos)
    {
        this.level = level;
        this.maxVolume = maxVolume;
        this.isProportional = isProportional;
        this.blocksPerTick = blocksPerTick;
        this.initialConcentration = initialConcentration;
        this.startPos = startPos;
        this.breachPos = breachPos != null ? breachPos : startPos;
        long start = startPos.asLong();
        this.queue.enqueue(start);
        this.visited.add(start);
    }

    public boolean tick()
    {
        if (isFinished)
            return true;

        int processed = 0;
        while (!queue.isEmpty() && processed < blocksPerTick)
        {
            long currentLong = queue.dequeueLong();
            BlockPos currentPos = BlockPos.of(currentLong);

            BlockEntity be = level.getBlockEntity(currentPos);
            if (be instanceof IOxygenNode node)
            {
                if(foundNodes.add(node.getPos().asLong()))
                {
                    if(foundNodes.size() == 1)
                    {
                        this.maxVolume = Math.max(this.maxVolume, node.getBaseVolume());
                        this.blocksPerTick = Math.max(this.blocksPerTick, node.getScanSpeed());
                        this.capturedEnvironment = node.getTargetEnvironment();
                    }
                    else this.maxVolume += node.getVolumeBonus();
                }
            }

            for (Direction dir : Direction.values())
            {
                BlockPos neighborPos = currentPos.relative(dir);
                long neighborLong = neighborPos.asLong();

                if (visited.contains(neighborLong) || boundaries.contains(neighborLong))
                    continue;

                if (EnvironmentUtil.canGasFlow(level, currentPos, neighborPos, dir))
                {
                    visited.add(neighborLong);
                    queue.enqueue(neighborLong);

                    if (visited.size() > maxVolume)
                    {
                        isFailed = true;
                        isFinished = true;
                        return true;
                    }
                } else boundaries.add(neighborLong);
            }
            processed++;
        }

        if (queue.isEmpty())
            isFinished = true;

        return isFinished;
    }

    public boolean isProportional()
    {
        return isProportional;
    }

    public BlockPos getStartPos()
    {
        return startPos;
    }
    public void setExpandingRoomUUID(UUID roomId)
    {
        this.expandingRoomId = roomId;
    }
    public UUID getExpandingRoomUUID()
    {
        return expandingRoomId;
    }
    public boolean hasFailed()
    {
        return isFailed;
    }
    public LongOpenHashSet getVisited()
    {
        return visited;
    }
    public LongOpenHashSet getBoundaries()
    {
        return boundaries;
    }
    public LongOpenHashSet getFoundNodes()
    {
        return foundNodes;
    }
    public float getInitialConcentration()
    {
        return initialConcentration;
    }
    public EnvironmentData getCapturedEnvironment()
    {
        return capturedEnvironment;
    }
    public BlockPos getBreachPos()
    {
        return breachPos;
    }
}