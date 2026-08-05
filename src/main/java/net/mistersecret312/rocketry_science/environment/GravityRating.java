package net.mistersecret312.rocketry_science.environment;

public enum GravityRating
{
	EXTREME(10d, Double.MAX_VALUE),
	VERY_HIGH(3d, 10d),
	HIGH(1.5d, 3d),
	NORMAL(0.5d, 1.5d),
	LOW(0.1d, 0.5d),
	VERY_LOW(0d, 0.1d);

	final double min, max;
	GravityRating(double min, double max)
	{
		this.min = min;
		this.max = max;
	}

	public static GravityRating getRating(double value)
	{
		for(GravityRating rating : GravityRating.values())
		{
			if(value > rating.min && value <= rating.max)
				return rating;
		}

		return NORMAL;
	}
}
