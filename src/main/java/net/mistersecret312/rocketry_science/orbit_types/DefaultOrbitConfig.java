package net.mistersecret312.rocketry_science.orbit_types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mistersecret312.rocketry_science.data.orbits.OrbitConfig;
import net.mistersecret312.rocketry_science.data.orbits.OrbitType;
import net.mistersecret312.rocketry_science.init.OrbitTypeInit;

public record DefaultOrbitConfig(String name, double altitude) implements OrbitConfig
{
	public static final MapCodec<DefaultOrbitConfig> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.STRING.fieldOf("name").forGetter(DefaultOrbitConfig::name),
			Codec.DOUBLE.fieldOf("altitude").forGetter(DefaultOrbitConfig::altitude)
	).apply(inst, DefaultOrbitConfig::new));

	@Override
	public OrbitType<?> getType()
	{
		return OrbitTypeInit.DEFAULT.get();
	}
}
