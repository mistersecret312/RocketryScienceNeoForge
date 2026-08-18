package net.mistersecret312.rocketry_science.environment;

import net.minecraft.util.Mth;

public class EnvironmentData
{
	public double pressure;
	public double gravity;
	public double radiation;
	public TemperatureGradient temperature;

	public EnvironmentData(double pressure, double gravity, double radiation, TemperatureGradient temperature)
	{
		this.pressure = pressure;
		this.gravity = gravity;
		this.radiation = radiation;
		this.temperature = temperature;
	}

	public EnvironmentData(EnvironmentData data)
	{
		this.pressure = data.pressure;
		this.gravity = data.gravity;
		this.radiation = data.radiation;
		this.temperature = data.temperature;
	}

	public TemperatureGradient getTemperatureGradient()
	{
		return temperature;
	}

	public double getPressureSeaLevel()
	{
		return pressure;
	}

	public double getGravity()
	{
		return gravity;
	}

	public double getRadiation()
	{
		return radiation;
	}

	public static class TemperatureGradient
	{
		double minTemp;
		double maxTemp;

		public TemperatureGradient(double minTemp, double maxTemp)
		{
			this.minTemp = minTemp;
			this.maxTemp = maxTemp;
		}

		public double getMaxTemp()
		{
			return maxTemp;
		}

		public double getMinTemp()
		{
			return minTemp;
		}

		public double getTemperature(double skyLight)
		{
			return Mth.clampedLerp(getMinTemp(), getMaxTemp(), skyLight);
		}

		public double getAverageTemperature()
		{
			return Mth.clampedLerp(getMinTemp(), getMaxTemp(), 0.5d);
		}
	}
}
