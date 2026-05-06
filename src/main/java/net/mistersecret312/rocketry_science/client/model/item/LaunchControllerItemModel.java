package net.mistersecret312.rocketry_science.client.model.item;

import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.LaunchControllerBlockEntity;
import net.mistersecret312.rocketry_science.client.model.block.LaunchControllerModel;
import net.mistersecret312.rocketry_science.items.LaunchControllerBlockItem;
import software.bernie.geckolib.model.GeoModel;

public class LaunchControllerItemModel extends GeoModel<LaunchControllerBlockItem>
{

	@Override
	public ResourceLocation getModelResource(LaunchControllerBlockItem animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "geo/block/launch_controller.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(LaunchControllerBlockItem animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/block/launch_controller.png");
	}

	@Override
	public ResourceLocation getAnimationResource(LaunchControllerBlockItem animatable)
	{
		return null;
	}
}
