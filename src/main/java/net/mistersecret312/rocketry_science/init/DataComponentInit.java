package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.util.RocketFuel;
import net.mistersecret312.rocketry_science.util.RocketMaterial;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class DataComponentInit
{
	public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.createDataComponents(
			Registries.DATA_COMPONENT_TYPE, RocketryScience.MODID);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<RocketMaterial>> ROCKET_MATERIAL =
			register("rocket_material", builder -> builder.persistent(RocketMaterial.CODEC));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<RocketFuel>> ROCKET_FUEL =
			register("rocket_fuel", builder -> builder.persistent(RocketFuel.CODEC));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> UUID =
			register("uuid", builder -> builder.persistent(UUIDUtil.CODEC));

	private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator)
	{
		return DATA_COMPONENTS.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
	}

	public static void register(IEventBus eventBus)
	{
		DATA_COMPONENTS.register(eventBus);
	}

}
