package net.mistersecret312.rocketry_science.orbit_types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mistersecret312.rocketry_science.data.orbits.OrbitConfig;
import net.mistersecret312.rocketry_science.data.orbits.OrbitType;
import net.mistersecret312.rocketry_science.init.OrbitTypeInit;

public record VanAllenOrbit(String name, double altitude, double period, int radiationLevel) implements OrbitConfig
{
	public static MapCodec<VanAllenOrbit> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.STRING.fieldOf("name").forGetter(VanAllenOrbit::name),
			Codec.DOUBLE.fieldOf("altitude").forGetter(VanAllenOrbit::altitude),
			Codec.DOUBLE.fieldOf("period").forGetter(VanAllenOrbit::period),
			Codec.INT.fieldOf("radiation_level").forGetter(VanAllenOrbit::radiationLevel)
	).apply(inst, VanAllenOrbit::new));

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
