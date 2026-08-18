package net.mistersecret312.rocketry_science.data.room;

import net.minecraft.core.BlockPos;
import net.mistersecret312.rocketry_science.environment.EnvironmentData;

public interface IOxygenNode
{
    float getOxygenOutput();
    int getVolumeBonus();
    int getBaseVolume();
    int getScanSpeed();
    boolean isActive();
    BlockPos getPos();
    EnvironmentData getTargetEnvironment();
}