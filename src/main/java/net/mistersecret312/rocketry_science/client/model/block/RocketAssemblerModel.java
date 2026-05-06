package net.mistersecret312.rocketry_science.client.model.block;

import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class RocketAssemblerModel extends GeoModel<RocketAssemblerBlockEntity>
{

	@Override
	public ResourceLocation getModelResource(RocketAssemblerBlockEntity animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "geo/block/rocket_assembler.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(RocketAssemblerBlockEntity animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/block/rocket_assembler.png");
	}

	@Override
	public ResourceLocation getAnimationResource(RocketAssemblerBlockEntity animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "animations/block/rocket_assembler.animation.json");
	}
}
