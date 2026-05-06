package net.mistersecret312.rocketry_science.client.renderer.item;

import net.mistersecret312.rocketry_science.client.model.item.RocketAssemblerItemModel;
import net.mistersecret312.rocketry_science.items.RocketAssemblerBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class RocketAssemblerItemRenderer extends GeoItemRenderer<RocketAssemblerBlockItem>
{
	public RocketAssemblerItemRenderer()
	{
		super(new RocketAssemblerItemModel());
		this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
	}
}
