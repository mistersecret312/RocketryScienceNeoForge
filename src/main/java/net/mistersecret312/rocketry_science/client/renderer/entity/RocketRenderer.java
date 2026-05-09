package net.mistersecret312.rocketry_science.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.client.vessel.BlockDataRendererRegistry;
import net.mistersecret312.rocketry_science.client.vessel.IBlockDataRenderer;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;

import java.util.Iterator;
import java.util.Map;

public class RocketRenderer extends EntityRenderer<RocketEntity>
{
    BlockRenderDispatcher dispatcher;
    ModelBlockRenderer blockRenderer;
    public RocketRenderer(EntityRendererProvider.Context pContext)
    {
        super(pContext);
        this.dispatcher = pContext.getBlockRenderDispatcher();
        this.blockRenderer = pContext.getBlockRenderDispatcher().getModelRenderer();
    }

    @Override
    public void render(RocketEntity rocket, float yaw, float partial, PoseStack pose,
                       MultiBufferSource buffer, int light)
    {

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.ZP.rotationDegrees(rocket.getViewXRot(partial)));
        pose.translate(-0.5f, 0f, -0.5f);
        Iterator<Stage> stageIterator = rocket.getRocket().stages.iterator();
        while(stageIterator.hasNext())
        {
            Stage stage = stageIterator.next();
            BlockPos.MutableBlockPos mutablePos = rocket.blockPosition().mutable().move(0, 0, 0);
            for(Map.Entry<BlockPos, BlockData> data : stage.blocks.entrySet())
            {
                BlockData blockData = data.getValue();
                BlockPos pos = data.getKey();
                pose.translate(pos.getX(), pos.getY(), pos.getZ());
                IBlockDataRenderer<BlockData> renderer = (IBlockDataRenderer<BlockData>) BlockDataRendererRegistry.getRenderer(data.getValue().getType());
                renderer.render(blockData, rocket.level(), mutablePos, partial, pose, buffer, light);
                pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
            }
        }
        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity rocketEntity)
    {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "block/air");
    }
}
