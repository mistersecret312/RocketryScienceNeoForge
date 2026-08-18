package net.mistersecret312.rocketry_science.data.room;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Decompression
{
    private final Room room;
    private final Leak flowField;
    private final BlockPos leakPos;
    private int remainingTicks;
    private final int totalTicks;

    private final AABB cachedBounds;

    public Decompression(Room room, Leak flowField, BlockPos leakPos, int durationTicks)
    {
        this.room = room;
        this.flowField = flowField;
        this.leakPos = leakPos;
        this.remainingTicks = durationTicks;
        this.totalTicks = durationTicks;

        this.cachedBounds = room.calculateBounds().inflate(3);
    }

    public boolean tick(ServerLevel level)
    {
        remainingTicks--;

        float fractionRemaining = (float) remainingTicks / (float) totalTicks;
        room.setCurrentOxygen(level, room.getCurrentOxygen() * fractionRemaining);

        if (remainingTicks % 15 == 0)
            level.playSound(null, leakPos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 0.4f, 1.8f);

        if (level.random.nextFloat() < 0.3f)
        {
            Vec3 yeet = flowField.getSuctionVector(level, leakPos, leakPos.getCenter());
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    leakPos.getCenter().x, leakPos.getCenter().y, leakPos.getCenter().z,
                    16,
                    yeet.x * 0.2, yeet.y * 0.2, yeet.z * 0.2,
                    0.5
            );
        }

        double basePullStrength = 0.2;
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, cachedBounds);
        for (Entity entity : entities)
        {
            if (entity instanceof Player player && (player.isSpectator()))
                continue;

            Vec3 exactPos = entity.getBoundingBox().getCenter();
            BlockPos evalPos = BlockPos.containing(exactPos);

            boolean inInterior = room.getInteriorSet().contains(evalPos.asLong());
            boolean justOutsideBreach = evalPos.distManhattan(leakPos) <= 2;

            if (inInterior || justOutsideBreach)
            {
                Vec3 pullDirection = flowField.getSuctionVector(level, evalPos, exactPos);

                Vec3 currentMotion = entity.getDeltaMovement();
                double strength = basePullStrength * (fractionRemaining + 0.2);

                double yPull = pullDirection.y * strength * 0.5;
                double horizontalPull = Math.sqrt(pullDirection.x * pullDirection.x + pullDirection.z * pullDirection.z);
                if (pullDirection.y > 0 && horizontalPull > 0.05)
                    yPull = 0;


                entity.setDeltaMovement(currentMotion.add(
                        pullDirection.x * strength,
                        yPull,
                        pullDirection.z * strength
                ));

                if(entity instanceof ServerPlayer player)
                    player.connection.send(new ClientboundSetEntityMotionPacket(player));
                else entity.hasImpulse = true;

                if (level.random.nextFloat() < 0.3f)
                {
                    level.sendParticles(
                            ParticleTypes.CLOUD,
                            entity.getX(), entity.getY() + 0.5, entity.getZ(),
                            2,
                            pullDirection.x * 0.2, pullDirection.y * 0.2, pullDirection.z * 0.2,
                            0.05
                    );
                }
            }
        }

        return remainingTicks <= 0;
    }

    public Room getRoom()
    {
        return room;
    }
}