package net.mistersecret312.rocketry_science.network.packets;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.network.ClientPacketHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ClientBoundSpacecraftRemovePacket(UUID uuid) implements CustomPacketPayload
{
	public static final Type<ClientBoundSpacecraftRemovePacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "s2c_spacecraft_remove"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundSpacecraftRemovePacket> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ClientBoundSpacecraftRemovePacket::uuid,
			ClientBoundSpacecraftRemovePacket::new
	);

	@Override
	public Type<ClientBoundSpacecraftRemovePacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundSpacecraftRemovePacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ClientPacketHandler.removeSpaceCraft(packet.uuid);
			});
	}
}