package net.mistersecret312.rocketry_science.network.packets.footprint;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.footprint.FootprintClientHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientBoundChunkFootprintsRebuildPacket(ResourceLocation dimension, long pos) implements CustomPacketPayload
{
	public static final Type<ClientBoundChunkFootprintsRebuildPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "s2c_chunkprint_rebuild"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundChunkFootprintsRebuildPacket> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, ClientBoundChunkFootprintsRebuildPacket::dimension,
			ByteBufCodecs.VAR_LONG, ClientBoundChunkFootprintsRebuildPacket::pos,
			ClientBoundChunkFootprintsRebuildPacket::new
	);

	@Override
	public Type<ClientBoundChunkFootprintsRebuildPacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundChunkFootprintsRebuildPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				FootprintClientHandler.handleChunkRebuild(packet, ctx);
			});
	}
}
