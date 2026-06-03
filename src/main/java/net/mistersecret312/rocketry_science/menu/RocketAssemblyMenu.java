package net.mistersecret312.rocketry_science.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.init.BlockInit;
import net.mistersecret312.rocketry_science.init.MenuInit;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class RocketAssemblyMenu extends AbstractContainerMenu
{
	public RocketAssemblerBlockEntity blockEntity;
	public RocketPad pad;
	public Level level;
	public RocketAssemblyMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer)
	{
		this(containerId, inventory, inventory.player.level().getBlockEntity(buffer.readBlockPos()),
				new RocketPad(buffer.readBlockPos(), buffer.readResourceKey(Registries.DIMENSION)));
	}

	public RocketAssemblyMenu(int containerId, Inventory inventory, BlockEntity blockEntity, RocketPad pad)
	{
		super(MenuInit.ROCKET_ASSEMBLY.get(), containerId);
		this.blockEntity = (RocketAssemblerBlockEntity) blockEntity;
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
		return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockInit.ROCKET_ASSEMBLER.get());
	}
}
