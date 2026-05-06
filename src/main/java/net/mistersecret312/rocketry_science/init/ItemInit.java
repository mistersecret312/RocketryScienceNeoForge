package net.mistersecret312.rocketry_science.init;

import net.minecraft.world.item.Item;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemInit
{
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RocketryScience.MODID);

	public static final DeferredItem<Item> CHROMIUM_INGOT = ITEMS.registerSimpleItem("chromium_ingot");
	public static final DeferredItem<Item> STAINLESS_STEEL_INGOT = ITEMS.registerSimpleItem("stainless_steel_ingot");

	public static final DeferredItem<Item> STAINLESS_STEEL_COMBUSTION_CHAMBER = ITEMS.registerSimpleItem("stainless_steel_combustion_chamber");
	public static final DeferredItem<Item> STAINLESS_STEEL_TURBOPUMP = ITEMS.registerSimpleItem("stainless_steel_turbopump");

	public static final DeferredItem<Item> LIQUID_HYDROGEN_BUCKET = ITEMS.registerSimpleItem("liquid_hydrogen_bucket", new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> LIQUID_OXYGEN_BUCKET = ITEMS.registerSimpleItem("liquid_oxygen_bucket", new Item.Properties().stacksTo(1));

	public static void register(IEventBus bus)
	{
		ITEMS.register(bus);
	}
}
