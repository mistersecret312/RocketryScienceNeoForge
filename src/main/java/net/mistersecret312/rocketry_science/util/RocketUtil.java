package net.mistersecret312.rocketry_science.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;

public class RocketUtil
{
	public static ItemStack getPickedBlock(RocketEntity rocketEntity)
	{
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;

		if(player == null)
			return ItemStack.EMPTY;

		BlockHitResult result = rocketEntity.getTargetedBlockHit(player);
		BlockData data = rocketEntity.getTargetedBlockData(player);

		if(data == null || result == null)
			return ItemStack.EMPTY;

		ItemStack copy = data.getBlockState().getCloneItemStack(result, player.level(),
				data.pos.offset(rocketEntity.blockPosition()), player).copy();
		if(!copy.isEmpty())
			return copy;
		return ItemStack.EMPTY;
	}

	private static BlockData getBlockDataAt(RocketEntity rocket, BlockPos relative) {
		for (Stage stage : rocket.getRocket().getStages()) {
			if (stage.blocks.containsKey(relative)) {
				return stage.blocks.get(relative);
			}
		}
		return null;
	}
}
