package net.mistersecret312.rocketry_science.network.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.network.ClientPacketHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientBoundSpacecraftSyncPacket(SpaceCraft craft) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<ClientBoundSpacecraftSyncPacket> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "s2c_spacecraft_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundSpacecraftSyncPacket> STREAM_CODEC = StreamCodec.composite(
			SpaceCraft.STREAM_CODEC,ClientBoundSpacecraftSyncPacket::craft,
			ClientBoundSpacecraftSyncPacket::new
	);

	@Override
	public Type<ClientBoundSpacecraftSyncPacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundSpacecraftSyncPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ClientPacketHandler.syncSpacecraft(packet.craft);
			});
	}
}