package net.mistersecret312.rocketry_science.client.vessel.block_data;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.mistersecret312.rocketry_science.client.renderer.block.FuelTankRenderer;
import net.mistersecret312.rocketry_science.client.vessel.AbstractBlockDataRenderer;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.mistersecret312.rocketry_science.vessel.block_data.FuelTankData;

public class FuelTankDataRenderer extends AbstractBlockDataRenderer<FuelTankData>
{
	@Override
	public void render(FuelTankData data, Level level, BlockPos.MutableBlockPos mutablePos, float partialTick, PoseStack poseStack,
					   MultiBufferSource buffer, int packedLight)
	{
		mutablePos.move(data.pos);

		int blockLight = level.getBrightness(LightLayer.BLOCK, mutablePos);
		int skyLight = level.getBrightness(LightLayer.SKY, mutablePos);

		packedLight = LightTexture.pack(blockLight, skyLight);

		switch (data.width)
		{
			case 1:
			{
				FuelTankRenderer.renderSingularWidth(data.height, level, mutablePos, 0f, poseStack, buffer, OverlayTexture.NO_OVERLAY,
						packedLight);
				break;
			}
			case 2:
			{
				FuelTankRenderer.renderDoubleWidth(data.height, level, mutablePos, 0f, poseStack, buffer, OverlayTexture.NO_OVERLAY, LevelRenderer.getLightColor(level, mutablePos));
				break;
			}
			case 3:
			{
				FuelTankRenderer.renderTripleWidth(data.height, level, mutablePos, 0f, poseStack, buffer, OverlayTexture.NO_OVERLAY, LevelRenderer.getLightColor(level, mutablePos));
				break;
			}
		}
		mutablePos.move(-data.pos.getX(), -data.pos.getY(), -data.pos.getZ());
	}
}
