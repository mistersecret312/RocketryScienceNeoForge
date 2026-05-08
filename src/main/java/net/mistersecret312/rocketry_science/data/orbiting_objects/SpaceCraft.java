package net.mistersecret312.rocketry_science.data.orbiting_objects;

import net.mistersecret312.rocketry_science.data.orbits.Orbit;

public class SpaceCraft implements IOrbitObject<Orbit<SpaceCraft>>
{
	@Override
	public Orbit<SpaceCraft> getOrbit()
	{
		return null;
	}

	@Override
	public void setOrbit(Orbit<SpaceCraft> orbit)
	{

	}

	@Override
	public String getName()
	{
		return "";
	}
}
