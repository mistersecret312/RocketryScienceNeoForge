package net.mistersecret312.rocketry_science.data.orbits;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.mistersecret312.rocketry_science.data.orbiting_objects.IOrbitObject;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import org.joml.Vector2d;

public abstract class Orbit<T extends IOrbitObject<?>>
{
	public Vector2d getPosition(long tick, RegistryAccess registryAccess)
	{
		if(getOrbitalAltitude() == 0)
			return new Vector2d();

		double angle = getAngle(tick);
		double radians = Math.toRadians(angle);

		double x = getOrbitalAltitude() * Math.cos(radians);
		double y = getOrbitalAltitude() * Math.sin(radians);

		return new Vector2d(x, y);
	}

	public double getAngle(long tick)
	{
		if(getOrbitalPeriod() == 0)
			return 0;
		double period = getOrbitalPeriod() * 20 * 20 * 60;
		double velocity = (360D / period);

		double epoch = getOrbitingObject().getEpoch();
		double angle = ((velocity * tick) + epoch) % 360;
		if(isRetrograde())
			angle = 360D - angle;
		return angle;
	}

	public Vector2d getVisualPosition(long tick)
	{
		double altitude = getOrbitalAltitude();
		if(altitude == 0)
			return new Vector2d();
		if(getOrbitingObject() instanceof CelestialBody body)
			if(body.getOrbitData().isPresent())
				altitude = body.getOrbitData().get().getMapAltitude();

		double angle = getAngle(tick);
		double radians = Math.toRadians(angle);

		double cos = Math.cos(radians);
		double sin = Math.sin(radians);

		double squareScale = 1.0 / Math.max(Math.abs(cos), Math.abs(sin));

		double x = altitude * cos * squareScale;
		double y = altitude * sin * squareScale;

		return new Vector2d(x, y);
	}

	public void tick(RegistryAccess registryAccess)
	{}

	public abstract CompoundTag save(RegistryAccess registryAccess);
	public abstract Orbit<T> load(CompoundTag tag, RegistryAccess registryAccess);

	public abstract double getOrbitalPeriod();
	public abstract double getOrbitalAltitude();

	public boolean isRetrograde()
	{
		return false;
	}

	public abstract ResourceKey<CelestialBody> getParent(RegistryAccess registryAccess);
	public abstract T getOrbitingObject();
}
