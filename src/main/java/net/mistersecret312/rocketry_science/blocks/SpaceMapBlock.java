package net.mistersecret312.rocketry_science.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.rocketry_science.client.screen.SpaceMapScreen;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.datapack.SolarSystem;
import net.mistersecret312.rocketry_science.util.OrbitUtil;

public class SpaceMapBlock extends Block
{

	public SpaceMapBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
											   BlockHitResult hitResult)
	{
		if(player instanceof LocalPlayer localPlayer)
		{
			CelestialBody body = OrbitUtil.getCelestialBody(localPlayer.level());
			if(body == null)
				return InteractionResult.PASS;

			SolarSystem solarSystem = OrbitUtil.getSystem(body, localPlayer.level());
			if(solarSystem == null)
				return InteractionResult.PASS;

			SpaceMapScreen screen = new SpaceMapScreen(solarSystem, localPlayer.clientLevel);
			Minecraft.getInstance().setScreen(screen);
		}

		return super.useWithoutItem(state, level, pos, player, hitResult);
	}
}
