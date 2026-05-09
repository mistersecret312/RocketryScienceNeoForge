package net.mistersecret312.rocketry_science.client.vessel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.vessel.VesselData;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;

public interface IBlockDataRenderer<T extends BlockData>
{
    void render(T data, Level level, BlockPos.MutableBlockPos mutablePos, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight);
}