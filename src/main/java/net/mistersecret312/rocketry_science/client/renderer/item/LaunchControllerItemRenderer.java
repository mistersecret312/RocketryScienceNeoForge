package net.mistersecret312.rocketry_science.client.renderer.item;

import net.mistersecret312.rocketry_science.client.model.item.LaunchControllerItemModel;
import net.mistersecret312.rocketry_science.items.LaunchControllerBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class LaunchControllerItemRenderer extends GeoItemRenderer<LaunchControllerBlockItem>
{
	public LaunchControllerItemRenderer()
	{
		super(new LaunchControllerItemModel());
		this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
	}
}
