package net.mistersecret312.rocketry_science.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.blocks.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockInit
{
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RocketryScience.MODID);

	public static final DeferredBlock<LiquidBlock> CRYOGENIC_HYDROGEN = BLOCKS.register("cryogenic_hydrogen",
			() -> new LiquidBlock(FluidInit.SOURCE_CRYOGENIC_HYDROGEN.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
	public static final DeferredBlock<LiquidBlock> CRYOGENIC_OXYGEN = BLOCKS.register("cryogenic_oxygen",
			() -> new LiquidBlock(FluidInit.SOURCE_CRYOGENIC_OXYGEN.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

	public static final DeferredBlock<LaunchControllerBlock> LAUNCH_CONTROLLER = BLOCKS.register("launch_controller",
			() -> new LaunchControllerBlock(BlockBehaviour.Properties.of().noOcclusion()));

	public static final DeferredBlock<RocketAssemblerBlock> ROCKET_ASSEMBLER = BLOCKS.register("rocket_assembler",
			() -> new RocketAssemblerBlock(BlockBehaviour.Properties.of().noOcclusion()));

	public static final DeferredBlock<RocketPadBlock> ROCKET_PAD = registerBlock("rocket_pad",
			() -> new RocketPadBlock(BlockBehaviour.Properties.of()));
	public static final DeferredBlock<LaunchTowerBlock> LAUNCH_TOWER = registerBlock("launch_tower",
			() -> new LaunchTowerBlock(BlockBehaviour.Properties.of().noOcclusion()));

	public static final DeferredBlock<FuelTankBlock> FUEL_TANK = registerBlock("fuel_tank",
			() -> new FuelTankBlock(BlockBehaviour.Properties.of().noOcclusion(), 2000));

	public static final DeferredBlock<CombustionChamberBlock> STEEL_COMBUSTION_CHAMBER = registerBlock("steel_combustion_chamber",
			() -> new CombustionChamberBlock(BlockBehaviour.Properties.of().noOcclusion().strength(15).explosionResistance(15).sound(
					SoundType.COPPER)));
	public static final DeferredBlock<NozzleBlock> STEEL_NOZZLE_ATMOPSHERE = registerBlock("steel_nozzle_atmosphere",
			() -> new NozzleBlock(BlockBehaviour.Properties.of().noOcclusion().explosionResistance(10).explosionResistance(10).sound(SoundType.COPPER),
					false, true));
	public static final DeferredBlock<Block> STEEL_ROCKET_ENGINE_STUB = registerBlock("steel_rocket_engine_stub",
			() -> new Block(BlockBehaviour.Properties.of().noOcclusion()));

	public static final DeferredBlock<Block> SEPARATOR = registerBlock("separator",
			() -> new SeparatorBlock(BlockBehaviour.Properties.of().noOcclusion()));

	public static final DeferredBlock<Block> LUNAR_REGOLITH = registerBlock("lunar_regolith",
			() -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)));


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
