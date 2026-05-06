package net.mistersecret312.rocketry_science.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.blocks.RocketAssemblerBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockInit
{
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RocketryScience.MODID);

	public static final DeferredBlock<Block> LAUNCH_CONTROLLER = registerBlock("launch_controller", () -> new Block(
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

	public static final DeferredBlock<RocketAssemblerBlock> ROCKET_ASSEMBLER = registerBlock("rocket_assembler", () -> new RocketAssemblerBlock(
			BlockBehaviour.Properties.of().noOcclusion()));

	private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block)
	{
		DeferredBlock<T> toReturn = BLOCKS.register(name, block);
		registerBlockItem(name, toReturn);
		return toReturn;
	}

	private static <T extends Block> DeferredHolder<Item, BlockItem> registerBlockItem(String name, DeferredBlock<T> block)
	{
		return ItemInit.ITEMS.register(name, () -> new BlockItem(block.get(),
				new Item.Properties()));
	}

	public static void register(IEventBus bus)
	{
		BLOCKS.register(bus);
	}
}
