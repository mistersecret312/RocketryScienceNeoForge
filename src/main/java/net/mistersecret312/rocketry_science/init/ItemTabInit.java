package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemTabInit
{
	public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
			RocketryScience.MODID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ROCKETRY_SCIENCE = TABS.register(
			"rocketry_science",
			() -> CreativeModeTab.builder().icon(() -> new ItemStack(ItemInit.STAINLESS_STEEL_COMBUSTION_CHAMBER.get()))
								 .title(Component.translatable("tabs.rocketry_science"))
								 .displayItems((parameters, output) ->
									 {
										 output.accept(ItemInit.CHROMIUM_INGOT);

										 output.accept(ItemInit.STAINLESS_STEEL_COMBUSTION_CHAMBER);
										 output.accept(ItemInit.STAINLESS_STEEL_TURBOPUMP);

										 output.accept(ItemInit.LIQUID_HYDROGEN_BUCKET);
										 output.accept(ItemInit.LIQUID_OXYGEN_BUCKET);
									 }).build());

	public static void register(IEventBus bus)
	{
		TABS.register(bus);
	}
}
