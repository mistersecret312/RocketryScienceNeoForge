package net.mistersecret312.rocketry_science.data.orbiting_objects;

import net.mistersecret312.rocketry_science.data.orbits.ArtificialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.Orbit;

public class SpaceCraft implements IOrbitObject<Orbit<SpaceCraft>>
{
	private Orbit<SpaceCraft> orbit;

	@Override
	public Orbit<SpaceCraft> getOrbit()
	{
		return orbit;
	}

	@Override
	public void setOrbit(Orbit<SpaceCraft> orbit)
	{
		this.orbit = orbit;
	}

	@Override
	public String getName()
	{
		return "";
	}
}
