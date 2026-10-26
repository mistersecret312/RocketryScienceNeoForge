package net.mistersecret312.rocketry_science.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.orbiting_objects.IOrbitObject;
import net.mistersecret312.rocketry_science.data.orbits.CelestialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.ConfiguredOrbit;
import net.mistersecret312.rocketry_science.environment.EnvironmentData;
import net.mistersecret312.rocketry_science.environment.modifiers.ModifierConfig;
import net.mistersecret312.rocketry_science.modifiers.BreathableAtmosphereModifierConfig;
import net.mistersecret312.rocketry_science.util.StarUtil;
import org.joml.Vector2d;

import java.util.List;
import java.util.Optional;

import static net.mistersecret312.rocketry_science.util.EnvironmentUtil.EARTH;
import static net.mistersecret312.rocketry_science.util.EnvironmentUtil.LUNA;

public class CelestialBody implements IOrbitObject<CelestialOrbit>
{
	public static final ResourceLocation CELESTIAL_BODY_LOCATION = ResourceLocation.fromNamespaceAndPath(
			RocketryScience.MODID, "celestial_body");
	public static final ResourceKey<Registry<CelestialBody>> REGISTRY_KEY = ResourceKey.createRegistryKey(CELESTIAL_BODY_LOCATION);

	public static final Codec<CelestialBody> CODEC = RecordCodecBuilder.<CelestialBody>create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("name", "").forGetter(CelestialBody::getName),
			Codec.STRING.optionalFieldOf("type", "").forGetter(CelestialBody::getType),
			Codec.DOUBLE.optionalFieldOf("radius", -1d).forGetter(CelestialBody::getRadius),
			SurfaceData.CODEC.optionalFieldOf("surface").forGetter(CelestialBody::getSurfaceData),
			GalacticData.CODEC.optionalFieldOf("galactic").forGetter(CelestialBody::getGalacticData),
			OrbitData.CODEC.optionalFieldOf("orbit").forGetter(CelestialBody::getOrbitData),
			DisplayData.CODEC.optionalFieldOf("display").forGetter(CelestialBody::getDisplayData),
			StarData.CODEC.optionalFieldOf("star").forGetter(CelestialBody::getStarData),
			ConfiguredOrbit.CODEC.listOf().optionalFieldOf("supported_orbits", List.of()).forGetter(CelestialBody::getSupportedOrbits),
			ModifierConfig.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(CelestialBody::getModifiers)
	).apply(instance, CelestialBody::new)).flatXmap(
			body -> {
				boolean hasOrbit = body.getOrbitData().isPresent();
				boolean hasGalacticPos = body.getGalacticData().isPresent();

				if(hasOrbit ^ hasGalacticPos)
					return DataResult.success(body);
				else return DataResult.error(() -> "CelestialBody must contain either 'galactic' or 'orbit' fields");
			}, DataResult::success
	);

	public String name;
	private final String type;
	private final double radius;

	private final SurfaceData surfaceData;
	private final GalacticData galacticData;
	private final OrbitData orbitData;
	private final DisplayData displayData;
	private final StarData starData;

	private final List<ConfiguredOrbit> supportedOrbits;
	private final List<ModifierConfig> modifiers;

	private CelestialOrbit orbit = null;
	private EnvironmentData environment = null;

	public CelestialBody(String name, String type,
						 double radius, Optional<SurfaceData> surfaceData,
						 Optional<GalacticData> galacticData, Optional<OrbitData> orbitData,
						 Optional<DisplayData> displayData, Optional<StarData> starData,
						 List<ConfiguredOrbit> supportedOrbits, List<ModifierConfig> modifiers)
	{
		this.name = name;
		this.type = type;
		this.radius = radius;

		this.surfaceData = surfaceData.orElse(null);
		this.galacticData = galacticData.orElse(null);
		this.orbitData = orbitData.orElse(null);
		this.displayData = displayData.orElse(null);
		this.starData = starData.orElse(null);

		this.supportedOrbits = supportedOrbits;
		this.modifiers = modifiers;

		if(orbitData.isPresent())
			this.orbit = new CelestialOrbit(this.orbitData.parent, this.orbitData.altitude,
					this.orbitData.period, this.orbitData.isRetrograde, this);
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

	public Optional<SurfaceData> getSurfaceData()
	{
		return Optional.ofNullable(surfaceData);
	}

	public Optional<DisplayData> getDisplayData()
	{
		return Optional.ofNullable(displayData);
	}

	public Optional<OrbitData> getOrbitData()
	{
		return Optional.ofNullable(orbitData);
	}

	public Optional<GalacticData> getGalacticData()
	{
		return Optional.ofNullable(galacticData);
	}

	public Optional<StarData> getStarData()
	{
		if(starData != null)
			StarUtil.fillIfMissing(starData);

		return Optional.ofNullable(starData);
	}

	public Optional<ResourceKey<Level>> getDimension()
	{
		if(getSurfaceData().isPresent())
			return Optional.ofNullable(getSurfaceData().get().dimension());
		return Optional.empty();
	}

	public EnvironmentData getEnvironment()
	{
		if(environment == null)
		{
			this.environment = new EnvironmentData();
			if(modifiers.isEmpty())
				this.environment = EARTH;

			for(ModifierConfig modifier : this.modifiers)
				modifier.setup(this);
			return environment;
		}
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

	public String getType()
	{
		return type;
	}

	public boolean hasAtmosphere()
	{
		return getEnvironment().getPressureSeaLevel() > 0.05;
	}

	public List<ModifierConfig> getModifiers()
	{
		return modifiers;
	}

	public double getRadius()
	{
		return radius;
	}

	@Override
	public double getEpoch()
	{
		if(getOrbitData().isPresent())
			return getOrbitData().get().epoch();
		return 0;
	}

	public List<ConfiguredOrbit> getSupportedOrbits()
	{
		return supportedOrbits;
	}

	public MutableComponent getTranslatableName()
	{
		String translateString = "celestial_body."+this.getName().replace(':', '.');
		return Component.translatable(translateString);
	}

	public boolean isBreathableAtmosphere()
	{
		return modifiers.stream().anyMatch(config -> config instanceof BreathableAtmosphereModifierConfig);
	}

	public record GalacticData(ResourceKey<Galaxy> galaxy, Vector2d galPos, double escapeVelocity)
	{
		private static final Codec<Vector2d> POSITION = Codec.DOUBLE.listOf().comapFlatMap(f -> Util.fixedSize(f, 2).map(vec -> new Vector2d(vec.get(0), vec.get(1))), element -> List.of(element.get(0), element.get(1))).stable();
		private static final ResourceKey<Galaxy> MILKY_WAY = ResourceKey.create(Galaxy.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "milky_way"));

		public static final Codec<GalacticData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceKey.codec(Galaxy.REGISTRY_KEY).optionalFieldOf("galaxy", MILKY_WAY).forGetter(GalacticData::galaxy),
				POSITION.fieldOf("position").forGetter(GalacticData::galPos),
				Codec.DOUBLE.fieldOf("escape_velocity_kms").forGetter(GalacticData::escapeVelocity)
		).apply(instance, GalacticData::new));
	}

	public record OrbitData(ResourceKey<CelestialBody> parent, double altitude,
							double epoch, double period, double mapAltitude,
							boolean isRetrograde, int orbitColor)
	{
		public static final Codec<OrbitData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceKey.codec(REGISTRY_KEY).fieldOf("parent").forGetter(
						OrbitData::parent),
				Codec.DOUBLE.fieldOf("altitude").forGetter(OrbitData::altitude),
				Codec.doubleRange(0d, 360d).optionalFieldOf("epoch", 0d).forGetter(OrbitData::period),
				Codec.DOUBLE.fieldOf("period").forGetter(OrbitData::period),
				Codec.DOUBLE.optionalFieldOf("map_altitude", -1d).forGetter(OrbitData::mapAltitude),
				Codec.BOOL.optionalFieldOf("is_retrograde", false).forGetter(OrbitData::isRetrograde),
				Codec.INT.optionalFieldOf("orbit_color", -1).forGetter(OrbitData::orbitColor)
		).apply(instance, OrbitData::new));

		public double getMapAltitude()
		{
			if(mapAltitude == -1)
				return altitude;
			return mapAltitude;
		}
	}

	public record SurfaceData(ResourceKey<Level> dimension, int dayLength)
	{
		public static final Codec<SurfaceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(SurfaceData::dimension),
				Codec.INT.optionalFieldOf("day_length", 20).forGetter(SurfaceData::dayLength)
		).apply(instance, SurfaceData::new));
	}

	public record DisplayData(Optional<ResourceLocation> icon, int size, double iconScale, Optional<CollapseData> collapseData)
	{
		public static final Codec<DisplayData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(DisplayData::icon),
				Codec.INT.optionalFieldOf("icon_size", 32).forGetter(DisplayData::size),
				Codec.DOUBLE.optionalFieldOf("icon_scale", 1d).forGetter(DisplayData::iconScale),
				CollapseData.CODEC.optionalFieldOf("collapse_data").forGetter(
						DisplayData::collapseData)
		).apply(instance, DisplayData::new));

		public record CollapseData(Optional<ResourceLocation> collapsedIcon, double iconScale,
								   double systemSize, double fadeStartPixels, double fadeEndPixels)
		{
			public static final Codec<CollapseData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					ResourceLocation.CODEC.optionalFieldOf("icon").forGetter(
							CollapseData::collapsedIcon),
					Codec.DOUBLE.optionalFieldOf("icon_scale", 1d).forGetter(
							CollapseData::iconScale),
					Codec.DOUBLE.optionalFieldOf("system_size", -1d).forGetter(CollapseData::systemSize),
					Codec.DOUBLE.optionalFieldOf("fade_start_pixels", -1d).forGetter(CollapseData::fadeStartPixels),
					Codec.DOUBLE.optionalFieldOf("fade_end_pixels", -1d).forGetter(CollapseData::fadeEndPixels)
			).apply(instance, CollapseData::new));
		}
	}

	public static class StarData
	{
		public static final Codec<StarData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.DOUBLE.optionalFieldOf("luminosity", 0d).forGetter(StarData::getLuminosity),
				Codec.DOUBLE.optionalFieldOf("surface_temperature", 0d).forGetter(StarData::getSurfaceTemperature),
				Codec.STRING.fieldOf("spectral_class").forGetter(StarData::getSpectralClass)
		).apply(instance, StarData::new));

		public double luminosity;
		public double surfaceTemperature;
		public final String spectralClass;

		public StarData(double luminosity, double surfaceTemperature, String spectralClass)
		{
			this.luminosity = luminosity;
			this.surfaceTemperature = surfaceTemperature;
			this.spectralClass = spectralClass;
		}

		public String getSpectralClass()
		{
			return spectralClass;
		}

		public double getLuminosity()
		{
			return luminosity;
		}

		public double getSurfaceTemperature()
		{
			return surfaceTemperature;
		}
	}
}
