package net.mistersecret312.rocketry_science.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;

public class RocketRenderTypes extends RenderType
{
	public RocketRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize,
							 boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState,
							 Runnable pClearState)
	{
		super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
	}

	public static RenderType plume(ResourceLocation rl)
	{
		return create("plume", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
				false, true,
				RenderType.CompositeState.builder()
										 .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_ALPHA_SHADER)
										 .setTextureState(new TextureStateShard(rl, false, false))
										 .setCullState(NO_CULL)
										 .createCompositeState(true));
	}

	public static RenderType orbit(ResourceLocation texture)
	{
		return RenderType.create("orbit",
				DefaultVertexFormat.POSITION_TEX_COLOR,
				VertexFormat.Mode.QUADS,
				1536, false, false,
				RenderType.CompositeState.builder()
										 .setShaderState(new ShaderStateShard(() -> RocketryScience.ClientModEvents.orbit))
										 .setTextureState(new TextureStateShard(texture, false, false))
										 .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
										 .createCompositeState(false));
	}
}
