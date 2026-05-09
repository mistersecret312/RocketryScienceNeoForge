package net.mistersecret312.rocketry_science.data.orbiting_objects;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.mistersecret312.rocketry_science.data.orbits.ArtificialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.Orbit;
import net.mistersecret312.rocketry_science.data.orbits.TransferOrbit;

import java.util.UUID;

public class SpaceCraft implements IOrbitObject<Orbit<SpaceCraft>>
{
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
}
