package net.mistersecret312.rocketry_science.network.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.orbiting_objects.SpaceCraft;
import net.mistersecret312.rocketry_science.network.ClientPacketHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientBoundSpacecraftClearPacket() implements CustomPacketPayload
{
	public static final Type<ClientBoundSpacecraftClearPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "s2c_spacecraft_clear"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundSpacecraftClearPacket> STREAM_CODEC = new StreamCodec<>()
	{
		@Override
		public ClientBoundSpacecraftClearPacket decode(RegistryFriendlyByteBuf registryFriendlyByteBuf)
		{
			return new ClientBoundSpacecraftClearPacket();
		}

		@Override
		public void encode(RegistryFriendlyByteBuf o, ClientBoundSpacecraftClearPacket clientBoundSpacecraftClearPacket)
		{

		}
	};

	@Override
	public Type<ClientBoundSpacecraftClearPacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundSpacecraftClearPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ClientPacketHandler.clearSpacecraft();
			});
	}
}