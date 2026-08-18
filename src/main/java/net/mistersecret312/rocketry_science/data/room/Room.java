package net.mistersecret312.rocketry_science.data.room;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.rocketry_science.network.packets.ClientBoundRoomUpdatePacket;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class Room
{
	private final UUID uuid;
	private final LongOpenHashSet interiorSet;
	private final LongOpenHashSet wallsSet;
	private final LongOpenHashSet oxygenNodes;

	private float currentOxygen;
	private float volume;
	private double targetAtmosphere = 1;
	private double targetTemperature = 296;
	private boolean isGhost = false;

	public Room(UUID uuid,
				LongOpenHashSet interiorSet, LongOpenHashSet wallsSet,
				LongOpenHashSet oxygenNodes)
	{
		this.uuid = uuid;
		this.interiorSet = interiorSet;
		this.wallsSet = wallsSet;
		this.oxygenNodes = oxygenNodes;

		this.currentOxygen = 0;
		this.volume = interiorSet.size() * 100;
	}

	public void tickEconomy(ServerLevel level)
	{
		if(isGhost)
			return;

		float netChange = 0;

		LongIterator it = oxygenNodes.iterator();
		while (it.hasNext())
		{
			long posLong = it.nextLong();
			BlockPos pos = BlockPos.of(posLong);

			if (!level.isLoaded(pos))
				continue;

			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof IOxygenNode node && node.isActive())
			{
				netChange += node.getOxygenOutput();
				if(level.getGameTime() % 8 == 0 && currentOxygen+netChange <= volume)
					level.playSound(null, pos, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.AMBIENT,
							0.15f, 0.7f);
			}
			else it.remove();
		}

		this.setCurrentOxygen(level, Math.clamp(this.currentOxygen + netChange, 0, this.volume));
	}

	public boolean isGhost()
	{
		return isGhost;
	}
	public void setGhost(boolean ghost)
	{
		this.isGhost = ghost;
	}

	public LongOpenHashSet getInteriorSet()
	{
		return interiorSet;
	}

	public LongOpenHashSet getWallsSet()
	{
		return wallsSet;
	}

	public LongOpenHashSet getOxygenNodes()
	{
		return oxygenNodes;
	}

	public float getCurrentOxygen()
	{
		return currentOxygen;
	}

	public void setCurrentOxygen(Level level, float currentOxygen)
	{
		this.currentOxygen = currentOxygen;
		if(level != null && !level.isClientSide())
			PacketDistributor.sendToPlayersInDimension((ServerLevel) level,
					new ClientBoundRoomUpdatePacket(uuid, currentOxygen, volume, targetAtmosphere, targetTemperature));
	}

	public float getVolume()
	{
		return volume;
	}

	public void setVolume(float volume)
	{
		this.volume = volume;
	}

	public UUID getUUID()
	{
		return uuid;
	}

	public double getFilledPercentage()
	{
		return currentOxygen/volume;
	}

	public AABB calculateBounds()
	{
		if (interiorSet.isEmpty())
			return new AABB(0, 0, 0, 0, 0, 0);

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;

		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;

		for (long posLong : interiorSet)
		{
			int x = BlockPos.getX(posLong);
			int y = BlockPos.getY(posLong);
			int z = BlockPos.getZ(posLong);

			if (x < minX) minX = x;
			if (y < minY) minY = y;
			if (z < minZ) minZ = z;

			if (x > maxX) maxX = x;
			if (y > maxY) maxY = y;
			if (z > maxZ) maxZ = z;
		}

		return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
	}

	public double getTargetAtmosphere()
	{
		return targetAtmosphere;
	}

	public void setTargetAtmosphere(Level level, double targetAtmosphere)
	{
		this.targetAtmosphere = targetAtmosphere;
		if(level != null && !level.isClientSide())
			PacketDistributor.sendToPlayersInDimension((ServerLevel) level,
					new ClientBoundRoomUpdatePacket(uuid, currentOxygen, volume, targetAtmosphere, targetTemperature));

	}

	public double getTargetTemperature()
	{
		return targetTemperature;
	}

	public void setTargetTemperature(Level level, double targetTemperature)
	{
		this.targetTemperature = targetTemperature;
		if(level != null && !level.isClientSide())
			PacketDistributor.sendToPlayersInDimension((ServerLevel) level,
					new ClientBoundRoomUpdatePacket(uuid, currentOxygen, volume, targetAtmosphere, targetTemperature));

	}
}