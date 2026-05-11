package net.mistersecret312.rocketry_science.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.orbiting_objects.IOrbitObject;
import net.mistersecret312.rocketry_science.data.orbits.CelestialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.ConfiguredOrbit;

import java.util.List;
import java.util.Optional;

public class CelestialBody implements IOrbitObject<CelestialOrbit>
{
	public static final ResourceLocation CELESTIAL_BODY_LOCATION = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "celestial_body");
	public static final ResourceKey<Registry<CelestialBody>> REGISTRY_KEY = ResourceKey.createRegistryKey(CELESTIAL_BODY_LOCATION);

	public static final ResourceKey<CelestialBody> THE_SUN = ResourceKey.create(REGISTRY_KEY,
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "sun"));

	public static final Codec<CelestialBody> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.STRING.fieldOf("name").forGetter(CelestialBody::getName),
			ResourceLocation.CODEC.fieldOf("icon").forGetter(CelestialBody::getIcon),
			ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(CelestialBody::getDimension),
			ResourceKey.codec(REGISTRY_KEY).optionalFieldOf("parent", THE_SUN).forGetter(CelestialBody::getParentKey),
			Codec.DOUBLE.fieldOf("altitude").forGetter(CelestialBody::getAltitude),
			Codec.DOUBLE.fieldOf("period").forGetter(CelestialBody::getPeriod),
			Codec.BOOL.optionalFieldOf("has_atmosphere", false).forGetter(CelestialBody::hasAtmosphere),
			Codec.INT.optionalFieldOf("day_length", 20).forGetter(CelestialBody::getDayLength),
			Codec.DOUBLE.fieldOf("gravity").forGetter(CelestialBody::getGravity),
			Codec.DOUBLE.fieldOf("radius").forGetter(CelestialBody::getRadius),
			ConfiguredOrbit.CODEC.listOf().optionalFieldOf("supported_orbits", List.of()).forGetter(CelestialBody::getSupportedOrbits)
	).apply(inst, CelestialBody::new));

	private final String name;
	private final ResourceLocation icon;
	private final ResourceKey<Level> dimension;
	private final ResourceKey<CelestialBody> parentKey;
	private final double altitude;
	private final double period;
	private final boolean hasAtmosphere;
	private final int dayLength;
	private final double gravity;
	private final double radius;
	private final List<ConfiguredOrbit> supportedOrbits;

	private CelestialOrbit orbit = null;

	public CelestialBody(String name, ResourceLocation icon, Optional<ResourceKey<Level>> dimension, ResourceKey<CelestialBody> parentKey, double altitude, double period,
						 boolean hasAtmosphere, int dayLength, double gravity, double radius, List<ConfiguredOrbit> supportedOrbits)
	{
		this.name = name;
		this.icon = icon;
		this.dimension = dimension.orElse(null);
		this.parentKey = parentKey;
		this.altitude = altitude;
		this.period = period;
		this.supportedOrbits = supportedOrbits;

		this.hasAtmosphere = hasAtmosphere;
		this.dayLength = dayLength;
		this.gravity = gravity;
		this.radius = radius;

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

	public ResourceLocation getIcon()
	{
		return icon;
	}

	public Optional<ResourceKey<Level>> getDimension()
	{
		return Optional.ofNullable(dimension);
	}

	public double getAltitude()
	{
		return altitude;
	}

	public double getPeriod()
	{
		return period;
	}

	public boolean hasAtmosphere()
	{
		return hasAtmosphere;
	}

	public int getDayLength()
	{
		return dayLength;
	}

	public double getGravity()
	{
		return gravity;
	}

	public double getGravityMS2()
	{
		return gravity*9.8;
	}

	public double getGravitationalParameter()
	{
		return getGravityMS2()*getRadius()*getRadius();
	}

	public double getRadius()
	{
		return radius;
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
