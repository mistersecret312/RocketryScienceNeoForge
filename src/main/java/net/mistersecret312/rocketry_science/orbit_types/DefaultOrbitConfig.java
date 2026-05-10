package net.mistersecret312.rocketry_science.orbit_types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mistersecret312.rocketry_science.data.orbits.OrbitConfig;
import net.mistersecret312.rocketry_science.data.orbits.OrbitType;
import net.mistersecret312.rocketry_science.init.OrbitTypeInit;

public record DefaultOrbitConfig(String name, double altitude, double period) implements OrbitConfig
{
	public static MapCodec<DefaultOrbitConfig> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.STRING.fieldOf("name").forGetter(DefaultOrbitConfig::name),
			Codec.DOUBLE.fieldOf("altitude").forGetter(DefaultOrbitConfig::altitude),
			Codec.DOUBLE.fieldOf("period").forGetter(DefaultOrbitConfig::period)
	).apply(inst, DefaultOrbitConfig::new));

	@Override
	public OrbitType<?> getType()
	{
		return OrbitTypeInit.DEFAULT.get();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public double getAltitude()
	{
		return altitude;
	}

	@Override
	public double getPeriod()
	{
		return period;
	}
}
