package net.mistersecret312.rocketry_science.compatability.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum RocketEntityComponent implements IEntityComponentProvider
{
	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig plugin)
	{
		Entity entity = accessor.getEntity();
		if(entity instanceof RocketEntity rocketEntity)
		{
//			Player player = Minecraft.getInstance().player;
//			if(player == null)
//				return;
//
//			BlockData data = rocketEntity.getTargetedBlockData(player);
//			if(data != null)
//			{
//				BlockState state = data.getBlockState();
//				MutableComponent component = state.getBlock().getName();
//				tooltip.add(component);
//			}
		}
	}

	@Override
	public ResourceLocation getUid()
	{
		return ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "rocket");
	}
}
