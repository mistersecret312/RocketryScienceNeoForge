package net.mistersecret312.rocketry_science.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.orbiting_objects.SpaceCraft;
import net.mistersecret312.rocketry_science.data.orbits.CelestialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.Orbit;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;

import java.util.*;

public class OrbitUtil
{
	public static HashMap<UUID, SpaceCraft> SPACECRAFT = new HashMap<>();

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

		return registry.get(ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "earth"));
	}

	public static CelestialOrbit getCelestialOrbit(ResourceKey<CelestialBody> key, Level level)
	{
		CelestialBody body = getCelestialBody(key, level);
		return body.getOrbit();
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
