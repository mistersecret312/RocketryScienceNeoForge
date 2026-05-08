package net.mistersecret312.rocketry_science.data.orbits;

import net.minecraft.resources.ResourceKey;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;

public class TravelPoint
{
	private final ConfiguredOrbit orbit;
	private final long tick;
	private final ResourceKey<CelestialBody> body;

	private TravelPoint(ConfiguredOrbit orbit, long tick, ResourceKey<CelestialBody> body)
	{
		this.orbit = orbit;
		this.tick = tick;
		this.body = body;
	}

	public ConfiguredOrbit getOrbit()
	{
		return orbit;
	}

	public long getTick()
	{
		return tick;
	}

	public ResourceKey<CelestialBody> getBody()
	{
		return body;
	}
}