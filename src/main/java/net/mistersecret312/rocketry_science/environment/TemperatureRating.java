package net.mistersecret312.rocketry_science.environment;

public enum TemperatureRating
{
	EXTREME(1000d, Double.MAX_VALUE),
	VERY_HIGH(500d, 1000d),
	HIGH(320d, 500),
	NORMAL(250d, 320d),
	LOW(150d, 250d),
	VERY_LOW(0d, 150d);

	final double min, max;
	TemperatureRating(double min, double max)
	{
		this.min = min;
		this.max = max;
	}

	public static TemperatureRating getRating(double value)
	{
		for(TemperatureRating rating : TemperatureRating.values())
		{
			if(value > rating.min && value <= rating.max)
				return rating;
		}

		return NORMAL;
	}
}
