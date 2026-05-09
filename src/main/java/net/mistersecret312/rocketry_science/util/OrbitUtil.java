package net.mistersecret312.rocketry_science.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.data.orbiting_objects.SpaceCraft;
import net.mistersecret312.rocketry_science.data.orbits.CelestialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.Orbit;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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

	public static CelestialOrbit getCelestialOrbit(ResourceKey<CelestialBody> key, Level level)
	{
		CelestialBody body = getCelestialBody(key, level);
		return body.getOrbit();
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
