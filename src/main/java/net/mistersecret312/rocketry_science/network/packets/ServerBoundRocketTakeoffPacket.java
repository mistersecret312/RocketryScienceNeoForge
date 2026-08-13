package net.mistersecret312.rocketry_science.network.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.LaunchControllerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPadData;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.menu.LaunchControllerMenu;
import net.mistersecret312.rocketry_science.menu.RocketAssemblyMenu;
import net.mistersecret312.rocketry_science.vessel.VesselState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public record ServerBoundRocketTakeoffPacket() implements CustomPacketPayload
{
	public static final Type<ServerBoundRocketTakeoffPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "c2s_start_takeoff"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundRocketTakeoffPacket> STREAM_CODEC = new StreamCodec<>()
	{
		@Override
		public ServerBoundRocketTakeoffPacket decode(RegistryFriendlyByteBuf registryFriendlyByteBuf)
		{
			return new ServerBoundRocketTakeoffPacket();
		}

		@Override
		public void encode(RegistryFriendlyByteBuf o, ServerBoundRocketTakeoffPacket packet)
		{

		}
	};

	@Override
	public Type<ServerBoundRocketTakeoffPacket> type()
	{
		return TYPE;
	}

	public static void handle(ServerBoundRocketTakeoffPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				Player player = ctx.player();
				if(player.containerMenu instanceof LaunchControllerMenu menu)
				{
					BlockEntity blockEntity = menu.blockEntity;
					Level level = menu.level;
					if(blockEntity instanceof LaunchControllerBlockEntity controller)
					{
						if(controller.getPadUUID() == null || level.getServer() == null)
							return;

						RocketPadData data = RocketPadData.get(level.getServer());
						RocketPad rocketPad = data.rocketPads.get(controller.getPadUUID());
						if(rocketPad == null)
							return;
						Level padLevel = level.getServer().getLevel(rocketPad.getDimension());
						if(padLevel == null)
							return;

						RocketPadBlockEntity padBE = (RocketPadBlockEntity) padLevel.getBlockEntity(rocketPad.getPos());
						if(padBE != null)
						{
							AABB box = padBE.getOnPadBox();
							List<RocketEntity> rocketEntity = padLevel.getEntitiesOfClass(RocketEntity.class, box);
							if(!rocketEntity.isEmpty())
							{
								RocketEntity first = rocketEntity.getFirst();
								first.getRocket().canLand = true;
								first.getRocket().setState(VesselState.TAKEOFF);
							}
						}
					}
				}
			});
	}
}