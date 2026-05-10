package net.mistersecret312.rocketry_science.init;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.items.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemInit
{
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RocketryScience.MODID);

	public static final DeferredItem<Item> CHROMIUM_INGOT = ITEMS.registerSimpleItem("chromium_ingot");
	public static final DeferredItem<Item> STAINLESS_STEEL_INGOT = ITEMS.registerSimpleItem("stainless_steel_ingot");

	public static final DeferredItem<CombustionChamberItem> STAINLESS_STEEL_COMBUSTION_CHAMBER = ITEMS.register("stainless_steel_combustion_chamber",
			() -> new CombustionChamberItem(new Item.Properties().stacksTo(64)));
	public static final DeferredItem<TurboPumpItem> STAINLESS_STEEL_TURBOPUMP = ITEMS.register("stainless_steel_turbopump",
			() -> new TurboPumpItem(new Item.Properties().stacksTo(64)));

	public static final DeferredItem<PadLinkingItem> PAD_LINKING = ITEMS.register("pad_linking",
			() -> new PadLinkingItem(new Item.Properties().stacksTo(1)));

	public static final DeferredItem<BucketItem> LIQUID_HYDROGEN_BUCKET = ITEMS.register("liquid_hydrogen_bucket",
			() -> new BucketItem(FluidInit.SOURCE_CRYOGENIC_HYDROGEN.get(), new Item.Properties().stacksTo(1)));
	public static final DeferredItem<BucketItem> LIQUID_OXYGEN_BUCKET = ITEMS.register("liquid_oxygen_bucket",
			() -> new BucketItem(FluidInit.SOURCE_CRYOGENIC_OXYGEN.get(), new Item.Properties().stacksTo(1)));

	public static final DeferredItem<Item> LAUNCH_CONTROLLER = ITEMS.register("launch_controller",
			() -> new LaunchControllerBlockItem(BlockInit.LAUNCH_CONTROLLER.get(), new Item.Properties()));
	public static final DeferredItem<Item> ROCKET_ASSEMBLER = ITEMS.register("rocket_assembler",
			() -> new RocketAssemblerBlockItem(BlockInit.ROCKET_ASSEMBLER.get(), new Item.Properties()));

	public static void register(IEventBus bus)
	{
		ITEMS.register(bus);
	}
}
