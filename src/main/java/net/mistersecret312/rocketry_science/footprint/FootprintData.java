package net.mistersecret312.rocketry_science.footprint;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.SpaceCraftData;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FootprintData extends SavedData
{
	private static final String FILE_NAME = RocketryScience.MODID + "-footprint";

	private static final String FOOTPRINT = "footprints";

	private final Map<Pair<ChunkPos, ResourceLocation>, List<Footprint>> footprintMap = new ConcurrentHashMap<>();
	private MinecraftServer server;

	private CompoundTag serialize()
	{
		CompoundTag tag = new CompoundTag();
		tag.put(FOOTPRINT, serializeFootprintData());
		return tag;
	}

	private CompoundTag serializeFootprintData()
	{
		CompoundTag objectsTag = new CompoundTag();
		this.footprintMap.forEach((key, prints) ->
			{
				CompoundTag chunkTag = new CompoundTag();
				chunkTag.putString("dimension", key.getSecond().toString());

				long[] posArr = prints.stream().mapToLong(Footprint::packedPos).toArray();
				long[] subArr = prints.stream().mapToLong(Footprint::subBlockPos).toArray();
				byte[] rotArr = new byte[prints.size()];
				for(int i = 0; i < prints.size(); i++)
					rotArr[i] = prints.get(i).rotation();
				byte[] varArr = new byte[prints.size()];
				for(int i = 0; i < prints.size(); i++)
					varArr[i] = prints.get(i).variant();

				chunkTag.putLongArray("pos", posArr);
				chunkTag.putLongArray("sub", subArr);
				chunkTag.putByteArray("rot", rotArr);
				chunkTag.putByteArray("var", varArr);

				objectsTag.put(String.valueOf(key.getFirst().toLong()), chunkTag);
			});

		return objectsTag;
	}

	private void deserialize(CompoundTag tag)
	{
		deserializeFootprintData(tag.getCompound(FOOTPRINT));
	}

	private void deserializeFootprintData(CompoundTag tag)
	{
		for(String key : tag.getAllKeys())
		{
			ChunkPos pos = new ChunkPos(Long.parseLong(key));
			CompoundTag chunkTag = tag.getCompound(key);

			String rlString = chunkTag.getString("dimension");
			ResourceLocation rl = ResourceLocation.parse(rlString);

			long[] posArr = chunkTag.getLongArray("pos");
			long[] subArr = chunkTag.getLongArray("sub");
			byte[] rotArr = chunkTag.getByteArray("rot");
			byte[] varArr = chunkTag.getByteArray("var");

			List<Footprint> list = new ArrayList<>();
			for(int i = 0; i < posArr.length; i++)
				list.add(new Footprint(posArr[i], (short) subArr[i], rotArr[i], varArr[i]));

			this.footprintMap.put(new Pair<>(pos, rl), list);
		}
	}

	public void addFootprint(ChunkPos pos, ResourceLocation dimension, Footprint print)
	{
		Pair<ChunkPos, ResourceLocation> pair = new Pair<>(pos, dimension);
		List<Footprint> prints = footprintMap.computeIfAbsent(pair, (key) -> new ArrayList<>());
		prints.add(print);
		enforceGlobalLimit(dimension);
		setDirty();
	}

	private void enforceGlobalLimit(ResourceLocation dimension)
	{
		int totalCount = 0;
		for (Map.Entry<Pair<ChunkPos, ResourceLocation>, List<Footprint>> entry : footprintMap.entrySet())
		{
			if (entry.getKey().getSecond().equals(dimension))
				totalCount += entry.getValue().size();
		}

		while (totalCount > 10_000)
		{
			Pair<ChunkPos, ResourceLocation> oldestPair = null;
			for (Map.Entry<Pair<ChunkPos, ResourceLocation>, List<Footprint>> entry : footprintMap.entrySet())
				if (entry.getKey().getSecond().equals(dimension) && !entry.getValue().isEmpty())
				{
					oldestPair = entry.getKey();
					break;
				}

			if (oldestPair != null)
			{
				List<Footprint> list = footprintMap.get(oldestPair);
				if (list != null && !list.isEmpty())
				{
					list.removeFirst();
					totalCount--;
					if (list.isEmpty())
						footprintMap.remove(oldestPair);
				}
			} else break;
		}
	}

	public void removeFootprintsAt(BlockPos pos, ResourceLocation dimension)
	{
		ChunkPos chunkPos = new ChunkPos(pos);
		Pair<ChunkPos, ResourceLocation> pair = new Pair<>(chunkPos, dimension);

		List<Footprint> list = footprintMap.get(pair);
		if(list != null && list.removeIf(print -> print.toBlockPos().equals(pos)))
		{
			if(list.isEmpty())
				footprintMap.remove(pair);
			setDirty();
		}
	}

	public List<Footprint> getFootprintsAtChunk(ChunkPos pos, ResourceLocation dimension)
	{
		Pair<ChunkPos, ResourceLocation> pair = new Pair<>(pos, dimension);
		return footprintMap.get(pair);
	}

	@Override
	public void setDirty()
	{
		super.setDirty();
	}

	public FootprintData(MinecraftServer server)
	{
		this.server = server;
	}

	public static FootprintData create(MinecraftServer server)
	{
		return new FootprintData(server);
	}

	public static FootprintData load(MinecraftServer server, CompoundTag tag)
	{
		FootprintData data = create(server);

		data.server = server;
		data.deserialize(tag);

		return data;
	}

	public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider)
	{
		tag = serialize();
		return tag;
	}

	@Nonnull
	public static FootprintData get(Level level)
	{
		if(level.isClientSide())
			throw new RuntimeException("Don't access this client-side!");

		return FootprintData.get(level.getServer());
	}

	public static SavedData.Factory<FootprintData> dataFactory(MinecraftServer server)
	{
		return new SavedData.Factory<>(() -> create(server), (tag, provider) -> load(server, tag));
	}

	@Nonnull
	public static FootprintData get(MinecraftServer server)
	{
		DimensionDataStorage storage = server.overworld().getDataStorage();
		return storage.computeIfAbsent(dataFactory(server), FILE_NAME);
	}
}
