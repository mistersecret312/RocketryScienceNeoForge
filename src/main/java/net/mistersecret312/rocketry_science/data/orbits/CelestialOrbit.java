package net.mistersecret312.rocketry_science.data.orbits;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
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
	public ResourceKey<CelestialBody> getParent()
	{
		return parent;
	}

	@Override
	public CelestialBody getOrbitingObject()
	{
		return object;
	}

	@Override
	public CompoundTag save(HolderLookup.Provider registryAccess)
	{
		return new CompoundTag();
	}

	@Override
	public Orbit<CelestialBody> load(CompoundTag tag, HolderLookup.Provider registryAccess)
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
}
