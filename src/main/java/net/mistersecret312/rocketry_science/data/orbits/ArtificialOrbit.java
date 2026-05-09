package net.mistersecret312.rocketry_science.data.orbits;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.data.orbiting_objects.SpaceCraft;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;

public class ArtificialOrbit extends Orbit<SpaceCraft>
{
	private final ResourceKey<CelestialBody> parent;
	private final SpaceCraft craft;

	private final ConfiguredOrbit orbit;

	public ArtificialOrbit(ResourceKey<CelestialBody> parent, SpaceCraft craft, ConfiguredOrbit orbitData)
	{
		this.parent = parent;
		this.craft = craft;
		this.orbit = orbitData;
	}

	public ArtificialOrbit(SpaceCraft craft)
	{
		this.parent = null;
		this.craft = craft;
		this.orbit = null;
	}

	@Override
	public CompoundTag save(RegistryAccess registryAccess)
	{
		CompoundTag tag = new CompoundTag();
		tag.putString("parent", this.getParent().location().toString());

		Tag orbitTag = ConfiguredOrbit.CODEC.encodeStart(NbtOps.INSTANCE, this.orbit).getOrThrow();
		tag.put("orbit_data", orbitTag);

		return tag;
	}

	@Override
	public ArtificialOrbit load(CompoundTag tag, RegistryAccess registryAccess)
	{
		String parentString = tag.getString("parent");
		ResourceKey<CelestialBody> parentKey = ResourceKey.create(CelestialBody.REGISTRY_KEY,
				ResourceLocation.parse(parentString));

		ConfiguredOrbit configuredOrbit = ConfiguredOrbit.CODEC.decode(NbtOps.INSTANCE, tag.get("orbit")).getOrThrow().getFirst();
		return new ArtificialOrbit(parentKey, this.getOrbitingObject(), configuredOrbit);
	}

	@Override
	public double getOrbitalPeriod()
	{
		return orbit.orbit().getPeriod();
	}

	public ConfiguredOrbit getOrbitData()
	{
		return orbit;
	}

	@Override
	public double getOrbitalAltitude()
	{
		return orbit.orbit().getAltitude();
	}

	@Override
	public ResourceKey<CelestialBody> getParent()
	{
		return parent;
	}

	@Override
	public SpaceCraft getOrbitingObject()
	{
		return craft;
	}
}
