package net.mistersecret312.rocketry_science.network.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.network.ClientPacketHandler;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientBoundRocketUpdatePacket(int id, Rocket rocket) implements CustomPacketPayload
{
	public static final Type<ClientBoundRocketUpdatePacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "s2c_rocket_update"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundRocketUpdatePacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ClientBoundRocketUpdatePacket::id,
			Rocket.STREAM_CODEC, ClientBoundRocketUpdatePacket::rocket,
			ClientBoundRocketUpdatePacket::new
	);

	@Override
	public Type<ClientBoundRocketUpdatePacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundRocketUpdatePacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ClientPacketHandler.updateRocket(packet.id, packet.rocket);
			});
	}
}