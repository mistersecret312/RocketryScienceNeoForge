package net.mistersecret312.rocketry_science.util;

import net.mistersecret312.rocketry_science.datapack.CelestialBody.StarData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StarUtil
{
	private static final Pattern spectralPattern = Pattern.compile("^([OBAFGKM])(\\d)?\\s*(IA\\+|IA|IB|II|III|IV|V|VI|VII)?");

	public static void fillIfMissing(StarData data)
	{
		String spectral = data.getSpectralClass().trim().toUpperCase();
		double luminosity = data.getLuminosity();
		double surfaceTemp = data.getSurfaceTemperature();

		if(luminosity == 0)
			data.luminosity = approximateLuminosity(spectral);
		if(surfaceTemp == 0)
			data.surfaceTemperature = approximateSurfaceTemperature(spectral);
	}

	private static double approximateLuminosity(String spectral)
	{
		Matcher matcher = spectralPattern.matcher(spectral);
		if(!matcher.find())
			return 1;

		char type = matcher.group(1).charAt(0);
		int subtype = (matcher.group(2) != null) ? Integer.parseInt(matcher.group(2)) : 0;
		String lumClass = (matcher.group(3) != null) ? matcher.group(3) : "V";

		return switch(lumClass)
		{
			case "IA+", "0" -> 500000;
			case "IA", "IB" -> 50000;
			case "II", "III" -> 100;
			case "IV" -> 5;
			case "VI" -> 0.2;
			case "VII" -> 0.0005;
			default -> switch(type)
			{
				case 'O' -> Math.max(10000, 800000 - (subtype * 100000));
				case 'B' -> Math.max(100, 20000 - (subtype * 2000));
				case 'A' -> Math.max(5, 25 - (subtype * 2));
				case 'F' -> Math.max(1.5, 5 - (subtype * 0.4));
				case 'G' -> Math.max(0.6, 1.5 - (subtype * 0.1));
				case 'K' -> Math.max(0.1, 0.6 - (subtype * 0.05));
				case 'M' -> Math.max(0.0001, 0.08 - (subtype * 0.008));
				default -> 1.0;
			};
		};
	}

	private static double approximateSurfaceTemperature(String spectral)
	{
		char type = spectral.charAt(0);
		int subtype = 0;
		if (spectral.length() > 1 && Character.isDigit(spectral.charAt(1)))
			subtype = Character.getNumericValue(spectral.charAt(1));

		double baseTemp = switch(type)
		{
			case 'O' -> 40000 - (subtype * 2000);
			case 'B' -> 20000 - (subtype * 1700);
			case 'A' -> 10000 - (subtype * 250);
			case 'F' -> 7500 - (subtype * 150);
			case 'G' -> 6000 - (subtype * 80);
			case 'K' -> 5200 - (subtype * 300);
			case 'M' -> 3800 - (subtype * 150);
			default -> 5777;
		};
		return Math.max(2000, baseTemp);
	}
}
