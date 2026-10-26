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

public class Galaxy
{
	public static final ResourceLocation GALAXY_LOCATION = ResourceLocation.fromNamespaceAndPath(
			RocketryScience.MODID, "galaxy");
	public static final ResourceKey<Registry<Galaxy>> REGISTRY_KEY = ResourceKey.createRegistryKey(GALAXY_LOCATION);

	private static final Codec<Vector2d> POSITION = Codec.DOUBLE.listOf().comapFlatMap(f -> Util.fixedSize(f, 2).map(vec -> new Vector2d(vec.get(0), vec.get(1))), element -> List.of(element.get(0), element.get(1))).stable();

	public static final Codec<Galaxy> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			ResourceLocation.CODEC.fieldOf("icon").forGetter(Galaxy::getIcon),
			Codec.STRING.fieldOf("name").forGetter(Galaxy::getName),
			POSITION.fieldOf("position").forGetter(Galaxy::getPosition),
			Codec.DOUBLE.fieldOf("size").forGetter(Galaxy::getSize)
	).apply(inst, Galaxy::new));

	private final ResourceLocation icon;
	private final String name;
	private final Vector2d position;
	private final double size;

	public Galaxy(ResourceLocation icon, String name,
				  Vector2d position, double size)
	{
		this.icon = icon;
		this.name = name;
		this.position = position;
		this.size = size;
	}

	public ResourceLocation getIcon()
	{
		return icon;
	}

	public String getName()
	{
		return name;
	}

	public Vector2d getPosition()
	{
		return position;
	}

	public double getSize()
	{
		return size;
	}
}
