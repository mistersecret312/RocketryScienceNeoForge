package net.mistersecret312.rocketry_science.footprint;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class ChunkFootprintBuffer
{
	private final ChunkPos pos;
	private final List<Footprint> footprints = new ArrayList<>();
	private final VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
	private final AABB chunkBox;
	private boolean dirty = false;

	public static final ResourceLocation FOOTPRINT_TEXTURE = ResourceLocation.fromNamespaceAndPath("rocketry_science",
			"textures/misc/footprints.png");

	private static final RenderType FOOTPRINT_RENDER_TYPE = RenderType.create(
			"footprint_rendertype",
			DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
			VertexFormat.Mode.QUADS,
			256,
			false,
			true,
			RenderType.CompositeState.builder()
									 .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorTexLightmapShader))
									 .setTextureState(new RenderStateShard.TextureStateShard(FOOTPRINT_TEXTURE, false, false))
									 .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
									 .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
									 .setCullState(RenderStateShard.CULL)
									 .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
									 .setLightmapState(RenderStateShard.LIGHTMAP)
									 .createCompositeState(false)
	);

	public ChunkFootprintBuffer(ChunkPos pos)
	{
		this.pos = pos;
		this.chunkBox = new AABB(pos.getMinBlockX(), -64, pos.getMinBlockZ(),
				pos.getMaxBlockX()+1, 320, pos.getMaxBlockZ()+1);
	}

	public void add(long pos, short subPos, byte rot, byte variant)
	{
		footprints.add(new Footprint(pos, subPos, rot, variant));
		setDirty();
	}

	public void removeAt(BlockPos pos)
	{
		if(footprints.removeIf(print -> print.toBlockPos().equals(pos)))
			setDirty();
	}

	public void replace(List<Footprint> list)
	{
		footprints.clear();
		footprints.addAll(list);
		rebuildBuffer();
	}

	public boolean isEmpty()
	{
		return footprints.isEmpty();
	}

	public AABB getBoundingBox()
	{
		return chunkBox;
	}

	public void setDirty()
	{
		this.dirty = true;
	}

	public void render(PoseStack poseStack, Matrix4f projectionMatrix, Vec3 cameraPos, Quaternionf cameraRotation)
	{
		if(footprints.isEmpty())
			return;
		if(dirty)
		{
			rebuildBuffer();
			dirty = false;
		}

		poseStack.pushPose();
		poseStack.mulPose(cameraRotation.conjugate(new Quaternionf()));
		poseStack.translate(pos.getMinBlockX()- cameraPos.x,
				-cameraPos.y+0.061, pos.getMinBlockZ() - cameraPos.z);

		RenderType renderType = FOOTPRINT_RENDER_TYPE;
		renderType.setupRenderState();

		buffer.bind();
		buffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionColorTexLightmapShader());
		VertexBuffer.unbind();

		renderType.clearRenderState();

		poseStack.popPose();
	}

	private void rebuildBuffer()
	{
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

		int minX = pos.getMinBlockX();
		int minZ = pos.getMinBlockZ();
		Level level = Minecraft.getInstance().level;

		for (Footprint fp : footprints)
		{
			BlockPos blockPos = fp.toBlockPos();

			float localX = (blockPos.getX() - minX) + fp.getSubBlockX();
			float exactY = blockPos.getY() + fp.getSubBlockY() + 0.002f;
			float localZ = (blockPos.getZ() - minZ) + fp.getSubBlockZ();

			int packedLight = level != null ? LevelRenderer.getLightColor(level, blockPos.above()) : 0xF000F0;

			int blockLight = LightTexture.block(packedLight);
			int skyLight = LightTexture.sky(packedLight);
			float blockFactor = blockLight / 15.0f;

			float finalBrightness = Math.max(0.15f, Math.max(blockFactor, skyLight));
			finalBrightness = Math.clamp(finalBrightness, 0.05f, 0.7f);
			int colorByte = (int) (finalBrightness * 255.0f);

			float uMin, uMax, vMin, vMax;
			float halfWidth, halfHeight;

			switch (fp.variant())
			{
				case 0 -> {
					uMin = 0.0f / 16.0f; uMax = 4.0f / 16.0f;
					vMin = 0.0f / 16.0f; vMax = 4.0f / 16.0f;
					halfWidth = (4.0f * 0.0625f) / 2.0f;
					halfHeight = (4.0f * 0.0625f) / 2.0f;
				}
				case 1 -> {
					uMin = 4.0f / 16.0f; uMax = 9.0f / 16.0f;
					vMin = 0.0f / 16.0f; vMax = 5.0f / 16.0f;
					halfWidth = (5.0f * 0.0625f) / 2.0f;
					halfHeight = (5.0f * 0.0625f) / 2.0f;
				}
				case 2 -> {
					uMin = 9.0f / 16.0f; uMax = 14.0f / 16.0f;
					vMin = 0.0f / 16.0f; vMax = 7.0f / 16.0f;
					halfWidth = (5.0f * 0.0625f) / 2.0f;
					halfHeight = (7.0f * 0.0625f) / 2.0f;
				}
				default -> {
					uMin = 0.0f; uMax = 1.0f;
					vMin = 0.0f; vMax = 1.0f;
					halfWidth = 0.125f;
					halfHeight = 0.125f;
				}
			}

			float rad = (float) Math.toRadians(fp.getRotationDegrees());
			float cos = Mth.cos(rad);
			float sin = Mth.sin(rad);

			float[][] corners =
					{
							{-halfWidth,  halfHeight},
							{ halfWidth,  halfHeight},
							{ halfWidth, -halfHeight},
							{-halfWidth, -halfHeight}
					};

			for (float[] c : corners)
			{
				float rx = c[0] * cos - c[1] * sin;
				float rz = c[0] * sin + c[1] * cos;

				float u = c[0] < 0 ? uMin : uMax;
				float v = c[1] > 0 ? vMin : vMax;

				bufferBuilder.addVertex(localX + rx + 0.125f, exactY, localZ + rz + 0.125f)
							 .setColor(colorByte, colorByte, colorByte, 255)
							 .setUv(u, v)
							 .setLight(packedLight);
			}
		}

		MeshData data = bufferBuilder.build();
		if (data != null)
		{
			buffer.bind();
			buffer.upload(data);
			VertexBuffer.unbind();
		}
	}

	public void close()
	{
		buffer.close();
	}
}
