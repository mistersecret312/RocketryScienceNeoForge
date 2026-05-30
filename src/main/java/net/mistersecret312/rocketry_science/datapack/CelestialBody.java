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
import net.mistersecret312.rocketry_science.environment.EnvironmentData;
import net.mistersecret312.rocketry_science.environment.modifiers.ModifierConfig;
import net.mistersecret312.rocketry_science.util.EnvironmentUtil;
import net.mistersecret312.rocketry_science.util.OrbitUtil;

import java.util.List;
import java.util.Optional;

import static net.mistersecret312.rocketry_science.util.EnvironmentUtil.EARTH;
import static net.mistersecret312.rocketry_science.util.EnvironmentUtil.LUNA;

public class CelestialBody implements IOrbitObject<CelestialOrbit>
{
	public static final ResourceLocation CELESTIAL_BODY_LOCATION = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "celestial_body");
	public static final ResourceKey<Registry<CelestialBody>> REGISTRY_KEY = ResourceKey.createRegistryKey(CELESTIAL_BODY_LOCATION);

	public static final Codec<CelestialBody> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.STRING.fieldOf("name").forGetter(CelestialBody::getName),
			ResourceLocation.CODEC.fieldOf("icon").forGetter(CelestialBody::getIcon),
			ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(CelestialBody::getDimension),
			ResourceKey.codec(REGISTRY_KEY).optionalFieldOf("parent").forGetter(CelestialBody::getParent),
			Codec.DOUBLE.fieldOf("altitude").forGetter(CelestialBody::getAltitude),
			Codec.DOUBLE.fieldOf("period").forGetter(CelestialBody::getPeriod),
			Codec.BOOL.optionalFieldOf("has_atmosphere", false).forGetter(CelestialBody::hasAtmosphere),
			Codec.INT.optionalFieldOf("day_length", 20).forGetter(CelestialBody::getDayLength),
			Codec.DOUBLE.fieldOf("radius").forGetter(CelestialBody::getRadius),
			ConfiguredOrbit.CODEC.listOf().optionalFieldOf("supported_orbits", List.of()).forGetter(CelestialBody::getSupportedOrbits),
			ModifierConfig.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(CelestialBody::getModifiers)
	).apply(inst, CelestialBody::new));

	private final String name;
	private final ResourceLocation icon;
	private final ResourceKey<Level> dimension;
	private final ResourceKey<CelestialBody> parentKey;
	private final double altitude;
	private final double period;
	private final boolean hasAtmosphere;
	private final int dayLength;
	private final double radius;
	private final List<ConfiguredOrbit> supportedOrbits;
	private final List<ModifierConfig> modifiers;

	private CelestialOrbit orbit = null;
	private EnvironmentData environment = null;

	public CelestialBody(String name, ResourceLocation icon, Optional<ResourceKey<Level>> dimension, Optional<ResourceKey<CelestialBody>> parentKey, double altitude, double period,
						 boolean hasAtmosphere, int dayLength, double radius, List<ConfiguredOrbit> supportedOrbits,
						 List<ModifierConfig> modifiers)
	{
		this.name = name;
		this.icon = icon;
		this.dimension = dimension.orElse(null);
		this.parentKey = parentKey.orElse(null);
		this.altitude = altitude;
		this.period = period;
		this.supportedOrbits = supportedOrbits;
		this.modifiers = modifiers;

		this.hasAtmosphere = hasAtmosphere;
		this.dayLength = dayLength;
		this.radius = radius;

		if(altitude == 0 || period == 0 || this.parentKey == null)
			return;
		this.orbit = new CelestialOrbit(this.parentKey, altitude, period, this);
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

	public EnvironmentData getEnvironment()
	{
		if(environment == null)
			return hasAtmosphere() ? EARTH : LUNA;
		return environment;
	}

	public void setEnvironment(EnvironmentData environment)
	{
		this.environment = environment;
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

	public double getOrbitAngle(long time)
	{
		if(getOrbit() == null)
			return 0;

		double orbitTime = time % getPeriod();
		double orbitPercentage = orbitTime / getPeriod();
		return orbitPercentage * 2d * Math.PI;
	}

	public boolean hasAtmosphere()
	{
		return hasAtmosphere;
	}

	public int getDayLength()
	{
		return dayLength;
	}

	public List<ModifierConfig> getModifiers()
	{
		return modifiers;
	}

	public double getRadius()
	{
		return radius;
	}

	public Optional<ResourceKey<CelestialBody>> getParent()
	{
		return Optional.ofNullable(this.parentKey);
	}

	public ResourceKey<CelestialBody> getParentKey()
	{
		return this.parentKey;
	}

	public List<ConfiguredOrbit> getSupportedOrbits()
	{
		return supportedOrbits;
	}
}
