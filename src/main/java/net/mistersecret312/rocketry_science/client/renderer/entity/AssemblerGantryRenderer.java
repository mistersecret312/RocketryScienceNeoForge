package net.mistersecret312.rocketry_science.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.client.model.assembler_gantry.AssemblerGantryBaseModel;
import net.mistersecret312.rocketry_science.client.model.assembler_gantry.AssemblerGantryPillarModel;
import net.mistersecret312.rocketry_science.client.model.assembler_gantry.AssemblerGantryRingCornerModel;
import net.mistersecret312.rocketry_science.client.model.assembler_gantry.AssemblerGantryRingSideModel;
import net.mistersecret312.rocketry_science.entities.RocketAssemblerGantryEntity;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import org.joml.Vector4d;

public class AssemblerGantryRenderer extends EntityRenderer<RocketAssemblerGantryEntity>
{
	public AssemblerGantryBaseModel base = RocketryScience.ClientModEvents.gantryBase;
	public AssemblerGantryRingSideModel side = RocketryScience.ClientModEvents.gantryRingSide;
	public AssemblerGantryPillarModel pillar = RocketryScience.ClientModEvents.gantryPillar;
	public AssemblerGantryRingCornerModel corner = RocketryScience.ClientModEvents.gantryRingCorner;

	public static final ResourceLocation baseTexture = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
			"textures/block/assembler_gantry/base.png");
	public static final ResourceLocation sideTexture = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
			"textures/block/assembler_gantry/ring_side.png");
	public static final ResourceLocation pillarTexture = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
			"textures/block/assembler_gantry/pillar.png");
	public static final ResourceLocation cornerTexture = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
			"textures/block/assembler_gantry/ring_corner.png");


	public AssemblerGantryRenderer(EntityRendererProvider.Context context)
	{
		super(context);
	}

	@Override
	public void render(RocketAssemblerGantryEntity entity, float yaw, float partial, PoseStack pose,
					   MultiBufferSource buffer, int light)
	{
		Level level = entity.level();

		pose.pushPose();
		pose.translate(0, 1.5, 0);

		AABB aabb = entity.makeBoundingBox();
		double height = aabb.getYsize();
		double xWidth = aabb.getXsize()-2;
		double zWidth = aabb.getZsize()-2;
		Vector4d[] corners = new Vector4d[] {
				new Vector4d(aabb.minX, aabb.minY, aabb.minZ, 180),
				new Vector4d(aabb.maxX, aabb.minY, aabb.minZ, 90),
				new Vector4d(aabb.maxX, aabb.minY, aabb.maxZ, 0),
				new Vector4d(aabb.minX, aabb.minY, aabb.maxZ, 270)
		};


		int pillarHeight = (int) (height/0.4375f);
		float progress = entity.getProgress();
		double currentHeight = progress*(pillarHeight+1);

		for(Vector4d pos : corners)
		{
			double baseY = pos.y + 1.5;
			BlockPos basePos = BlockPos.containing(pos.x, baseY, pos.z);
			int baseLight = LevelRenderer.getLightColor(level, basePos);

			pose.pushPose();
			pose.translate(pos.x-entity.position().x, pos.y-entity.position().y, pos.z-entity.position().z);
			pose.mulPose(Axis.YP.rotationDegrees((float) pos.w));
			base.renderToBuffer(pose, buffer.getBuffer(RenderType.entityTranslucent(baseTexture)),
					baseLight, OverlayTexture.NO_OVERLAY);
			pose.popPose();

			pose.pushPose();
			pose.translate(pos.x-entity.position().x, pos.y-entity.position().y, pos.z-entity.position().z);
			pose.mulPose(Axis.YP.rotationDegrees((float) pos.w));
			pose.translate(0.25, 0.75, 0.25);

			int maxIndex = (int) currentHeight;
			for(int i = 0; i <= maxIndex; i++)
			{
				float yOffset;
				if (i == maxIndex)
				{
					float fraction = (float) (currentHeight - maxIndex);
					yOffset = (i - 1 + fraction) * 0.4375f;
				}
				else yOffset = i * 0.4375f;

				double pillarY = baseY + 0.75 + yOffset;
				BlockPos pillarPos = BlockPos.containing(pos.x, pillarY, pos.z);
				int pillarLight = LevelRenderer.getLightColor(level, pillarPos);

				pose.pushPose();
				pose.translate(0, yOffset, 0);
				pillar.renderToBuffer(pose, buffer.getBuffer(RenderType.entityTranslucent(pillarTexture)),
						pillarLight, OverlayTexture.NO_OVERLAY);
				pose.popPose();
			}
			pose.popPose();
		}
		float[] cornerRot = new float[]{0, 270, 180, 90};
		Vec3[] cornerOffset = new Vec3[]
									  {
											  new Vec3(0.25, 0, 0.25),
											  new Vec3(0.25, 0, 0.75),
											  new Vec3(0.75, 0, 0.75),
											  new Vec3(0.75, 0, 0.25)
									  };
		Direction.Axis[] cornerAxis = new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z, Direction.Axis.X, Direction.Axis.Z};
		int i = 0;
		for(Vector4d pos : corners)
		{
			double topY = pos.y + 2.25 + (currentHeight * 0.4375f);
			BlockPos cornerPos = BlockPos.containing(pos.x, topY, pos.z);
			int cornerLight = LevelRenderer.getLightColor(level, cornerPos);

			pose.pushPose();
			pose.translate(pos.x-entity.position().x, pos.y-entity.position().y+0.75, pos.z-entity.position().z);
			pose.translate(0.25, 0, 0.25);
			pose.mulPose(Axis.YP.rotationDegrees(cornerRot[i]));
			pose.translate(cornerOffset[i].x, currentHeight*0.4375f, cornerOffset[i].z);

			corner.renderToBuffer(pose, buffer.getBuffer(RenderType.entityTranslucent(cornerTexture)),
					cornerLight, OverlayTexture.NO_OVERLAY);

			int size = (int) (cornerAxis[i].equals(Direction.Axis.X) ? xWidth : zWidth);
			for(int j = 1; j < size+1; j++)
			{
				int dx = 0, dz = 0;
				if (i == 0)
					dx = j;
				else if (i == 1)
					dz = j;
				else if (i == 2)
					dx = -j;
				else if (i == 3)
					dz = -j;

				BlockPos sidePos = BlockPos.containing(pos.x + dx, topY, pos.z + dz);
				int sideLight = LevelRenderer.getLightColor(level, sidePos);

				pose.pushPose();
				pose.translate(j, 0, 0);
				side.renderToBuffer(pose, buffer.getBuffer(RenderType.entityTranslucent(sideTexture)),
						sideLight, OverlayTexture.NO_OVERLAY);
				pose.popPose();
			}

			pose.popPose();
			i++;
		}

		pose.popPose();
	}

	@Override
	public boolean shouldRender(RocketAssemblerGantryEntity entity, Frustum camera, double camX, double camY,
								double camZ)
	{
		return camera.isVisible(entity.makeBoundingBox());
	}

	@Override
	public ResourceLocation getTextureLocation(RocketAssemblerGantryEntity entity)
	{
		return ResourceLocation.fromNamespaceAndPath("minecraft", "block/air");
	}
}
