package net.mistersecret312.rocketry_science.vessel;

import net.minecraft.world.level.Level;

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
}
