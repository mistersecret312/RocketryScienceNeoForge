package net.mistersecret312.rocketry_science.network.packets.footprint;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.footprint.FootprintClientHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientBoundChunkFootprintsAddPacket(ResourceLocation dimension, long pos, short subPos, byte rot, byte variant) implements CustomPacketPayload
{
	public static final Type<ClientBoundChunkFootprintsAddPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "s2c_chunkprint_add"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundChunkFootprintsAddPacket> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, ClientBoundChunkFootprintsAddPacket::dimension,
			ByteBufCodecs.VAR_LONG, ClientBoundChunkFootprintsAddPacket::pos,
			ByteBufCodecs.SHORT, ClientBoundChunkFootprintsAddPacket::subPos,
			ByteBufCodecs.BYTE, ClientBoundChunkFootprintsAddPacket::rot,
			ByteBufCodecs.BYTE, ClientBoundChunkFootprintsAddPacket::variant,
			ClientBoundChunkFootprintsAddPacket::new
	);

	@Override
	public Type<ClientBoundChunkFootprintsAddPacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundChunkFootprintsAddPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				FootprintClientHandler.handleSyncAdd(packet, ctx);
			});
	}
}
