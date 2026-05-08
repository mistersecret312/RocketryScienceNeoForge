package net.mistersecret312.rocketry_science.data.orbits;

import com.mojang.serialization.MapCodec;

public record OrbitType<C extends OrbitConfig>(MapCodec<C> codec) {}