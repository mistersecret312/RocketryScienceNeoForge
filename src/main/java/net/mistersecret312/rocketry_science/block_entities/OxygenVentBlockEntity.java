package net.mistersecret312.rocketry_science.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.rocketry_science.data.room.IOxygenNode;
import net.mistersecret312.rocketry_science.environment.EnvironmentData;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import net.mistersecret312.rocketry_science.util.EnvironmentUtil;

public class OxygenVentBlockEntity extends BlockEntity implements IOxygenNode
{
	public OxygenVentBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.OXYGEN_VENT.get(), pos, blockState);
	}

	@Override
	public float getOxygenOutput()
	{
		return 20;
	}

	@Override
	public int getVolumeBonus()
	{
		return 10000;
	}

	@Override
	public int getBaseVolume()
	{
		return 4000;
	}

	@Override
	public int getScanSpeed()
	{
		return 200;
	}

	@Override
	public boolean isActive()
	{
		return true;
	}

	@Override
	public BlockPos getPos()
	{
		return worldPosition;
	}

	@Override
	public EnvironmentData getTargetEnvironment()
	{
		EnvironmentData data = new EnvironmentData(EnvironmentUtil.EARTH);
		data.pressure = 1;
		data.temperature = new EnvironmentData.TemperatureGradient(296, 296);
		return data;
	}
}
