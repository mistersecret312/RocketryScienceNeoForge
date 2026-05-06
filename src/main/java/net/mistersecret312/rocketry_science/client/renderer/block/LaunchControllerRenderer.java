package net.mistersecret312.rocketry_science.client.renderer.block;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.mistersecret312.rocketry_science.block_entities.LaunchControllerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.client.model.block.LaunchControllerModel;
import net.mistersecret312.rocketry_science.client.model.block.RocketAssemblerModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class LaunchControllerRenderer extends GeoBlockRenderer<LaunchControllerBlockEntity>
{
	public LaunchControllerRenderer(BlockEntityRendererProvider.Context context)
	{
		super(new LaunchControllerModel());
		this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
	}
}
