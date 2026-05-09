package net.mistersecret312.rocketry_science.data.rocket_pad;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.data.orbiting_objects.SpaceCraft;
import net.mistersecret312.rocketry_science.data.orbits.Orbit;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class RocketPadData extends SavedData
{
	private static final String FILE_NAME = RocketryScience.MODID + "-rocketpad";

	private static final String ROCKET_PADS = "rocketpad";

	public HashMap<UUID, RocketPad> rocketPads = new HashMap<>();

	private MinecraftServer server;

	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================

	private CompoundTag serialize()
	{
		CompoundTag tag = new CompoundTag();

		tag.put(ROCKET_PADS, serializeRocketPadData());

		return tag;
	}

	private CompoundTag serializeRocketPadData()
	{
		CompoundTag objectsTag = new CompoundTag();

		this.rocketPads.forEach((uuid, craft) ->
			{
				objectsTag.put(uuid.toString(), craft.save());
			});

		return objectsTag;
	}

	private void deserialize(CompoundTag tag)
	{
		deserializeRocketPadData(tag.getCompound(ROCKET_PADS));
	}

	private void deserializeRocketPadData(CompoundTag tag)
	{
		for(String key : tag.getAllKeys())
		{
			this.rocketPads.put(UUID.fromString(key),
					RocketPad.load(tag.getCompound(key)));
		}
	}

	public void addRocketPad(UUID uuid, BlockPos pos, ResourceKey<Level> dimension)
	{
		this.rocketPads.put(uuid, new RocketPad(pos, dimension));
		this.setDirty();
	}

	public RocketPad getRocketPad(UUID uuid)
	{
		return rocketPads.get(uuid);
	}

	@Override
	public void setDirty()
	{
		super.setDirty();
		Iterator<Map.Entry<UUID, RocketPad>> iterator = this.rocketPads.entrySet().iterator();
		while(iterator.hasNext())
		{
			Map.Entry<UUID, RocketPad> entry = iterator.next();
			ServerLevel level = server.getLevel(entry.getValue().getDimension());
			if(level == null)
				continue;
			if(level.getBlockEntity(entry.getValue().getPos()) instanceof RocketPadBlockEntity pad)
			{
				if(!pad.isMaster())
					pad = (RocketPadBlockEntity) level.getBlockEntity(pad.getMasterPos());

				if(!pad.getUUID().equals(entry.getKey()))
					iterator.remove();
			}
			else iterator.remove();
		}
	}

	public RocketPadData(MinecraftServer server)
	{
		this.server = server;
	}

	public static RocketPadData create(MinecraftServer server)
	{
		return new RocketPadData(server);
	}

	public static RocketPadData load(MinecraftServer server, CompoundTag tag)
	{
		RocketPadData data = create(server);

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
	public static RocketPadData get(Level level)
	{
		if(level.isClientSide())
			throw new RuntimeException("Don't access this client-side!");

		return RocketPadData.get(level.getServer());
	}

	public static Factory<RocketPadData> dataFactory(MinecraftServer server)
	{
		return new Factory<>(() -> create(server), (tag, provider) -> load(server, tag));
	}

	@Nonnull
	public static RocketPadData get(MinecraftServer server)
	{
		DimensionDataStorage storage = server.overworld().getDataStorage();

		return storage.computeIfAbsent(dataFactory(server), FILE_NAME);
	}
}
