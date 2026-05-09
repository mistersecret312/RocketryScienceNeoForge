package net.mistersecret312.rocketry_science.data.orbiting_objects;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.mistersecret312.rocketry_science.data.orbits.*;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;

import java.util.UUID;

public class SpaceCraft implements IOrbitObject<Orbit<SpaceCraft>>
{
	public static final StreamCodec<RegistryFriendlyByteBuf, SpaceCraft> STREAM_CODEC =
			StreamCodec.of(SpaceCraft::encode, SpaceCraft::decode);

	private final UUID uuid;

	private Orbit<SpaceCraft> orbit;

	public SpaceCraft(UUID uuid)
	{
		this.uuid = uuid;
	}

	@Override
	public Orbit<SpaceCraft> getOrbit()
	{
		return orbit;
	}

	@Override
	public void setOrbit(Orbit<SpaceCraft> orbit)
	{
		this.orbit = orbit;
	}

	@Override
	public String getName()
	{
		return "";
	}

	public UUID getUUID()
	{
		return uuid;
	}

	public CompoundTag save(RegistryAccess registryAccess)
	{
		CompoundTag tag = new CompoundTag();
		tag.putUUID("uuid", this.uuid);

		if(this.getOrbit() != null)
		{
			if(this.getOrbit() instanceof TransferOrbit)
				tag.putBoolean("transfer", true);
			tag.put("craft_orbit", this.getOrbit().save(registryAccess));
		}
		return tag;
	}

	public static SpaceCraft load(CompoundTag tag, RegistryAccess registryAccess)
	{
		UUID uuid = tag.getUUID("uuid");
		SpaceCraft craft = new SpaceCraft(uuid);

		Orbit<SpaceCraft> orbit;
		CompoundTag orbitTag = tag.getCompound("craft_orbit");
		if(tag.contains("transfer"))
			orbit = new TransferOrbit(craft).load(orbitTag, registryAccess);
		else orbit = new ArtificialOrbit(craft).load(orbitTag, registryAccess);

		craft.setOrbit(orbit);
		return craft;
	}

	public static void encode(RegistryFriendlyByteBuf buf, SpaceCraft craft) {
		buf.writeUUID(craft.uuid);

		boolean hasOrbit = craft.getOrbit() != null;
		buf.writeBoolean(hasOrbit);

		if (hasOrbit) {
			boolean isTransfer = craft.getOrbit() instanceof TransferOrbit;
			buf.writeBoolean(isTransfer);

			if (isTransfer) {
				TransferOrbit transfer = (TransferOrbit) craft.getOrbit();
				TravelPoint.writeTravelPoint(buf, transfer.getDeparture());
				TravelPoint.writeTravelPoint(buf, transfer.getArrival());
				buf.writeLong(transfer.getTravelDuration());
			} else {
				ArtificialOrbit artificial = (ArtificialOrbit) craft.getOrbit();
				buf.writeResourceKey(artificial.getParent());
				ConfiguredOrbit.STREAM_CODEC.encode(buf, artificial.getOrbitData());
			}
		}
	}

	public static SpaceCraft decode(RegistryFriendlyByteBuf buf) {
		UUID uuid = buf.readUUID();
		SpaceCraft craft = new SpaceCraft(uuid);

		boolean hasOrbit = buf.readBoolean();
		if (hasOrbit) {
			boolean isTransfer = buf.readBoolean();

			if (isTransfer) {
				TravelPoint departure = TravelPoint.readTravelPoint(buf);
				TravelPoint arrival = TravelPoint.readTravelPoint(buf);
				long duration = buf.readLong();
				craft.setOrbit(new TransferOrbit(craft, departure, arrival, duration));
			} else {
				ResourceKey<CelestialBody> parent = buf.readResourceKey(CelestialBody.REGISTRY_KEY);
				ConfiguredOrbit orbitData = ConfiguredOrbit.STREAM_CODEC.decode(buf);
				craft.setOrbit(new ArtificialOrbit(parent, craft, orbitData));
			}
		}
		return craft;
	}
}
