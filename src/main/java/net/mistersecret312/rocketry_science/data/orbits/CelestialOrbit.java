package net.mistersecret312.rocketry_science.data.orbits;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;

public class CelestialOrbit extends Orbit<CelestialBody>
{
	private final ResourceKey<CelestialBody> parent;
	private final double altitude;
	private final double period;
	private final boolean isRetrograde;

	private final CelestialBody object;

	public CelestialOrbit(ResourceKey<CelestialBody> parent,
						  double altitude, double period, boolean isRetrograde,
						  CelestialBody object)
	{
		this.parent = parent;
		this.altitude = altitude;
		this.period = period;
		this.isRetrograde = isRetrograde;

		this.object = object;
	}

	@Override
	public ResourceKey<CelestialBody> getParent(RegistryAccess registryAccess)
	{
		return parent;
	}

	@Override
	public CelestialBody getOrbitingObject()
	{
		return object;
	}

	@Override
	public CompoundTag save(RegistryAccess registryAccess)
	{
		return new CompoundTag();
	}

	@Override
	public Orbit<CelestialBody> load(CompoundTag tag, RegistryAccess registryAccess)
	{
		return null;
	}

	@Override
	public double getOrbitalPeriod()
	{
		return period;
	}

	@Override
	public double getOrbitalAltitude()
	{
		return altitude;
	}

	@Override
	public boolean isRetrograde()
	{
		return isRetrograde;
	}
}
