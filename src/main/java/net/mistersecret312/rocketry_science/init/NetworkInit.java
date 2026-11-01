package net.mistersecret312.rocketry_science.init;

import net.mistersecret312.rocketry_science.network.packets.*;
import net.mistersecret312.rocketry_science.network.packets.footprint.*;
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

		registrar.playToServer(
				ServerBoundRequestSpacecraftEntityPacket.TYPE,
				ServerBoundRequestSpacecraftEntityPacket.STREAM_CODEC,
				ServerBoundRequestSpacecraftEntityPacket::handle
		);

		registrar.playToServer(
				ServerBoundRocketTakeoffPacket.TYPE,
				ServerBoundRocketTakeoffPacket.STREAM_CODEC,
				ServerBoundRocketTakeoffPacket::handle
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

		registrar.playToClient(
				ClientBoundAssemblerUpdatePacket.TYPE,
				ClientBoundAssemblerUpdatePacket.STREAM_CODEC,
				ClientBoundAssemblerUpdatePacket::handle
		);

		registrar.playToClient(
				ClientBoundRecieveSpaceCraftEntityPacket.TYPE,
				ClientBoundRecieveSpaceCraftEntityPacket.STREAM_CODEC,
				ClientBoundRecieveSpaceCraftEntityPacket::handle
		);

		registrar.playToClient(
				ClientBoundRoomUpdatePacket.TYPE,
				ClientBoundRoomUpdatePacket.STREAM_CODEC,
				ClientBoundRoomUpdatePacket::handle
		);

		// Footprints

		registrar.playToServer(
				ServerBoundChunkFootprintsRequestPacket.TYPE,
				ServerBoundChunkFootprintsRequestPacket.STREAM_CODEC,
				ServerBoundChunkFootprintsRequestPacket::handle
		);

		registrar.playToClient(
				ClientBoundChunkFootprintsRemovePacket.TYPE,
				ClientBoundChunkFootprintsRemovePacket.STREAM_CODEC,
				ClientBoundChunkFootprintsRemovePacket::handle
		);

		registrar.playToClient(
				ClientBoundChunkFootprintsAddPacket.TYPE,
				ClientBoundChunkFootprintsAddPacket.STREAM_CODEC,
				ClientBoundChunkFootprintsAddPacket::handle
		);

		registrar.playToClient(
				ClientBoundChunkFootprintsSyncPacket.TYPE,
				ClientBoundChunkFootprintsSyncPacket.STREAM_CODEC,
				ClientBoundChunkFootprintsSyncPacket::handle
		);

		registrar.playToClient(
				ClientBoundChunkFootprintsRebuildPacket.TYPE,
				ClientBoundChunkFootprintsRebuildPacket.STREAM_CODEC,
				ClientBoundChunkFootprintsRebuildPacket::handle
		);
	}
}
