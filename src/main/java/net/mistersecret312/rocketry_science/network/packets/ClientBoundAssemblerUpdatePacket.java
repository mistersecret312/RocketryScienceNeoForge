package net.mistersecret312.rocketry_science.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.network.ClientPacketHandler;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientBoundAssemblerUpdatePacket(BlockPos pos, double progress, double maxProgress, boolean started) implements CustomPacketPayload
{
	public static final Type<ClientBoundAssemblerUpdatePacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "s2c_update_assembler"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundAssemblerUpdatePacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ClientBoundAssemblerUpdatePacket::pos,
			ByteBufCodecs.DOUBLE, ClientBoundAssemblerUpdatePacket::progress,
			ByteBufCodecs.DOUBLE, ClientBoundAssemblerUpdatePacket::maxProgress,
			ByteBufCodecs.BOOL, ClientBoundAssemblerUpdatePacket::started,
			ClientBoundAssemblerUpdatePacket::new
	);

	@Override
	public Type<ClientBoundAssemblerUpdatePacket> type()
	{
		return TYPE;
	}

	public static void handle(ClientBoundAssemblerUpdatePacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				ClientPacketHandler.updateAssembler(packet.pos, packet.progress, packet.maxProgress, packet.started);
			});
	}
}