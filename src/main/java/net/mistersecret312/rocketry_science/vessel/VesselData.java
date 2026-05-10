package net.mistersecret312.rocketry_science.vessel;

import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.util.OrbitUtil;

import java.util.LinkedHashSet;

public abstract class VesselData
{
	public abstract Level level();
	public abstract boolean isInSpace();

	public abstract void tick(Level level);

	public abstract LinkedHashSet<Stage> getStages();
	public abstract void addStage(Stage stage);
	public abstract void removeStage(Stage stage);

	public void setState(VesselState rocketState)
	{

	}

	public double getLocalGravityMS2()
	{
		double gravity = 9.8;
		CelestialBody body = OrbitUtil.getCelestialBody(level());
		if(body != null)
			gravity = body.getGravityMS2();

		return gravity;
	}

	public double getLocalGravity()
	{
		double gravity = 1;
		CelestialBody body = OrbitUtil.getCelestialBody(level());
		if(body != null)
			gravity = body.getGravity();

		return gravity;
	}
}
