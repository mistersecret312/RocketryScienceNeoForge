package net.mistersecret312.rocketry_science.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPadData;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.menu.RocketAssemblyMenu;
import net.mistersecret312.rocketry_science.network.ClientPacketHandler;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerBoundRequestRocketEntityPacket() implements CustomPacketPayload
{
	public static final Type<ServerBoundRequestRocketEntityPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "c2s_request_rocket"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundRequestRocketEntityPacket> STREAM_CODEC = new StreamCodec<>()
	{
		@Override
		public ServerBoundRequestRocketEntityPacket decode(RegistryFriendlyByteBuf registryFriendlyByteBuf)
		{
			return new ServerBoundRequestRocketEntityPacket();
		}

		@Override
		public void encode(RegistryFriendlyByteBuf o, ServerBoundRequestRocketEntityPacket packet)
		{

		}
	};

	@Override
	public Type<ServerBoundRequestRocketEntityPacket> type()
	{
		return TYPE;
	}

	public static void handle(ServerBoundRequestRocketEntityPacket packet, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
			{
				Player player = ctx.player();
				if(player.containerMenu instanceof RocketAssemblyMenu assemblyMenu)
				{
					Level level = assemblyMenu.level;
					RocketAssemblerBlockEntity blockEntity = assemblyMenu.blockEntity;

					RocketPadData data = RocketPadData.get(level.getServer());
					RocketPad rocketPad = data.rocketPads.get(blockEntity.getPadUUID());
					if(rocketPad == null)
						return;
					Level padLevel = level.getServer().getLevel(rocketPad.getDimension());
					if(padLevel == null)
						return;

					RocketPadBlockEntity padBE = (RocketPadBlockEntity) padLevel.getBlockEntity(rocketPad.getPos());
					if(padBE != null)
					{
						RocketEntity rocketEntity = new RocketEntity(padLevel);
						String msg = blockEntity.assembleRocket(padBE, rocketEntity, true);

						AABB box = rocketEntity.makeBoundingBox();
						double desiredTime = 0;

						AABB stagesBox = new AABB(rocketEntity.position(), rocketEntity.position());
						for(Stage stage : rocketEntity.getRocket().getStages())
							for(BlockData value : stage.blocks.values())
							{
								desiredTime += 20;
							}

						double volume = box.getXsize()*box.getYsize()*box.getZsize();
						blockEntity.maxProgress = desiredTime;
						blockEntity.setChanged();
						level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 2);

						PacketDistributor.sendToPlayer(
								(ServerPlayer) player, new ClientBoundRecieveRocketEntityPacket(rocketEntity.getRocket(), msg));
					}
				}
			});
	}
}