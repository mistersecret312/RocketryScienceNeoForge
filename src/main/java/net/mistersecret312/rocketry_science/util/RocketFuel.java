package net.mistersecret312.rocketry_science.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public enum RocketFuel implements StringRepresentable
{
    HYDROLOX(0, "hydrolox", List.of(stack -> stack.getFluid().is(RocketryScience.HYDROGEN),
                                 stack -> stack.getFluid().is(RocketryScience.OXYGEN)),
            380d, 600);

    int id;
    String name;
    List<Predicate<FluidStack>> fluids;
    double efficiency;
    double thrust_kN;

    public static final Codec<RocketFuel> CODEC = StringRepresentable.fromEnum(RocketFuel::values);
    private static final IntFunction<RocketFuel> BY_ID = ByIdMap.continuous(
            fuel -> fuel.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, RocketFuel> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, fuel -> fuel.id);


    RocketFuel(int id, String name, List<Predicate<FluidStack>> fluids, double efficiency, double thrust_kN)
    {
        this.id = id;
        this.name = name;
        this.fluids = fluids;
        this.efficiency = efficiency;
        this.thrust_kN = thrust_kN;
    }

    public List<Predicate<FluidStack>> getPropellants()
    {
        return fluids;
    }

    public double getEfficiency()
    {
        return efficiency;
    }

    public double getThrustKiloNewtons()
    {
        return thrust_kN;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }
}
