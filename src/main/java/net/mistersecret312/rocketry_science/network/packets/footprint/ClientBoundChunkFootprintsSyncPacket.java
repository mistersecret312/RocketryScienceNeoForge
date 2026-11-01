package net.mistersecret312.rocketry_science.network.packets.footprint;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.footprint.FootprintClientHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientBoundChunkFootprintsSyncPacket(ResourceLocation dimension, long pos, byte[] data) implements CustomPacketPayload
{
	public static final Type<ClientBoundChunkFootprintsSyncPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "s2c_chunkprint_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundChunkFootprintsSyncPacket> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, ClientBoundChunkFootprintsSyncPacket::dimension,
			ByteBufCodecs.VAR_LONG, ClientBoundChunkFootprintsSyncPacket::pos,
			ByteBufCodecs.BYTE_ARRAY, ClientBoundChunkFootprintsSyncPacket::data,
			ClientBoundChunkFootprintsSyncPacket::new
	);

	@Override
	public Type<ClientBoundChunkFootprintsSyncPacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundChunkFootprintsSyncPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				FootprintClientHandler.handleChunkSync(packet, ctx);
			});
	}
}
