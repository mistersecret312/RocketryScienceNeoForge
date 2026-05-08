package net.mistersecret312.rocketry_science.orbit_requirements;

import com.mojang.serialization.MapCodec;

public record RequirementType<T extends OrbitRequirement>(MapCodec<T> codec) {}
