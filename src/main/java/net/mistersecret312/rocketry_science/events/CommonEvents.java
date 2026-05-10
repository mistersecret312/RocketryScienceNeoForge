package net.mistersecret312.rocketry_science.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.data.SpaceCraftData;
import net.mistersecret312.rocketry_science.network.packets.ClientBoundSpacecraftSyncPacket;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.mistersecret312.rocketry_science.util.OrbitalMath;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = RocketryScience.MODID, bus = EventBusSubscriber.Bus.GAME)
public class CommonEvents
{
	@SubscribeEvent
	public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		Player player = event.getEntity();
		if(player instanceof ServerPlayer serverPlayer)
		{
			SpaceCraftData data = SpaceCraftData.get(serverPlayer.level());
			data.getSpaceCraft().forEach((uuid, craft) -> PacketDistributor.sendToPlayer(serverPlayer, new ClientBoundSpacecraftSyncPacket(craft)));
		}
	}

	@SubscribeEvent
	public static void entityTick(EntityTickEvent.Pre event)
	{
		OrbitalMath.gravityAffect(event.getEntity());
	}

	@SubscribeEvent
	public static void levelTick(LevelTickEvent.Post event)
	{
		Level level = event.getLevel();
		if(!level.isClientSide() && level.getServer() != null)
		{
			ServerLevel serverLevel = level.getServer().overworld();
			for(Map.Entry<UUID, SpaceCraft> entry : SpaceCraftData.get(serverLevel).getSpaceCraft().entrySet())
			{
				SpaceCraft craft = entry.getValue();
				craft.tick(serverLevel);
			}
		}
	}
}
