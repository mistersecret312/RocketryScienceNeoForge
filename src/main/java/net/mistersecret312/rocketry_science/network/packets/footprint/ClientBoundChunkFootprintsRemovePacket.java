package net.mistersecret312.rocketry_science.network.packets.footprint;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.footprint.FootprintClientHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientBoundChunkFootprintsRemovePacket(ResourceLocation dimension, long pos) implements CustomPacketPayload
{
	public static final Type<ClientBoundChunkFootprintsRemovePacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "s2c_chunkprint_remove"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundChunkFootprintsRemovePacket> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, ClientBoundChunkFootprintsRemovePacket::dimension,
			ByteBufCodecs.VAR_LONG, ClientBoundChunkFootprintsRemovePacket::pos,
			ClientBoundChunkFootprintsRemovePacket::new
	);

	@Override
	public Type<ClientBoundChunkFootprintsRemovePacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundChunkFootprintsRemovePacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				FootprintClientHandler.handleSyncRemove(packet, ctx);
			});
	}
}
