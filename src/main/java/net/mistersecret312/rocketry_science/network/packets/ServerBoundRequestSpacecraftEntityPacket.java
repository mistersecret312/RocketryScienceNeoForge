package net.mistersecret312.rocketry_science.network.packets;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.data.SpaceCraftData;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPadData;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.menu.RocketAssemblyMenu;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.LinkedHashSet;
import java.util.UUID;

public record ServerBoundRequestSpacecraftEntityPacket(UUID craftUUID) implements CustomPacketPayload
{
	public static final Type<ServerBoundRequestSpacecraftEntityPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "c2s_request_spacecraft"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundRequestSpacecraftEntityPacket> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC, ServerBoundRequestSpacecraftEntityPacket::craftUUID,
			ServerBoundRequestSpacecraftEntityPacket::new
	);

	@Override
	public Type<ServerBoundRequestSpacecraftEntityPacket> type()
	{
		return TYPE;
	}

	public static void handle(ServerBoundRequestSpacecraftEntityPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				Player player = ctx.player();
				Level level = player.level();
				if(level instanceof ServerLevel serverLevel)
				{
					SpaceCraftData data = SpaceCraftData.get(serverLevel);
					SpaceCraft craft = data.getLink(packet.craftUUID);

					RocketEntity rocketEntity = new RocketEntity(craft.level());

					LinkedHashSet<Stage> stages = new LinkedHashSet<>(craft.getStages());
					Rocket rocket = new Rocket(rocketEntity, stages);
					for(Stage stage : stages)
					{
						stage.vessel = rocket;
						for(BlockData blockData : stage.blocks.values())
							blockData.stage = stage;
					}

					PacketDistributor.sendToPlayer(((ServerPlayer) player), new ClientBoundRecieveSpaceCraftEntityPacket(rocket));
				}
			});
	}
}