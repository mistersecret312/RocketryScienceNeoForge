package net.mistersecret312.rocketry_science.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.orbiting_objects.SpaceCraft;
import net.mistersecret312.rocketry_science.data.orbits.Orbit;
import net.mistersecret312.rocketry_science.network.packets.ClientBoundSpacecraftSyncPacket;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;

public class SpaceCraftData extends SavedData
{
	private static final String FILE_NAME = RocketryScience.MODID + "-spacecraft";

	private static final String SPACECRAFT = "spacecraft";

	public HashMap<UUID, SpaceCraft> spaceCraft = new HashMap<>();

	private MinecraftServer server;

	//============================================================================================
	//*************************************Saving and Loading*************************************
	//============================================================================================

	private CompoundTag serialize()
	{
		CompoundTag tag = new CompoundTag();

		tag.put(SPACECRAFT, serializeSpaceCraftData());

		return tag;
	}

	private CompoundTag serializeSpaceCraftData()
	{
		CompoundTag objectsTag = new CompoundTag();

		this.spaceCraft.forEach((uuid, craft) ->
			{
				objectsTag.put(uuid.toString(), craft.save(server.registryAccess()));
			});

		return objectsTag;
	}

	private void deserialize(CompoundTag tag)
	{
		deserializeSpaceCraftData(tag.getCompound(SPACECRAFT));
	}

	private void deserializeSpaceCraftData(CompoundTag tag)
	{
		for(String key : tag.getAllKeys())
		{
			this.spaceCraft.put(UUID.fromString(key),
					SpaceCraft.load(tag.getCompound(key), server.registryAccess()));
		}
	}

	public SpaceCraft addFreshSpaceCraft(UUID uuid)
	{
		SpaceCraft craft = this.spaceCraft.computeIfAbsent(uuid, SpaceCraft::new);
		this.setDirty(uuid);

		return craft;
	}

	public SpaceCraft addOrbitToSpacecraft(UUID uuid, Orbit<SpaceCraft> orbit)
	{
		SpaceCraft craft = this.spaceCraft.computeIfAbsent(uuid, SpaceCraft::new);
		craft.setOrbit(orbit);

		this.spaceCraft.put(uuid, craft);
		this.setDirty(uuid);

		return craft;
	}

	public SpaceCraft getLink(UUID uuid)
	{
		return this.spaceCraft.get(uuid);
	}

	public void setDirty(UUID uuid)
	{
		SpaceCraft craft = spaceCraft.get(uuid);
		PacketDistributor.sendToAllPlayers(new ClientBoundSpacecraftSyncPacket(craft));
		this.setDirty();
	}

	@Override
	public void setDirty()
	{
		super.setDirty();
	}

	public SpaceCraftData(MinecraftServer server)
	{
		this.server = server;
	}

	public static SpaceCraftData create(MinecraftServer server)
	{
		return new SpaceCraftData(server);
	}

	public static SpaceCraftData load(MinecraftServer server, CompoundTag tag)
	{
		SpaceCraftData data = create(server);

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
	public static SpaceCraftData get(Level level)
	{
		if(level.isClientSide())
			throw new RuntimeException("Don't access this client-side!");

		return SpaceCraftData.get(level.getServer());
	}

	public static SavedData.Factory<SpaceCraftData> dataFactory(MinecraftServer server)
	{
		return new SavedData.Factory<>(() -> create(server), (tag, provider) -> load(server, tag));
	}

	@Nonnull
	public static SpaceCraftData get(MinecraftServer server)
	{
		DimensionDataStorage storage = server.overworld().getDataStorage();

		return storage.computeIfAbsent(dataFactory(server), FILE_NAME);
	}
}
