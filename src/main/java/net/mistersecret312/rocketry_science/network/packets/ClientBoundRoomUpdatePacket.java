package net.mistersecret312.rocketry_science.network.packets;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.network.ClientPacketHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ClientBoundRoomUpdatePacket(UUID uuid, double oxygen, double volume,
										  double targetAtmosphere, double targetTemperature) implements CustomPacketPayload
{
	public static final Type<ClientBoundRoomUpdatePacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "s2c_room_update"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundRoomUpdatePacket> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ClientBoundRoomUpdatePacket::uuid,
			ByteBufCodecs.DOUBLE, ClientBoundRoomUpdatePacket::oxygen,
			ByteBufCodecs.DOUBLE, ClientBoundRoomUpdatePacket::volume,
			ByteBufCodecs.DOUBLE, ClientBoundRoomUpdatePacket::targetAtmosphere,
			ByteBufCodecs.DOUBLE, ClientBoundRoomUpdatePacket::targetTemperature,
			ClientBoundRoomUpdatePacket::new
	);

	@Override
	public Type<ClientBoundRoomUpdatePacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundRoomUpdatePacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ClientPacketHandler.updateRoom(packet.uuid, packet.oxygen, packet.volume, packet.targetAtmosphere, packet.targetTemperature);
			});
	}
}