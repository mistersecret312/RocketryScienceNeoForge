package net.mistersecret312.rocketry_science.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.rocketry_science.block_entities.SolarPanelBlockEntity;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.mixin.StellarViewResourcepackReloadListenerMixin;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.povstalec.stellarview.api.client.StellarViewRendering;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.client.render.space_objects.SpaceObjectRenderer;
import net.povstalec.stellarview.client.render.space_objects.resourcepack.StarRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.*;

import java.util.HashMap;

public class SolarPanelRenderer implements BlockEntityRenderer<SolarPanelBlockEntity>
{
	public SolarPanelRenderer(BlockEntityRendererProvider.Context context)
	{

	}

	@Override
	public void render(SolarPanelBlockEntity panel, float partialTick, PoseStack pose,
					   MultiBufferSource buffer, int packedLight, int packedOverlay)
	{
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		Vec3 pos = panel.getBlockPos().getCenter();
		if(level == null)
			return;

		pose.pushPose();
		pose.translate(0.5, 2, 0.5);
		pose.mulPose(Axis.XP.rotationDegrees(90));

		if(StellarViewRendering.isViewCenterPresent(level.dimension().location()))
		{
			ViewCenter center = StellarViewRendering.getViewCenter(level.dimension().location());
			if(center != null)
			{
				HashMap<ResourceLocation, SpaceObjectRenderer<?>> map = StellarViewResourcepackReloadListenerMixin.getSpaceObjects();

				if(center.getViewCenterKey().isPresent() && map != null)
				{
					SpaceObjectRenderer<?> renderer = map.get(center.getViewCenterKey().get().location());
					if(renderer != null && renderer.renderedObject() != null)
					{
						SpaceObject centerObject = renderer.renderedObject();
						AxisRotation rotation = center.getAxisRotation();

						double properPeriod = center.getRotationPeriod();
						double dayTicks = center.dayTicks();

						double period = 0;
						CelestialBody body = OrbitUtil.getCelestialBody(level);
						if(body != null && body.getSurfaceData().isPresent())
							period = (long) body.getSurfaceData().get().dayLength()*60*20;

						pose.mulPose(Axis.XP.rotation((float) rotation.xAxis()));
						pose.mulPose(Axis.YP.rotation((float) rotation.yAxis()));

						ResourceLocation location = ResourceLocation.fromNamespaceAndPath("stellarview", "star/milky_way/sol");
						StarRenderer<?> star = (StarRenderer<?>) map.get(location);

						SphericalCoords coords = new SphericalCoords();
						star.spaceCoords().skyPosition(coords, level, center, 1, partialTick, true);

						float phiDegrees = (float) Math.toDegrees(coords.phi);
						float thetaDegrees = (float) Math.toDegrees(coords.theta);
						float zDegrees = (float) Math.toDegrees(center.getZRotation(level, mc.gameRenderer.getMainCamera(), partialTick));

						pose.mulPose(Axis.XN.rotationDegrees(-90));
						pose.mulPose(Axis.YP.rotationDegrees(phiDegrees));

						float timeAngle = (float) Math.toDegrees(2f * Math.PI * ((dayTicks) / period));
						if(properPeriod > 0)
						{
							pose.mulPose(Axis.XP.rotationDegrees(timeAngle + 90));
							pose.mulPose(Axis.YN.rotationDegrees(zDegrees));
						}
						else
						{
							pose.mulPose(Axis.YP.rotationDegrees(timeAngle));
							pose.mulPose(Axis.XN.rotationDegrees(zDegrees));
						}
					}
				}
			}
		}

		VertexConsumer consumer = buffer.getBuffer(RenderType.endGateway());
		consumer.addVertex(pose.last().pose(), -0.5f, -0.5f, 0);
		consumer.addVertex(pose.last().pose(), 0.5f, -0.5f, 0);
		consumer.addVertex(pose.last().pose(), 0.5f, 0.5f, 0);
		consumer.addVertex(pose.last().pose(), -0.5f, 0.5f, 0);

		VertexConsumer consumerB = buffer.getBuffer(RenderType.endPortal());
		consumerB.addVertex(pose.last().pose(), -0.5f, 0.5f, 0);
		consumerB.addVertex(pose.last().pose(), 0.5f, 0.5f, 0);
		consumerB.addVertex(pose.last().pose(), 0.5f, -0.5f, 0);
		consumerB.addVertex(pose.last().pose(), -0.5f, -0.5f, 0);

		pose.mulPose(Axis.XN.rotationDegrees(90));
		pose.translate(0, -1, 0);

		consumerB.addVertex(pose.last().pose(), -0.5f, 0.5f, 0);
		consumerB.addVertex(pose.last().pose(), 0.5f, 0.5f, 0);
		consumerB.addVertex(pose.last().pose(), 0.5f, -0.5f, 0);
		consumerB.addVertex(pose.last().pose(), -0.5f, -0.5f, 0);

		consumerB.addVertex(pose.last().pose(), -0.5f, -0.5f, 0);
		consumerB.addVertex(pose.last().pose(), 0.5f, -0.5f, 0);
		consumerB.addVertex(pose.last().pose(), 0.5f, 0.5f, 0);
		consumerB.addVertex(pose.last().pose(), -0.5f, 0.5f, 0);


		pose.popPose();
	}
}
