package net.mistersecret312.rocketry_science.data.orbits;

import net.minecraft.resources.ResourceKey;
import net.mistersecret312.rocketry_science.data.orbiting_objects.SpaceCraft;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.orbit_types.DefaultOrbitConfig;

public class ArtificialOrbit extends Orbit<SpaceCraft>
{
	private final ResourceKey<CelestialBody> parent;
	private final SpaceCraft craft;

	private final ConfiguredOrbit orbit;

	public ArtificialOrbit(ResourceKey<CelestialBody> parent, SpaceCraft craft, ConfiguredOrbit orbitData)
	{
		this.parent = parent;
		this.craft = craft;
		this.orbit = orbitData;
	}

	@Override
	double getOrbitalPeriod()
	{
		return orbit.orbit().getPeriod();
	}

	@Override
	double getOrbitalAltitude()
	{
		return orbit.orbit().getAltitude();
	}

	@Override
	ResourceKey<CelestialBody> getParent()
	{
		return parent;
	}

	@Override
	SpaceCraft getOrbitingObject()
	{
		return craft;
	}
}
