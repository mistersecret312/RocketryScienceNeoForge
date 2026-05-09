package net.mistersecret312.rocketry_science.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.ServerLinks;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

public enum RocketMaterial implements StringRepresentable
{
    STAINLESS_STEEL(0, "stainless_steel", 1.0, 1.0, 1.0);
    String name;
    double massCoefficient;
    double thrustCoefficient;
    double efficiencyCoefficient;

    int id;

    public static final Codec<RocketMaterial> CODEC = StringRepresentable.fromEnum(RocketMaterial::values);
    private static final IntFunction<RocketMaterial> BY_ID = ByIdMap.continuous(
            material -> material.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, RocketMaterial> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, material -> material.id);

    RocketMaterial(int id, String name, double massCoefficient,
                   double thrustCoefficient, double efficiencyCoefficient)
    {
        this.id = id;
        this.name = name;
        this.massCoefficient = massCoefficient;
        this.thrustCoefficient = thrustCoefficient;
        this.efficiencyCoefficient = efficiencyCoefficient;
    }


    public double getMassCoefficient()
    {
        return massCoefficient;
    }

    public double getThrustCoefficient()
    {
        return thrustCoefficient;
    }

    public double getEfficiencyCoefficient()
    {
        return efficiencyCoefficient;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }
}
