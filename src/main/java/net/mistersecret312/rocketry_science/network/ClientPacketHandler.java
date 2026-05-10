package net.mistersecret312.rocketry_science.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.mistersecret312.rocketry_science.vessel.Rocket;

import java.util.UUID;

public class ClientPacketHandler
{
	public static void syncSpacecraft(SpaceCraft craft)
	{
		OrbitUtil.addSpaceCraft(craft);
	}

	public static void clearSpacecraft()
	{
		OrbitUtil.clearSpaceCraft();
	}

	public static void removeSpaceCraft(UUID uuid)
	{
		OrbitUtil.removeSpaceCraft(uuid);
	}

	public static void updateRocket(int id, Rocket rocket)
	{
		Entity entity = getEntity(id);
		if(entity instanceof RocketEntity rocketEntity)
			rocketEntity.setRocket(rocket);
	}

	public static <T extends Entity> T getEntity(int id)
	{
		ClientLevel level = Minecraft.getInstance().level;
		if(level == null)
			return null;
		Entity entity = level.getEntity(id);
		return (T) entity;
	}
}
