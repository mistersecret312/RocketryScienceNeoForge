package net.mistersecret312.rocketry_science.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.menu.RocketAssemblyMenu;
import net.mistersecret312.rocketry_science.network.ClientPacketHandler;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerBoundStartRocketAssemblyPacket() implements CustomPacketPayload
{
	public static final Type<ServerBoundStartRocketAssemblyPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "c2s_start_assembly"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundStartRocketAssemblyPacket> STREAM_CODEC = new StreamCodec<>()
	{
		@Override
		public ServerBoundStartRocketAssemblyPacket decode(RegistryFriendlyByteBuf registryFriendlyByteBuf)
		{
			return new ServerBoundStartRocketAssemblyPacket();
		}

		@Override
		public void encode(RegistryFriendlyByteBuf o, ServerBoundStartRocketAssemblyPacket packet)
		{

		}
	};

	@Override
	public Type<ServerBoundStartRocketAssemblyPacket> type()
	{
		return TYPE;
	}

	public static void handle(ServerBoundStartRocketAssemblyPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				Player player = ctx.player();
				if(player.containerMenu instanceof RocketAssemblyMenu assemblyMenu)
					assemblyMenu.blockEntity.startAssembly();
			});
	}
}