package net.mistersecret312.rocketry_science.data.orbiting_objects;

import net.mistersecret312.rocketry_science.data.orbits.Orbit;

public interface IOrbitObject<O extends Orbit<?>>
{
	O getOrbit();
	void setOrbit(O orbit);

	String getName();
}
