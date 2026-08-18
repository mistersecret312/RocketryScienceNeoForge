package net.mistersecret312.rocketry_science.data.room;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.rocketry_science.util.EnvironmentUtil;

public class Leak
{
    private final Long2IntOpenHashMap distanceMap = new Long2IntOpenHashMap();
    private final BlockPos leakOrigin;
    private Vec3 yeetVector = Vec3.ZERO;

    public Leak(BlockPos leakOrigin)
    {
        this.leakOrigin = leakOrigin;
        this.distanceMap.defaultReturnValue(-1);
    }

    public void setDistance(long posLong, int distance)
    {
        this.distanceMap.put(posLong, distance);
    }

    public boolean contains(long posLong)
    {
        return this.distanceMap.containsKey(posLong);
    }

    public Vec3 getSuctionVector(Level level, BlockPos currentPos, Vec3 exactPos)
    {
        long currentLong = currentPos.asLong();
        int currentDist = distanceMap.get(currentLong);

        if (currentDist <= 0.5)
            return yeetVector;

        Vec3 flowVector = Vec3.ZERO;
        boolean foundPath = false;

        for (Direction dir : Direction.values())
        {
            BlockPos neighborPos = currentPos.relative(dir);
            long neighborLong = neighborPos.asLong();

            int neighborDist = distanceMap.get(neighborLong);

            if (neighborDist != -1 && neighborDist < currentDist)
            {
                if (EnvironmentUtil.canGasFlow(level, currentPos, neighborPos, dir))
                {
                    flowVector = flowVector.add(dir.getStepX(), dir.getStepY(), dir.getStepZ());
                    foundPath = true;
                }
            }
        }

        Vec3 directToLeak = Vec3.atCenterOf(leakOrigin).subtract(exactPos).normalize();

        if (foundPath)
        {
            flowVector = flowVector.normalize();
            return flowVector.scale(0.85).add(directToLeak.scale(0.15)).normalize();
        }

        return directToLeak;
    }

    public static Leak generateFlowField(ServerLevel level, BlockPos leakPos, LongOpenHashSet roomInterior)
    {
        Leak leak = new Leak(leakPos);
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();

        long leakLong = leakPos.asLong();
        leak.setDistance(leakLong, 0);
        queue.enqueue(leakLong);

        Vec3 escape = Vec3.ZERO;
        for (Direction dir : Direction.values())
        {
            BlockPos adj = leakPos.relative(dir);
            if (roomInterior.contains(adj.asLong()))
                escape = escape.add(dir.getOpposite().getStepX(), dir.getOpposite().getStepY(), dir.getOpposite().getStepZ());
        }

        Vec3 rawYeet = escape.normalize();
        if (Math.abs(rawYeet.x) > 0.1 || Math.abs(rawYeet.z) > 0.1)
            leak.yeetVector = new Vec3(rawYeet.x, 0, rawYeet.z).normalize();
        else leak.yeetVector = rawYeet;

        if (leak.yeetVector.lengthSqr() == 0)
            leak.yeetVector = new Vec3(0, 0.5, 0).normalize();

        while (!queue.isEmpty())
        {
            long currentLong = queue.dequeueLong();
            BlockPos currentPos = BlockPos.of(currentLong);
            int currentDist = leak.distanceMap.get(currentLong);

            for (Direction dir : Direction.values())
            {
                BlockPos neighborPos = currentPos.relative(dir);
                long neighborLong = neighborPos.asLong();

                if (roomInterior.contains(neighborLong) && !leak.contains(neighborLong))
                    if (EnvironmentUtil.canGasFlow(level, currentPos, neighborPos, dir))
                    {
                        leak.setDistance(neighborLong, currentDist + 1);
                        queue.enqueue(neighborLong);
                    }
            }
        }

        return leak;
    }
}