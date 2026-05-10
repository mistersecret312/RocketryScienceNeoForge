package net.mistersecret312.rocketry_science.data.orbits;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.mistersecret312.rocketry_science.data.orbiting_objects.IOrbitObject;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import org.joml.Vector2d;

public abstract class Orbit<T extends IOrbitObject<?>>
{
	Vector2d getPosition(long tick, RegistryAccess registryAccess)
	{
		double angle = getAngle(tick);
		double radians = Math.toRadians(angle);

		double x = getOrbitalAltitude() * Math.cos(radians);
		double y = getOrbitalAltitude() * Math.sin(radians);

		return new Vector2d(x, y);
	}

	double getAngle(long tick)
	{
		double period = getOrbitalPeriod() * 20 * 20 * 60;
		double velocity = (360D / period);

		double angle = velocity * tick;
		return angle % 360D;
	}

	public void tick(RegistryAccess registryAccess)
	{}

	public abstract CompoundTag save(HolderLookup.Provider registryAccess);
	public abstract Orbit<T> load(CompoundTag tag, HolderLookup.Provider registryAccess);

	public abstract double getOrbitalPeriod();
	public abstract double getOrbitalAltitude();

	public abstract ResourceKey<CelestialBody> getParent();
	public abstract T getOrbitingObject();
}
