package net.mistersecret312.rocketry_science.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.orbiting_objects.IOrbitObject;
import net.mistersecret312.rocketry_science.data.orbits.CelestialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.ConfiguredOrbit;

import java.util.List;

public class CelestialBody implements IOrbitObject<CelestialOrbit>
{
	public static final ResourceLocation CELESTIAL_BODY_LOCATION = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "celestial_body");
	public static final ResourceKey<Registry<CelestialBody>> REGISTRY_KEY = ResourceKey.createRegistryKey(CELESTIAL_BODY_LOCATION);

	public static final ResourceKey<CelestialBody> THE_SUN = ResourceKey.create(REGISTRY_KEY,
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "sun"));

	public static final Codec<CelestialBody> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.STRING.fieldOf("name").forGetter(CelestialBody::getName),
			ResourceKey.codec(REGISTRY_KEY).optionalFieldOf("parent", THE_SUN).forGetter(CelestialBody::getParentKey),
			Codec.DOUBLE.fieldOf("altitude").forGetter(CelestialBody::getAltitude),
			Codec.DOUBLE.fieldOf("period").forGetter(CelestialBody::getPeriod),
			ConfiguredOrbit.CODEC.listOf().optionalFieldOf("supported_orbits", List.of()).forGetter(CelestialBody::getSupportedOrbits)
	).apply(inst, CelestialBody::new));

	private final String name;
	private final ResourceKey<CelestialBody> parentKey;
	private final double altitude;
	private final double period;
	private final List<ConfiguredOrbit> supportedOrbits;

	private CelestialOrbit orbit = null;

	public CelestialBody(String name, ResourceKey<CelestialBody> parentKey, double altitude, double period,
						 List<ConfiguredOrbit> supportedOrbits)
	{
		this.name = name;
		this.parentKey = parentKey;
		this.altitude = altitude;
		this.period = period;
		this.supportedOrbits = supportedOrbits;

		if(altitude == 0 || period == 0)
			return;
		this.orbit = new CelestialOrbit(parentKey, altitude, period, this);
	}

	@Override
	public CelestialOrbit getOrbit()
	{
		return orbit;
	}

	@Override
	public void setOrbit(CelestialOrbit orbit)
	{

	}

	@Override
	public String getName()
	{
		return name;
	}

	public double getAltitude()
	{
		return altitude;
	}

	public double getPeriod()
	{
		return period;
	}

	public ResourceKey<CelestialBody> getParentKey()
	{
		return parentKey;
	}

	public List<ConfiguredOrbit> getSupportedOrbits()
	{
		return supportedOrbits;
	}
}
