package net.mistersecret312.rocketry_science.init;

import net.mistersecret312.rocketry_science.network.packets.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkInit
{
	@SubscribeEvent
	public static void registerPackets(final RegisterPayloadHandlersEvent event)
	{
		final PayloadRegistrar registrar = event.registrar("1");

		registrar.playToServer(
				ServerBoundRequestRocketEntityPacket.TYPE,
				ServerBoundRequestRocketEntityPacket.STREAM_CODEC,
				ServerBoundRequestRocketEntityPacket::handle
		);

		registrar.playToServer(
				ServerBoundStartRocketAssemblyPacket.TYPE,
				ServerBoundStartRocketAssemblyPacket.STREAM_CODEC,
				ServerBoundStartRocketAssemblyPacket::handle
		);

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

		registrar.playToClient(
				ClientBoundRocketUpdatePacket.TYPE,
				ClientBoundRocketUpdatePacket.STREAM_CODEC,
				ClientBoundRocketUpdatePacket::handle
		);

		registrar.playToClient(
				ClientBoundSpacecraftRemovePacket.TYPE,
				ClientBoundSpacecraftRemovePacket.STREAM_CODEC,
				ClientBoundSpacecraftRemovePacket::handle
		);

		registrar.playToClient(
				ClientBoundRecieveRocketEntityPacket.TYPE,
				ClientBoundRecieveRocketEntityPacket.STREAM_CODEC,
				ClientBoundRecieveRocketEntityPacket::handle
		);
	}
}
