package net.mistersecret312.rocketry_science.init;

import net.mistersecret312.rocketry_science.network.packets.ClientBoundSpacecraftClearPacket;
import net.mistersecret312.rocketry_science.network.packets.ClientBoundSpacecraftSyncPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkInit
{
	@SubscribeEvent
	public static void registerPackets(final RegisterPayloadHandlersEvent event)
	{
		final PayloadRegistrar registrar = event.registrar("1");

		registrar.playToClient(
				ClientBoundSpacecraftClearPacket.TYPE,
				ClientBoundSpacecraftClearPacket.STREAM_CODEC,
				ClientBoundSpacecraftClearPacket::handle
		);

		registrar.playToClient(
				ClientBoundSpacecraftSyncPacket.TYPE,
				ClientBoundSpacecraftSyncPacket.STREAM_CODEC,
				ClientBoundSpacecraftSyncPacket::handle
		);
	}
}
