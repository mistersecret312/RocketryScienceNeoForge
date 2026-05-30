package net.mistersecret312.rocketry_science.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import org.joml.Vector2d;

import java.util.List;

public class SolarSystem
{
	public static final ResourceLocation CELESTIAL_SYSTEM_LOCATION = ResourceLocation.fromNamespaceAndPath(
			RocketryScience.MODID, "solar_system");
	public static final ResourceKey<Registry<SolarSystem>> REGISTRY_KEY = ResourceKey.createRegistryKey(CELESTIAL_SYSTEM_LOCATION);

	private static final Codec<Vector2d> POSITION = Codec.DOUBLE.listOf().comapFlatMap(f -> Util.fixedSize(f, 2).map(vec -> new Vector2d(vec.get(0), vec.get(1))), element -> List.of(element.get(0), element.get(1))).stable();

	public static final Codec<SolarSystem> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			ResourceKey.codec(CelestialBody.REGISTRY_KEY).fieldOf("star").forGetter(SolarSystem::getStar),
			ResourceLocation.CODEC.fieldOf("icon").forGetter(SolarSystem::getIcon),
			Codec.STRING.fieldOf("name").forGetter(SolarSystem::getName),
			Codec.DOUBLE.fieldOf("star_surface_temperature").forGetter(SolarSystem::getStarSurfaceTemperature),
			Codec.DOUBLE.fieldOf("escape_velocity").forGetter(SolarSystem::getEscapeVelocity),
			POSITION.fieldOf("position").forGetter(SolarSystem::getPosition)
	).apply(inst, SolarSystem::new));

	private final ResourceKey<CelestialBody> star;
	private final ResourceLocation icon;
	private final String name;
	private final double starSurfaceTemperature;
	private final double escapeVelocity;
	private final Vector2d position;

	public SolarSystem(ResourceKey<CelestialBody> star, ResourceLocation icon, String name,
					   double starSurfaceTemperature, double escapeVelocity, Vector2d position)
	{
		this.star = star;
		this.icon = icon;
		this.name = name;
		this.starSurfaceTemperature = starSurfaceTemperature;
		this.escapeVelocity = escapeVelocity;
		this.position = position;
	}

	public ResourceKey<CelestialBody> getStar()
	{
		return star;
	}

	public ResourceLocation getIcon()
	{
		return icon;
	}

	public String getName()
	{
		return name;
	}

	public double getStarSurfaceTemperature()
	{
		return starSurfaceTemperature;
	}

	public double getEscapeVelocity()
	{
		return escapeVelocity;
	}

	public Vector2d getPosition()
	{
		return position;
	}
}
