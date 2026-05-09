package net.mistersecret312.rocketry_science.client.vessel.block_data;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.client.renderer.PlumeRenderer;
import net.mistersecret312.rocketry_science.client.renderer.block.SeparatorRenderer;
import net.mistersecret312.rocketry_science.client.vessel.AbstractBlockDataRenderer;
import net.mistersecret312.rocketry_science.vessel.block_data.RocketEngineData;
import net.mistersecret312.rocketry_science.vessel.block_data.SeparatorData;
import net.neoforged.neoforge.client.model.data.ModelData;

public class SeparatorDataRenderer extends AbstractBlockDataRenderer<SeparatorData>
{
	@Override
	public void render(SeparatorData data, Level level, BlockPos.MutableBlockPos mutablePos, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
					   int packedLight)
	{
		mutablePos.move(data.pos);
		switch (data.width)
		{
			case 1:
			{
				SeparatorRenderer.renderSingularWidth(level, mutablePos, poseStack, buffer, OverlayTexture.NO_OVERLAY, LevelRenderer.getLightColor(level, mutablePos), data.extended);
				return;
			}
			case 2:
			{
				SeparatorRenderer.renderDoubleWidth(level, mutablePos, poseStack, buffer, OverlayTexture.NO_OVERLAY, LevelRenderer.getLightColor(level, mutablePos), data.extended);
				return;
			}
			case 3:
			{
				SeparatorRenderer.renderTripleWidth(level, mutablePos, poseStack, buffer, OverlayTexture.NO_OVERLAY, LevelRenderer.getLightColor(level, mutablePos), data.extended);
				return;
			}
		}
		mutablePos.move(-data.pos.getX(), -data.pos.getY(), -data.pos.getZ());
	}
}
