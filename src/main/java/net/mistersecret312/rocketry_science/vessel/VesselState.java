package net.mistersecret312.rocketry_science.vessel;

import net.minecraft.util.StringRepresentable;

public enum VesselState implements StringRepresentable
{
    IDLE,
    TAKEOFF,
    COASTING,
    STAGING,
    LANDING,
    ORBIT;

    @Override
    public String getSerializedName()
    {
        return this.name();
    }
}