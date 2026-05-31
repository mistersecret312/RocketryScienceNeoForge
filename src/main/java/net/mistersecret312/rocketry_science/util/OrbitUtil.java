package net.mistersecret312.rocketry_science.util;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.data.orbits.CelestialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.ConfiguredOrbit;
import net.mistersecret312.rocketry_science.data.orbits.Orbit;
import net.mistersecret312.rocketry_science.data.orbits.OrbitConfig;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.datapack.SolarSystem;
import org.joml.Vector2d;

import java.util.*;

public class OrbitUtil
{
	public static HashMap<UUID, SpaceCraft> SPACECRAFT = new HashMap<>();
	public static HashMap<ResourceKey<CelestialBody>, SolarSystem> SYSTEM_MAP = new HashMap<>();

	public static final ResourceKey<CelestialBody> THE_SUN = ResourceKey.create(CelestialBody.REGISTRY_KEY,
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "sun"));
	public static final ResourceKey<CelestialBody> EARTH = ResourceKey.create(CelestialBody.REGISTRY_KEY,
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "earth"));

	public static Registry<CelestialBody> getCelestialRegistry(RegistryAccess registryAccess)
	{
		return registryAccess.registryOrThrow(CelestialBody.REGISTRY_KEY);
	}

	public static Registry<SolarSystem> getSystemRegistry(Level level)
	{
		return level.registryAccess().registryOrThrow(SolarSystem.REGISTRY_KEY);
	}

	public static CelestialBody getCelestialBody(ResourceKey<CelestialBody> key, Level level)
	{
		return getCelestialRegistry(level.registryAccess()).get(key);
	}

	public static CelestialBody getCelestialBody(Level level)
	{
		Registry<CelestialBody> registry = getCelestialRegistry(level.registryAccess());
		for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
		{
			CelestialBody body = entry.getValue();
			if(body.getDimension().isPresent() && body.getDimension().get().equals(level.dimension()))
				return body;
		}

		return null;
	}

	public static CelestialBody getCelestialBodyByDimension(ResourceLocation location, RegistryAccess registryAccess)
	{
		Registry<CelestialBody> registry = getCelestialRegistry(registryAccess);
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
		ResourceLocation rl = getCelestialRegistry(level.registryAccess()).getKey(body);
		if(rl == null)
			return EARTH;
		return ResourceKey.create(CelestialBody.REGISTRY_KEY, rl);
	}

	public static Level getLevel(ResourceKey<CelestialBody> key, Level level)
	{
		CelestialBody body = getCelestialRegistry(level.registryAccess()).get(key);
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
		CelestialBody body = getCelestialRegistry(level.registryAccess()).get(key);
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
		Registry<CelestialBody> registry = getCelestialRegistry(level.registryAccess());
		List<CelestialBody> children = new ArrayList<>();

		for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
		{
			CelestialBody body = entry.getValue();
			if(body.getParent().isPresent() && body.getParentKey().location().equals(registry.getKey(parent)))
				children.add(body);
		}

		return children;
	}

	public static List<CelestialBody> getAllChildren(CelestialBody parent, Level level)
	{
		Registry<CelestialBody> registry = getCelestialRegistry(level.registryAccess());
		List<CelestialBody> children = new ArrayList<>();

		for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
		{
			CelestialBody body = entry.getValue();
			if(body.getParent().isPresent() && getHighestOrderBody(body, level.registryAccess()).equals(parent))
				children.add(body);
		}

		return children;
	}
	
	public static CelestialBody getHighestOrderBody(CelestialBody body, RegistryAccess registryAccess)
	{
		Registry<CelestialBody> registry = getCelestialRegistry(registryAccess);
		CelestialBody current = body;
		if(current.getParentKey() == null)
			return current;

		CelestialBody parent = registry.get(current.getParentKey());
		while(parent != null)
		{
			current = parent;
			if(parent.getParentKey() != null)
				parent = registry.get(parent.getParentKey());
			else return current;
		}

		return current;
	}

	public static Vector2d getOrderPosition(long tick, Orbit<?> orbit, RegistryAccess registryAccess, CelestialBody finish)
	{
		Registry<CelestialBody> registry = getCelestialRegistry(registryAccess);
		Vector2d position = orbit.getPosition(tick, registryAccess);

		CelestialBody current;
		if(orbit.getOrbitingObject() instanceof CelestialBody celestialBody)
			current = celestialBody;
		else
		{
			current = getCelestialRegistry(registryAccess).get(orbit.getParent(registryAccess));
			if(current != null && current.getOrbit() != null && !current.equals(finish))
				position.add(current.getOrbit().getPosition(tick, registryAccess));
		}

		if(current == null || current.getParentKey() == null)
			return position;
		if(current.equals(finish) && orbit instanceof CelestialOrbit)
			return new Vector2d();

		CelestialBody parent = registry.get(current.getParentKey());
		while(parent != null && !parent.equals(finish))
		{
			if(parent.getOrbit() != null)
			{
				Vector2d offset = parent.getOrbit().getPosition(tick, registryAccess);
				position.add(offset);
			}
			if(parent.getParentKey() != null)
				parent = registry.get(parent.getParentKey());
			else return position;
		}

		return position;
	}

	public static SolarSystem getSystem(CelestialBody body, Level level)
	{
		CelestialBody highest = getHighestOrderBody(body, level.registryAccess());
		ResourceLocation location = getCelestialRegistry(level.registryAccess()).getKey(highest);
		if(location != null)
		{
			ResourceKey<CelestialBody> key = ResourceKey.create(CelestialBody.REGISTRY_KEY, location);
			return SYSTEM_MAP.get(key);
		}
		return SYSTEM_MAP.get(THE_SUN);
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

	public static CelestialBody findCommonAncestor(RegistryAccess registryAccess, CelestialBody a, CelestialBody b)
	{
		if (a == null || b == null)
			return null;
		if (a.equals(b))
			return a;

		Set<CelestialBody> ancestorsOfA = new HashSet<>();
		CelestialBody currentA = a;
		while (currentA != null)
		{
			ancestorsOfA.add(currentA);

			if (currentA.getParent().isPresent())
				currentA = OrbitUtil.getCelestialRegistry(registryAccess).get(currentA.getParentKey());
			else currentA = null;
		}

		CelestialBody currentB = b;
		while (currentB != null)
		{
			if (ancestorsOfA.contains(currentB))
				return currentB;

			if (currentB.getParent().isPresent())
				currentB = OrbitUtil.getCelestialRegistry(registryAccess).get(currentB.getParentKey());
			else currentB = null;

		}

		return null;
	}

	public static double getRadiusRelativeToAncestor(Level level, CelestialBody body, CelestialBody ancestor) {
		double r = 0;
		CelestialBody current = body;
		while (current != null && !current.equals(ancestor))
		{
			r += current.getOrbit().getOrbitalAltitude() + current.getRadius();
			current = getCelestialBodyByDimension(current.getParentKey().location(), level.registryAccess());
		}
		return Math.max(r, 1.0);
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


	public static void init(Level level)
	{
		SYSTEM_MAP.clear();
		SPACECRAFT.clear();

		Registry<CelestialBody> bodyRegistry = getCelestialRegistry(level.registryAccess());
		Registry<SolarSystem> systemRegistry = getSystemRegistry(level);

		for(Map.Entry<ResourceKey<SolarSystem>, SolarSystem> systemEntry : systemRegistry.entrySet())
			for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> bodyEntry : bodyRegistry.entrySet())
			{
				if(systemEntry.getValue().getStar().equals(bodyEntry.getKey()))
					SYSTEM_MAP.put(bodyEntry.getKey(), systemEntry.getValue());
			}
	}
}
