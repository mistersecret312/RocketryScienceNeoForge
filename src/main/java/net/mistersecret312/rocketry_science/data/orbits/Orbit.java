package net.mistersecret312.rocketry_science.data.orbits;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec2;
import net.mistersecret312.rocketry_science.data.orbiting_objects.IOrbitObject;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import org.joml.Vector2d;

public abstract class Orbit<T extends IOrbitObject<?>>
{
	Vector2d getPosition(long tick)
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

	abstract double getOrbitalPeriod();
	abstract double getOrbitalAltitude();

	abstract ResourceKey<CelestialBody> getParent();
	abstract T getOrbitingObject();
	abstract void setOrbitingObject(T object);
}
