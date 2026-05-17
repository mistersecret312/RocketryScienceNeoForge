package net.mistersecret312.rocketry_science.environment.modifiers;

import com.mojang.serialization.MapCodec;

public record ModifierType<C extends ModifierConfig>(MapCodec<C> codec) {}