package net.mistersecret312.rocketry_science.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.rocketry_science.data.room.IOxygenNode;
import net.mistersecret312.rocketry_science.environment.EnvironmentData;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import net.mistersecret312.rocketry_science.util.EnvironmentUtil;

public class SolarPanelBlockEntity extends BlockEntity
{
	public SolarPanelBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.SOLAR_PANEL.get(), pos, blockState);
	}
}
