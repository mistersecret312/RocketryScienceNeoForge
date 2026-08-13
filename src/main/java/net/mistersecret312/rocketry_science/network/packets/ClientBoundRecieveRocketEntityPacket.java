package net.mistersecret312.rocketry_science.network.packets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.network.ClientPacketHandler;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientBoundRecieveRocketEntityPacket(int id, Rocket rocket, String msg) implements CustomPacketPayload
{
	public static final Type<ClientBoundRecieveRocketEntityPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "s2c_receive_rocket"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundRecieveRocketEntityPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ClientBoundRecieveRocketEntityPacket::id,
			Rocket.STREAM_CODEC, ClientBoundRecieveRocketEntityPacket::rocket,
			ByteBufCodecs.STRING_UTF8, ClientBoundRecieveRocketEntityPacket::msg,
			ClientBoundRecieveRocketEntityPacket::new
	);

	@Override
	public Type<ClientBoundRecieveRocketEntityPacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundRecieveRocketEntityPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ClientPacketHandler.recieveRocketEntity(packet.id, packet.rocket, packet.msg);
			});
	}
}