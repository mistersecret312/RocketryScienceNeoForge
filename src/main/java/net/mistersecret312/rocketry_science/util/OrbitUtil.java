package net.mistersecret312.rocketry_science.util;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.data.orbits.CelestialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.ConfiguredOrbit;
import net.mistersecret312.rocketry_science.data.orbits.OrbitConfig;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;

import java.util.*;

public class OrbitUtil
{
	public static HashMap<UUID, SpaceCraft> SPACECRAFT = new HashMap<>();

	public static final ResourceKey<CelestialBody> THE_SUN = ResourceKey.create(CelestialBody.REGISTRY_KEY,
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "sun"));
	public static final ResourceKey<CelestialBody> EARTH = ResourceKey.create(CelestialBody.REGISTRY_KEY,
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "earth"));

	public static Registry<CelestialBody> getCelestialRegistry(Level level)
	{
		return level.registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY);
	}

	public static CelestialBody getCelestialBody(ResourceKey<CelestialBody> key, Level level)
	{
		return getCelestialRegistry(level).get(key);
	}

	public static CelestialBody getCelestialBody(Level level)
	{
		Registry<CelestialBody> registry = getCelestialRegistry(level);
		for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
		{
			CelestialBody body = entry.getValue();
			if(body.getDimension().isPresent() && body.getDimension().get().equals(level.dimension()))
				return body;
		}

		return null;
	}

	public static CelestialBody getCelestialBodyByDimension(ResourceLocation location, Level level)
	{
		Registry<CelestialBody> registry = getCelestialRegistry(level);
		for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
		{
			CelestialBody body = entry.getValue();
			if(body.getDimension().isPresent() && body.getDimension().get().location().equals(location))
				return body;
		}

		return registry.get(EARTH);
	}

	public static ResourceKey<CelestialBody> getKey(CelestialBody body, Level level)
	{
		ResourceLocation rl = getCelestialRegistry(level).getKey(body);
		if(rl == null)
			return EARTH;
		return ResourceKey.create(CelestialBody.REGISTRY_KEY, rl);
	}

	public static Level getLevel(ResourceKey<CelestialBody> key, Level level)
	{
		CelestialBody body = getCelestialRegistry(level).get(key);
		if(body != null)
		{
			Optional<ResourceKey<Level>> dimension = body.getDimension();
			if(dimension.isPresent() && level.getServer() != null)
				return level.getServer().getLevel(dimension.get());
		}

		return level;
	}

	public static boolean bodyDimensionCheck(ResourceKey<CelestialBody> key, Level level)
	{
		CelestialBody body = getCelestialRegistry(level).get(key);
		if(body != null)
		{
			Optional<ResourceKey<Level>> dimension = body.getDimension();
			if(dimension.isPresent() && level.getServer() != null)
				return dimension.get().equals(level.dimension());
		}

		return false;
	}

	public static CelestialOrbit getCelestialOrbit(ResourceKey<CelestialBody> key, Level level)
	{
		CelestialBody body = getCelestialBody(key, level);
		return body.getOrbit();
	}

	public static List<CelestialBody> getChildren(CelestialBody parent, Level level)
	{
		Registry<CelestialBody> registry = getCelestialRegistry(level);
		List<CelestialBody> children = new ArrayList<>();

		for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
		{
			CelestialBody body = entry.getValue();
			if(body.getParentKey().location().equals(registry.getKey(parent)))
				children.add(body);
		}

		return children;
	}

	public static ConfiguredOrbit getDefaultLaunchOrbit(Level level)
	{
		CelestialBody body = getCelestialBody(level);
		if(body != null)
		{
			List<ConfiguredOrbit> orbits = new ArrayList<>(body.getSupportedOrbits());
			orbits.sort(Comparator.comparing(ConfiguredOrbit::orbit,
					Comparator.comparingDouble(OrbitConfig::getAltitude)));

			return orbits.getFirst();
		}
		return null;
	}

	public static double getSpaceHeight(Level level)
	{
		return (level.getMaxBuildHeight()-level.getMinBuildHeight())*2;
	}

	public static SpaceCraft getSpaceCraft(UUID uuid)
	{
		return SPACECRAFT.get(uuid);
	}

	public static void addSpaceCraft(SpaceCraft craft)
	{
		SPACECRAFT.put(craft.getUUID(), craft);
	}

	public static void removeSpaceCraft(UUID uuid)
	{
		SPACECRAFT.remove(uuid);
	}

	public static void clearSpaceCraft()
	{
		SPACECRAFT.clear();
	}
}
