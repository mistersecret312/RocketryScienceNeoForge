package net.mistersecret312.rocketry_science.data.orbits;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;

public class TravelPoint
{
	private final ConfiguredOrbit orbit;
	private final long tick;
	private final ResourceKey<CelestialBody> body;

	private TravelPoint(ConfiguredOrbit orbit, long tick, ResourceKey<CelestialBody> body)
	{
		this.orbit = orbit;
		this.tick = tick;
		this.body = body;
	}

	public ConfiguredOrbit getOrbit()
	{
		return orbit;
	}

	public long getTick()
	{
		return tick;
	}

	public ResourceKey<CelestialBody> getBody()
	{
		return body;
	}

	public CompoundTag save()
	{
		CompoundTag tag = new CompoundTag();

		tag.putString("body", this.getBody().location().toString());
		tag.putLong("tick", this.getTick());

		Tag orbitTag = ConfiguredOrbit.CODEC.encodeStart(NbtOps.INSTANCE, this.getOrbit()).getOrThrow();
		tag.put("orbit", orbitTag);

		return tag;
	}

	public static TravelPoint load(CompoundTag tag)
	{
		String parentString = tag.getString("body");
		ResourceKey<CelestialBody> parentKey = ResourceKey.create(CelestialBody.REGISTRY_KEY,
				ResourceLocation.parse(parentString));
		long tick = tag.getLong("tick");

		ConfiguredOrbit configuredOrbit = ConfiguredOrbit.CODEC.decode(NbtOps.INSTANCE, tag.get("orbit")).getOrThrow().getFirst();
		return new TravelPoint(configuredOrbit, tick, parentKey);
	}

	public static void writeTravelPoint(RegistryFriendlyByteBuf buf, TravelPoint point) {
		buf.writeResourceKey(point.getBody());
		buf.writeLong(point.getTick());
		ConfiguredOrbit.STREAM_CODEC.encode(buf, point.getOrbit());
	}

	public static TravelPoint readTravelPoint(RegistryFriendlyByteBuf buf) {
		ResourceKey<CelestialBody> body = buf.readResourceKey(CelestialBody.REGISTRY_KEY);
		long tick = buf.readLong();
		ConfiguredOrbit orbit = ConfiguredOrbit.STREAM_CODEC.decode(buf);

		return new TravelPoint(orbit, tick, body);
	}
}