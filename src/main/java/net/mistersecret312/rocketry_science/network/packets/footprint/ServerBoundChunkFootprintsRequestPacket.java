package net.mistersecret312.rocketry_science.network.packets.footprint;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.footprint.Footprint;
import net.mistersecret312.rocketry_science.footprint.FootprintClientHandler;
import net.mistersecret312.rocketry_science.footprint.FootprintData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerBoundChunkFootprintsRequestPacket(long pos, short subPos, byte rot, byte variant) implements CustomPacketPayload
{
	public static final Type<ServerBoundChunkFootprintsRequestPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "c2s_chunkprint_request"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundChunkFootprintsRequestPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, ServerBoundChunkFootprintsRequestPacket::pos,
			ByteBufCodecs.SHORT, ServerBoundChunkFootprintsRequestPacket::subPos,
			ByteBufCodecs.BYTE, ServerBoundChunkFootprintsRequestPacket::rot,
			ByteBufCodecs.BYTE, ServerBoundChunkFootprintsRequestPacket::variant,
			ServerBoundChunkFootprintsRequestPacket::new
	);

	@Override
	public Type<ServerBoundChunkFootprintsRequestPacket> type()
	{
		return TYPE;
	}

	public static void handle(ServerBoundChunkFootprintsRequestPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ServerPlayer player = (ServerPlayer) ctx.player();
				if (player == null) return;

				ServerLevel level = player.serverLevel();
				BlockPos pos = BlockPos.of(packet.pos);

				if (player.blockPosition().distSqr(pos) > 64)
					return;

				ChunkPos chunkPos = new ChunkPos(pos);
				ResourceLocation dimKey = level.dimension().location();
				Footprint footprint = new Footprint(packet.pos, packet.subPos, packet.rot, packet.variant);

				FootprintData data = FootprintData.get(level);
				data.addFootprint(chunkPos, dimKey, footprint);

				ClientBoundChunkFootprintsAddPacket broadcastPacket = new ClientBoundChunkFootprintsAddPacket(dimKey, packet.pos, packet.subPos, packet.rot, packet.variant);
				PacketDistributor.sendToAllPlayers(broadcastPacket);
			});
	}
}
