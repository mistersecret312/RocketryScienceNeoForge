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

public record ClientBoundRecieveSpaceCraftEntityPacket(Rocket rocket) implements CustomPacketPayload
{
	public static final Type<ClientBoundRecieveSpaceCraftEntityPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "s2c_receive_spacecraft"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundRecieveSpaceCraftEntityPacket> STREAM_CODEC = StreamCodec.composite(
			Rocket.STREAM_CODEC, ClientBoundRecieveSpaceCraftEntityPacket::rocket,
			ClientBoundRecieveSpaceCraftEntityPacket::new
	);

	@Override
	public Type<ClientBoundRecieveSpaceCraftEntityPacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundRecieveSpaceCraftEntityPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ClientPacketHandler.recieveSpaceCraft(packet.rocket);
			});
	}
}