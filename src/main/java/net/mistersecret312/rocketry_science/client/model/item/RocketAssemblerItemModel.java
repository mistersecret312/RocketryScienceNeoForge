package net.mistersecret312.rocketry_science.client.model.item;

import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.items.RocketAssemblerBlockItem;
import software.bernie.geckolib.model.GeoModel;

public class RocketAssemblerItemModel extends GeoModel<RocketAssemblerBlockItem>
{

	@Override
	public ResourceLocation getModelResource(RocketAssemblerBlockItem animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "geo/block/rocket_assembler.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(RocketAssemblerBlockItem animatable)
	{
		return ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/block/rocket_assembler.png");
	}

	@Override
	public ResourceLocation getAnimationResource(RocketAssemblerBlockItem animatable)
	{
		return null;
	}
}
