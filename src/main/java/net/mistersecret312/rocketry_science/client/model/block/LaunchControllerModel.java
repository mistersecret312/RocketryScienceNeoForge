package net.mistersecret312.rocketry_science.client.model.block;

import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.LaunchControllerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class LaunchControllerModel extends GeoModel<LaunchControllerBlockEntity>
{

	@Override
	public ResourceLocation getModelResource(LaunchControllerBlockEntity animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "geo/block/launch_controller.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(LaunchControllerBlockEntity animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/block/launch_controller.png");
	}

	@Override
	public ResourceLocation getAnimationResource(LaunchControllerBlockEntity animatable)
	{
		return null;
	}
}
