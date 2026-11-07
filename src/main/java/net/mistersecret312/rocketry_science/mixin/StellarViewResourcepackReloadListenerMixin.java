package net.mistersecret312.rocketry_science.mixin;

import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.resourcepack.ResourcepackReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;

@Mixin(ResourcepackReloadListener.class)
public interface StellarViewResourcepackReloadListenerMixin
{

	@Accessor(value = "spaceObjects", remap = false)
	static HashMap<ResourceLocation, SpaceObjectRenderer<?>> getSpaceObjects()
	{
		return null;
	}
}
