package net.mistersecret312.rocketry_science.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = RocketryScience.MODID, value = Dist.CLIENT)
public class ClientEvents
{
	@SubscribeEvent
	public static void highlightBlockData(RenderHighlightEvent.Entity event)
	{
		Entity entity = event.getTarget().getEntity();
		if (!(entity instanceof RocketEntity rocketEntity))
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;

		float partialTick = mc.getTimer().getGameTimeDeltaTicks();
		Vec3 cameraPos = event.getCamera().getPosition();
		double reach = mc.player.blockInteractionRange();
		Vec3 viewVector = mc.player.getViewVector(partialTick);
		Vec3 endPos = cameraPos.add(viewVector.scale(reach));

		Vec3 rocketRenderPos = rocketEntity.getPosition(partialTick);
		Rocket rocket = rocketEntity.getRocket();

		AABB closestHitShape = null;
		double minDistance = reach * reach;

		for (Stage stage : rocket.getStages()) {
			for (Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet()) {
				BlockPos relativePos = entry.getKey();

				AABB blockAABB = entry.getValue().getIndividualBoundingBox().move(
						rocketRenderPos.x + relativePos.getX(),
						rocketRenderPos.y + relativePos.getY(),
						rocketRenderPos.z + relativePos.getZ()
				);

				Optional<Vec3> hit = blockAABB.clip(cameraPos, endPos);

				if (hit.isPresent())
				{
					double distSq = cameraPos.distanceToSqr(hit.get());
					if (distSq < minDistance)
					{
						minDistance = distSq;
						closestHitShape = blockAABB;
					}
				}
			}
		}

		if (closestHitShape != null)
		{
			PoseStack poseStack = event.getPoseStack();
			poseStack.pushPose();

			poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
			VertexConsumer buffer = event.getMultiBufferSource().getBuffer(RenderType.lines());
			LevelRenderer.renderLineBox(poseStack, buffer, closestHitShape,
					0.0F, 0.0F, 0.0F, 0.4F);

			poseStack.popPose();
		}
	}
}
