package net.mistersecret312.rocketry_science.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.mistersecret312.rocketry_science.block_entities.LaunchControllerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.init.BlockInit;
import net.mistersecret312.rocketry_science.init.MenuInit;
import org.jetbrains.annotations.Nullable;

public class LaunchControllerMenu extends AbstractContainerMenu
{
	public LaunchControllerBlockEntity blockEntity;
	public RocketPad pad;
	public Level level;

	public LaunchControllerMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer)
	{
		this(containerId, inventory, inventory.player.level().getBlockEntity(buffer.readBlockPos()),
				new RocketPad(buffer.readBlockPos(), buffer.readResourceKey(Registries.DIMENSION)));
	}

	public LaunchControllerMenu(int containerId, Inventory inventory, BlockEntity blockEntity, RocketPad pad)
	{
		super(MenuInit.LAUNCH_CONTROLLER.get(), containerId);
		this.blockEntity = (LaunchControllerBlockEntity) blockEntity;
		this.level = inventory.player.level();
		this.pad = pad;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player)
	{
		return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockInit.LAUNCH_CONTROLLER.get());
	}
}
