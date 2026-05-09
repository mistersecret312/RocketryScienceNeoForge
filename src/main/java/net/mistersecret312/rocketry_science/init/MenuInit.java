package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.menus.CombustionChamberMenu;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuInit
{
	public static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(Registries.MENU, RocketryScience.MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<CombustionChamberMenu>> COMBUSTION_CHAMBER = registerMenuType(
			CombustionChamberMenu::new, "combustion_chamber");

	private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(
			IContainerFactory<T> factory, String name)
	{
		return TYPES.register(name, () -> IMenuTypeExtension.create(factory));
	}


	public static void register(IEventBus bus)
	{
		TYPES.register(bus);
	}
}
