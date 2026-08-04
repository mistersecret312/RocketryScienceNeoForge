package net.mistersecret312.rocketry_science.vessel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.util.EnvironmentUtil;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.mistersecret312.rocketry_science.vessel.block_data.RocketEngineData;

import java.util.LinkedHashSet;
import java.util.Map;

public abstract class VesselData
{
	public abstract Level level();
	public abstract boolean isInUI();
	public abstract boolean isInSpace();

	public abstract void tick(Level level);

	public abstract LinkedHashSet<Stage> getStages();
	public abstract void addStage(Stage stage);
	public abstract void removeStage(Stage stage);

	public abstract void setState(VesselState rocketState);

	public Stage getCurrentStage()
	{
		Stage stage = getStages().getFirst();
		for (Stage stageO : getStages())
		{
			stage = stageO;
			break;
		}
		return stage;
	}

	public double getMassKilogram()
	{
		double mass = 0;
		for(Stage stage : getStages())
			mass += stage.getTotalMass();
		return mass;
	}

	public double getMassDryKilogram()
	{
		double mass = 0;
		for(Stage stage : getStages())
			mass += stage.getTotalDryMass();
		return mass;
	}

	public boolean hasFuel()
	{
		Stage local = getCurrentStage();
		if(local == null)
			return false;

		for(Map.Entry<BlockPos, BlockData> entry : local.blocks.entrySet())
		{
			if(entry.getValue() instanceof RocketEngineData engineData)
			{
				if(engineData.hasFuel())
					return true;
			}
		}

		return false;
	}

	public void setEngineThrust(double thrust)
	{
		for(Stage stage : getStages())
			for(Map.Entry<BlockPos, BlockData > entry :stage.blocks.entrySet())
			{
				if(entry.getValue() instanceof RocketEngineData engine)
				{
					engine.thrustPercentage = Math.min(1.0, thrust);
				}
			}
	}

	public double getHoverThrust()
	{
		return (getMassKilogram()*getLocalGravityMS2())/(getMaxThrustKiloNewtons()*1000);
	}

	public void toggleEngines(boolean state)
	{
		Stage stage = getCurrentStage();
		if(stage == null) return;

		for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
		{
			if(entry.getValue() instanceof RocketEngineData engine)
			{
				engine.enabled = state;
			}
		}
	}

	public double getMaxTWR()
	{
		double gravity = getLocalGravityMS2();

		double thrust = getMaxThrustKiloNewtons()*1000;
		double mass = getMassKilogram()*gravity;
		return thrust / mass;
	}

	public double getMaxThrustKiloNewtons()
	{
		double thrustkN = 0;
		Stage stage = getCurrentStage();
		if(stage == null) return 0;

		for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
		{
			if(entry.getValue() instanceof RocketEngineData engine)
			{
				thrustkN += engine.getThrustkN();
			}
		}
		return (thrustkN);
	}

	public int getAverageFuelUsage()
	{
		int fuelUse = 0;
		int amount = 0;

		Stage current = getCurrentStage();

		for(Map.Entry<BlockPos, BlockData> entry : current.blocks.entrySet())
		{
			if(entry.getValue() instanceof RocketEngineData data)
			{
				fuelUse += (int) data.calculateMaxFuelUsage();
				amount++;
			}
		}

		if(amount == 0) return 0;
		return fuelUse / amount;
	}

	public double getAverageIsp()
	{
		double Isp = 0;
		int amount = 0;

		Stage current = this.getCurrentStage();
		if(current == null) return 0;

		for(Map.Entry<BlockPos, BlockData> entry : current.blocks.entrySet())
		{
			if(entry.getValue() instanceof RocketEngineData data)
			{
				Isp += data.getIsp();
				amount++;
			}
		}
		if(amount == 0) return 0;
		return Isp / amount;
	}

	public int getEngineAmount()
	{
		int amount = 0;
		Stage stage = getCurrentStage();
		if(stage == null)
			return 0;

		for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
			if(entry.getValue() instanceof RocketEngineData)
				amount++;


		return amount;
	}

	public double getLocalGravityMS2()
	{
		double gravity = 9.8;
		CelestialBody body = OrbitUtil.getCelestialBody(level());
		if(body != null)
			gravity = EnvironmentUtil.getGravityMS2(body);

		return gravity;
	}

	public double getLocalGravity()
	{
		double gravity = 1;
		CelestialBody body = OrbitUtil.getCelestialBody(level());
		if(body != null)
			gravity = EnvironmentUtil.getGravity(body);

		return gravity;
	}
}
