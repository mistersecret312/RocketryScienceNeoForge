package net.mistersecret312.rocketry_science.footprint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.network.packets.footprint.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

@EventBusSubscriber(modid = RocketryScience.MODID, value = Dist.CLIENT)
public class FootprintClientHandler
{
	private static double lastX, lastZ;
	private static float distanceAccumulator = 0.0f;
	private static ResourceLocation lastDimension = null;
	private static boolean alternateFoot = false;
	private static boolean wasOnGround = true;
	private static float oldFallDistance = 0f;

	private static final TagKey<Block> FOOTPRINT_BLOCKS = TagKey.create(Registries.BLOCK,
			ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "footprinting_blocks"));

	@SubscribeEvent
	public static void onPlayerTick(ClientTickEvent.Post event)
	{
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || player.isSpectator())
			return;

		ResourceLocation currentDim = player.level().dimension().location();
		if (lastDimension != null && !lastDimension.equals(currentDim))
		{
			ClientFootprintManager.clear();
			distanceAccumulator = 0.0f;
		}
		lastDimension = currentDim;

		boolean isOnGround = player.onGround();
		if (!wasOnGround && isOnGround && oldFallDistance > 2f)
		{
			trySpawnLandingFootprints(player);
			distanceAccumulator = 0f;
		}
		wasOnGround = isOnGround;

		if(isOnGround)
		{
			double dx = player.getX() - lastX;
			double dz = player.getZ() - lastZ;
			distanceAccumulator += (float) Math.sqrt(dx * dx + dz * dz);
			lastX = player.getX();
			lastZ = player.getZ();
		}
		else distanceAccumulator = 0f;

		if (distanceAccumulator >= 0.6f && oldFallDistance == 0f)
		{
			distanceAccumulator = 0.0f;
			trySpawnWalkingFootprint(player);
		}

		oldFallDistance = player.fallDistance;
	}

	private static void trySpawnLandingFootprints(LocalPlayer player) {
		Level level = player.level();
		float yaw = player.getYRot();
		float rad = Mth.DEG_TO_RAD * yaw;

		float rightX = Mth.cos(rad);
		float rightZ = Mth.sin(rad);

		float forwardX = -Mth.sin(rad) * 0.1f;
		float forwardZ = Mth.cos(rad) * 0.1f;

		spawnSingleFootprintAt(player, level, yaw, forwardX - rightX * 0.22f, forwardZ - rightZ * 0.22f);
		spawnSingleFootprintAt(player, level, yaw, forwardX + rightX * 0.22f, forwardZ + rightZ * 0.22f);
	}

	private static void trySpawnWalkingFootprint(LocalPlayer player)
	{
		Level level = player.level();
		float yaw = player.getYRot();
		float rad = Mth.DEG_TO_RAD * yaw;

		float rightX = Mth.cos(rad);
		float rightZ = Mth.sin(rad);

		float sideOffset = alternateFoot ? 0.22f : -0.22f;
		alternateFoot = !alternateFoot;

		spawnSingleFootprintAt(player, level, yaw, rightX * sideOffset, rightZ * sideOffset);
	}

	private static void spawnSingleFootprintAt(LocalPlayer player, Level level, float yaw, float offsetX, float offsetZ) {
		double footX = player.getX() + offsetX;
		double footZ = player.getZ() + offsetZ;
		double footY = player.getY();

		BlockPos belowPos = new BlockPos(
				Mth.floor(footX),
				Mth.floor(footY - 0.05),
				Mth.floor(footZ)
		);

		if (!level.getBlockState(belowPos).is(FOOTPRINT_BLOCKS))
			return;

		float relX = (float) (footX - belowPos.getX());
		float relY = (float) (footY - belowPos.getY());
		float relZ = (float) (footZ - belowPos.getZ());

		int subX = Mth.clamp((int)(relX * 16), 0, 15);
		int subY = Mth.clamp((int)(relY * 16), 0, 15);
		int subZ = Mth.clamp((int)(relZ * 16), 0, 15);

		short packedSubPos = (short) (subX | (subY << 4) | (subZ << 8));
		byte packedRot = Footprint.packRotation(yaw);
		byte footprintType = 0;
		ItemStack stack = player.getItemBySlot(EquipmentSlot.FEET);
		if(!stack.isEmpty())
			footprintType = 1;
		if(stack.is(Items.IRON_BOOTS))
			footprintType = 2;

		PacketDistributor.sendToServer(new ServerBoundChunkFootprintsRequestPacket(belowPos.asLong(), packedSubPos, packedRot, footprintType));
	}

	public static void handleSyncAdd(ClientBoundChunkFootprintsAddPacket packet, IPayloadContext context)
	{
		context.enqueueWork(() -> {
			LocalPlayer player = (LocalPlayer) context.player();
			if (player != null && player.level().dimension().location().equals(packet.dimension()))
				ClientFootprintManager.addFootprint(packet.pos(), packet.subPos(), packet.rot(), packet.variant());
		});
	}

	public static void handleSyncRemove(ClientBoundChunkFootprintsRemovePacket packet, IPayloadContext context)
	{
		context.enqueueWork(() -> {
			LocalPlayer player = (LocalPlayer) context.player();
			if (player != null && player.level().dimension().location().equals(packet.dimension()))
				ClientFootprintManager.removeFootprintsAt(BlockPos.of(packet.pos()));
		});
	}

	public static void handleChunkSync(ClientBoundChunkFootprintsSyncPacket packet, IPayloadContext context)
	{
		context.enqueueWork(() -> {
			LocalPlayer player = (LocalPlayer) context.player();
			if (player != null && player.level().dimension().location().equals(packet.dimension()))
			{
				List<Footprint> decoded = Footprint.decode(packet.data());
				ClientFootprintManager.setChunkFootprints(new ChunkPos(packet.pos()), decoded);
			}
		});
	}

	@SubscribeEvent
	public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event)
	{
		ClientFootprintManager.clear();
		lastDimension = null;
	}

	public static void handleChunkRebuild(ClientBoundChunkFootprintsRebuildPacket packet, IPayloadContext context)
	{
		context.enqueueWork(() -> {
			LocalPlayer player = (LocalPlayer) context.player();
			if (player != null && player.level().dimension().location().equals(packet.dimension()))
				ClientFootprintManager.markChunkDirty(BlockPos.of(packet.pos()));
		});
	}
}
