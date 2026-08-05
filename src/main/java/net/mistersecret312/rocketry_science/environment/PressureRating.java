package net.mistersecret312.rocketry_science.environment;

public enum PressureRating
{
	EXTREME(50d, Double.MAX_VALUE),
	VERY_HIGH(10d, 50d),
	HIGH(1.2d, 10d),
	NORMAL(0.8d, 1.2d),
	LOW(0.001d, 0.8d),
	VACUUM(0d, 0.001d);

	final double min, max;
	PressureRating(double min, double max)
	{
		this.min = min;
		this.max = max;
	}

	public double getMax()
	{
		return max;
	}

	public double getMin()
	{
		return min;
	}

	public static PressureRating getRating(double value)
	{
		for(PressureRating rating : PressureRating.values())
		{
			if(value >= rating.min && value < rating.max)
				return rating;
		}

		return NORMAL;
	}
}
