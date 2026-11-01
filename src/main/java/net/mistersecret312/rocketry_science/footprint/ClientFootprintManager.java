package net.mistersecret312.rocketry_science.footprint;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientFootprintManager
{
	private static final Map<ChunkPos, ChunkFootprintBuffer> CHUNK_BUFFERS = new ConcurrentHashMap<>();

	public static void addFootprint(long pos, short subPos, byte rot, byte variant)
	{
		BlockPos blockPos = BlockPos.of(pos);
		ChunkPos chunkPos = new ChunkPos(blockPos);

		CHUNK_BUFFERS.computeIfAbsent(chunkPos, ChunkFootprintBuffer::new).add(pos, subPos, rot, variant);
	}

	public static void removeFootprintsAt(BlockPos pos)
	{
		ChunkPos chunkPos = new ChunkPos(pos);
		ChunkFootprintBuffer buffer = CHUNK_BUFFERS.get(chunkPos);
		if(buffer != null)
		{
			buffer.removeAt(pos);
			if(buffer.isEmpty())
			{
				buffer.close();
				CHUNK_BUFFERS.remove(chunkPos);
			}
		}
	}

	public static void setChunkFootprints(ChunkPos chunkPos, List<Footprint> footprints)
	{
		if (footprints.isEmpty())
		{
			removeChunk(chunkPos);
			return;
		}

		ChunkFootprintBuffer buffer = CHUNK_BUFFERS.computeIfAbsent(chunkPos, ChunkFootprintBuffer::new);
		buffer.replace(footprints);
	}

	public static void removeChunk(ChunkPos chunkPos)
	{
		ChunkFootprintBuffer buffer = CHUNK_BUFFERS.remove(chunkPos);
		if (buffer != null)
			buffer.close();
	}

	public static void renderVisible(PoseStack poseStack, Matrix4f projectionMatrix, Frustum frustum, Vec3 camPos, Quaternionf camRot)
	{
		for (ChunkFootprintBuffer buffer : CHUNK_BUFFERS.values())
		{
			if (frustum.isVisible(buffer.getBoundingBox()))
				buffer.render(poseStack, projectionMatrix, camPos, camRot);
		}
	}

	public static void markChunkDirty(BlockPos pos)
	{
		ChunkPos chunkPos = new ChunkPos(pos);
		ChunkFootprintBuffer buffer = CHUNK_BUFFERS.get(chunkPos);
		if (buffer != null)
			buffer.setDirty();
	}

	public static void clear()
	{
		CHUNK_BUFFERS.values().forEach(ChunkFootprintBuffer::close);
		CHUNK_BUFFERS.clear();
	}
}
