package net.mistersecret312.rocketry_science.data.orbits;

import net.minecraft.resources.ResourceKey;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;

public class CelestialOrbit extends Orbit<CelestialBody>
{
	private final ResourceKey<CelestialBody> parent;
	private final double altitude;
	private final double period;

	private final CelestialBody object;

	public CelestialOrbit(ResourceKey<CelestialBody> parent, double altitude, double period, CelestialBody object)
	{
		this.parent = parent;
		this.altitude = altitude;
		this.period = period;

		this.object = object;
	}

	@Override
	ResourceKey<CelestialBody> getParent()
	{
		return parent;
	}

	@Override
	CelestialBody getOrbitingObject()
	{
		return object;
	}

	@Override
	double getOrbitalPeriod()
	{
		return period;
	}

	@Override
	double getOrbitalAltitude()
	{
		return altitude;
	}
}
