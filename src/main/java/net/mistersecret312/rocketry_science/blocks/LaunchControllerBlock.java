package net.mistersecret312.rocketry_science.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.rocketry_science.block_entities.LaunchControllerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPadData;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import net.mistersecret312.rocketry_science.menu.LaunchControllerMenu;
import net.mistersecret312.rocketry_science.menu.RocketAssemblyMenu;
import net.mistersecret312.rocketry_science.vessel.VesselState;
import org.jetbrains.annotations.Nullable;

public class LaunchControllerBlock extends BaseEntityBlock
{
	public static final MapCodec<LaunchControllerBlock> CODEC = simpleCodec(LaunchControllerBlock::new);

	public LaunchControllerBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
											   BlockHitResult hitResult)
	{
		if(!level.isClientSide())
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof LaunchControllerBlockEntity controller)
			{
				if(controller.getPadUUID() == null || level.getServer() == null)
					return InteractionResult.FAIL;

				RocketPadData data = RocketPadData.get(level.getServer());
				RocketPad rocketPad = data.rocketPads.get(controller.getPadUUID());
				if(rocketPad == null)
					return InteractionResult.FAIL;

				MenuProvider containerProvider = new MenuProvider()
				{
					@Override
					public Component getDisplayName()
					{
						return Component.translatable("screen.rocketry_science.launch_controller");
					}

					@Override
					public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
					{
						return new LaunchControllerMenu(windowId, playerInventory, blockEntity, rocketPad);
					}
				};
				if(player instanceof ServerPlayer serverPlayer)
					serverPlayer.openMenu(containerProvider, buff ->
						{
							buff.writeBlockPos(pos);
							buff.writeBlockPos(rocketPad.getPos());
							buff.writeResourceKey(rocketPad.getDimension());
						});
				return InteractionResult.SUCCESS;
			}
			else
			{
				throw new IllegalStateException("Our named container provider is missing!");
			}
		}

		return super.useWithoutItem(state, level, pos, player, hitResult);
	}


	@Override
	protected RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		return BlockEntityInit.LAUNCH_CONTROLLER.get().create(blockPos, blockState);
	}
}
