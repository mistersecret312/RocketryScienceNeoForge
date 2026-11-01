package net.mistersecret312.rocketry_science.events;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.data.SpaceCraftData;
import net.mistersecret312.rocketry_science.data.room.Room;
import net.mistersecret312.rocketry_science.data.room.RoomManager;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.environment.EnvironmentData;
import net.mistersecret312.rocketry_science.environment.modifiers.ModifierConfig;
import net.mistersecret312.rocketry_science.footprint.Footprint;
import net.mistersecret312.rocketry_science.footprint.FootprintData;
import net.mistersecret312.rocketry_science.init.AttachmentTypeInit;
import net.mistersecret312.rocketry_science.network.packets.ClientBoundSpacecraftSyncPacket;
import net.mistersecret312.rocketry_science.network.packets.footprint.ClientBoundChunkFootprintsRemovePacket;
import net.mistersecret312.rocketry_science.network.packets.footprint.ClientBoundChunkFootprintsSyncPacket;
import net.mistersecret312.rocketry_science.util.EnvironmentUtil;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.mistersecret312.rocketry_science.util.OrbitalMath;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = RocketryScience.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommonEvents
{
	@SubscribeEvent
	public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		Player player = event.getEntity();
		if(player instanceof LocalPlayer localPlayer)
			OrbitUtil.init(localPlayer.clientLevel);

		if(player instanceof ServerPlayer serverPlayer)
		{
			SpaceCraftData data = SpaceCraftData.get(serverPlayer.level());
			data.getSpaceCraft().forEach((uuid, craft) -> PacketDistributor.sendToPlayer(serverPlayer, new ClientBoundSpacecraftSyncPacket(craft)));
		}
	}

	@SubscribeEvent
	public static void entityTick(EntityTickEvent.Pre event)
	{
		EnvironmentUtil.modifiersAffect(event.getEntity());
		OrbitalMath.gravityAffect(event.getEntity());
		if(event.getEntity() instanceof LivingEntity living)
			EnvironmentUtil.airAffect(living);
	}

	@SubscribeEvent
	public static void levelTick(LevelTickEvent.Post event)
	{
		Level level = event.getLevel();
		if(!level.isClientSide() && level.getServer() != null)
		{
			ServerLevel serverLevel = level.getServer().overworld();
			CelestialBody body = OrbitUtil.getCelestialBody(level);
			if(body != null)
			{
				for(ModifierConfig modifier : body.getModifiers())
					modifier.tick(level);
			}

			for(Map.Entry<UUID, SpaceCraft> entry : SpaceCraftData.get(serverLevel).getSpaceCraft().entrySet())
			{
				SpaceCraft craft = entry.getValue();
				craft.tick(serverLevel);
			}

			RoomManager manager = level.getData(AttachmentTypeInit.ROOM_MANAGER);
			manager.tick();
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event)
	{
		if(event.getLevel() instanceof ServerLevel level)
		{
			BlockPos pos = event.getPos();
			ResourceLocation dimension = level.dimension().location();

			FootprintData data = FootprintData.get(level);
			data.removeFootprintsAt(pos, dimension);
			PacketDistributor.sendToAllPlayers(new ClientBoundChunkFootprintsRemovePacket(dimension, pos.asLong()));
		}
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event)
	{
		if (event.getLevel() instanceof ServerLevel serverLevel)
		{
			BlockPos placedPos = event.getPos();
			ResourceLocation dimKey = serverLevel.dimension().location();

			FootprintData data = FootprintData.get(serverLevel);

			data.removeFootprintsAt(placedPos, dimKey);
			BlockPos belowPos = placedPos.below();
			data.removeFootprintsAt(belowPos, dimKey);

			PacketDistributor.sendToAllPlayers(new ClientBoundChunkFootprintsRemovePacket(dimKey, placedPos.asLong()));
			PacketDistributor.sendToAllPlayers(new ClientBoundChunkFootprintsRemovePacket(dimKey, belowPos.asLong()));
		}
	}

	@SubscribeEvent
	public static void chunkWatch(ChunkWatchEvent.Watch event)
	{
		ServerLevel level = event.getLevel();
		ServerPlayer player = event.getPlayer();
		ChunkPos pos = event.getChunk().getPos();
		ResourceLocation dimension = level.dimension().location();

		FootprintData data = FootprintData.get(level);
		List<Footprint> footprintList = data.getFootprintsAtChunk(pos, dimension);
		if(footprintList != null && !footprintList.isEmpty())
		{
			byte[] encoded = Footprint.encode(footprintList);
			PacketDistributor.sendToPlayer(player, new ClientBoundChunkFootprintsSyncPacket(dimension, pos.toLong(), encoded));
		}
	}
}
