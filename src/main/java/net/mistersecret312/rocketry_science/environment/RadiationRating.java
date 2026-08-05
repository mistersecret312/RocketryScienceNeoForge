package net.mistersecret312.rocketry_science.environment;

public enum RadiationRating
{
	EXTREME(1000d, Double.MAX_VALUE),
	VERY_HIGH(100d, 1000d),
	HIGH(10d, 100d),
	NORMAL(0.5d, 10d),
	LOW(0.01d, 0.5d),
	NONE(0d, 0.01d);

	final double min, max;
	RadiationRating(double min, double max)
	{
		this.min = min;
		this.max = max;
	}

	public static RadiationRating getRating(double value)
	{
		for(RadiationRating rating : RadiationRating.values())
		{
			if(value >= rating.min && value < rating.max)
				return rating;
		}

		return NORMAL;
	}
}
