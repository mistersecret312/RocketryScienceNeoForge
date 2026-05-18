package net.mistersecret312.rocketry_science.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.environment.EnvironmentData;
import net.mistersecret312.rocketry_science.environment.modifiers.ModifierConfig;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.VesselData;

public class EnvironmentUtil
{
	public static final EnvironmentData EARTH = new EnvironmentData(1, 1, 0, new EnvironmentData.TemperatureGradient(260, 300));
	public static final EnvironmentData LUNA = new EnvironmentData(0, 0.165, 0.06, new EnvironmentData.TemperatureGradient(140, 400));

	public static EnvironmentData getEnvironment(Entity entity)
	{
		Level level = entity.level();
		//TODO - Add habitat EnvironmentDatas to be accounted and override celestial body environment;
		return getEnvironment(level);
	}

	public static EnvironmentData getEnvironment(Level level)
	{
		CelestialBody body = OrbitUtil.getCelestialBody(level);
		return getEnvironment(body);
	}

	public static EnvironmentData getEnvironment(CelestialBody body)
	{
		if(body == null)
			return EARTH;
		if(body.getEnvironment() != null)
			return body.getEnvironment();

		return body.hasAtmosphere() ? EARTH : LUNA;
	}

	public static double getTemperatureKelvin(Level level)
	{
		return getEnvironment(level).getTemperatureGradient().getTemperature(11-level.getSkyDarken());
	}

	public static double getTemperatureCelsius(Level level)
	{
		return getTemperatureKelvin(level)-273d;
	}

	public static double getTemperatureFahrenheit(Level level)
	{
		return 1.8d*getTemperatureCelsius(level)+32d;
	}
	
	public static double getSeaLevelPressure(Level level)
	{
		return getEnvironment(level).getPressureSeaLevel();
	}

	public static double getPressure(VesselData vessel)
	{
		Level level = vessel.level();
		double height = level.getSeaLevel();
		if(vessel instanceof SpaceCraft)
			height = OrbitUtil.getSpaceHeight(level)*2;
		else if(vessel instanceof Rocket rocket)
			height = rocket.getRocketEntity().position().y;

		return getPressure(level, height);
	}

	public static double getPressure(Level level, double yLevel)
	{
		double spaceY = OrbitUtil.getSpaceHeight(level);
		double seaLevelY = level.getSeaLevel();
		double seaLevelPressure = getSeaLevelPressure(level);

		if(yLevel >= spaceY || seaLevelPressure == 0)
			return 0d;

		double vacuumThreshold = 0.001;
		double scaleHeight = (seaLevelY - spaceY) / Math.log(vacuumThreshold / seaLevelPressure);

		double pressure =  seaLevelPressure * Math.exp(-(yLevel - seaLevelY) / scaleHeight);
		if(level.dimension().equals(Level.OVERWORLD) && pressure >= 1.0d)
			return 1.0d;

		return pressure;
	}

	public static double getGravity(CelestialBody body)
	{
		return getEnvironment(body).getGravity();
	}
	public static double getGravityMS2(CelestialBody body)
	{
		return getEnvironment(body).getGravity()*9.8d;
	}
	public static double getGravitationalParameter(CelestialBody body)
	{
		return getGravityMS2(body)*body.getRadius()*body.getRadius();
	}

	public static void modifiersAffect(Entity entity)
	{
		CelestialBody body = OrbitUtil.getCelestialBody(entity.level());
		if(body != null)
		{
			for(ModifierConfig modifier : body.getModifiers())
				modifier.tick(entity);
		}
	}

	public static void modifiersAffect(Level level)
	{
		CelestialBody body = OrbitUtil.getCelestialBody(level);
		if(body != null)
		{
			for(ModifierConfig modifier : body.getModifiers())
				modifier.tick(level);
		}
	}
}
