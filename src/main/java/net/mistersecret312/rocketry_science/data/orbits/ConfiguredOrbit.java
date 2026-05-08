package net.mistersecret312.rocketry_science.data.orbits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.mistersecret312.rocketry_science.init.OrbitTypeInit;
import net.mistersecret312.rocketry_science.orbit_requirements.OrbitRequirement;

import java.util.List;

public record ConfiguredOrbit(OrbitConfig orbit, List<OrbitRequirement> requirements)
{
	public static final Codec<ConfiguredOrbit> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			OrbitConfig.CODEC.fieldOf("orbit").forGetter(ConfiguredOrbit::orbit),
			OrbitRequirement.CODEC.listOf().optionalFieldOf("requirements", List.of()).forGetter(ConfiguredOrbit::requirements)
	).apply(inst, ConfiguredOrbit::new));
}

