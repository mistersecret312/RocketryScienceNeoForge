package net.mistersecret312.rocketry_science.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.environment.PressureRating;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public enum RocketFuel implements StringRepresentable
{
    HYDROLOX(0, "hydrolox", List.of(stack -> stack.getFluid().is(RocketryScience.HYDROGEN),
                                 stack -> stack.getFluid().is(RocketryScience.OXYGEN)),
            380, 450, 1000, PressureRating.NORMAL);

    final int id;
    final String name;
    final List<Predicate<FluidStack>> fluids;
    final double atmosphericISP;
    final double vacuumISP;
    final double thrust_kN;
    final PressureRating rating;

    public static final Codec<RocketFuel> CODEC = StringRepresentable.fromEnum(RocketFuel::values);
    private static final IntFunction<RocketFuel> BY_ID = ByIdMap.continuous(
            fuel -> fuel.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, RocketFuel> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, fuel -> fuel.id);


    RocketFuel(int id, String name, List<Predicate<FluidStack>> fluids,
               double atmosphericISP, double vacuumISP, double thrust_kN, PressureRating rating)
    {
        this.id = id;
        this.name = name;
        this.fluids = fluids;
        this.atmosphericISP = atmosphericISP;
        this.vacuumISP = vacuumISP;
        this.thrust_kN = thrust_kN;
        this.rating = rating;
    }

    public List<Predicate<FluidStack>> getPropellants()
    {
        return fluids;
    }

    public double getAtmosphericISP()
    {
        return atmosphericISP;
    }

    public double getVacuumISP()
    {
        return vacuumISP;
    }

    public PressureRating getRating()
    {
        return rating;
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
