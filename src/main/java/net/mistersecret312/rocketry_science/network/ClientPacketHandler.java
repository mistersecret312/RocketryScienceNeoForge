package net.mistersecret312.rocketry_science.network;

import net.mistersecret312.rocketry_science.data.orbiting_objects.SpaceCraft;
import net.mistersecret312.rocketry_science.util.OrbitUtil;

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
}
