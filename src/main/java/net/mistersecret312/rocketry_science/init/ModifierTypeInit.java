package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.orbits.OrbitType;
import net.mistersecret312.rocketry_science.environment.modifiers.ModifierType;
import net.mistersecret312.rocketry_science.modifiers.GravityModifierConfig;
import net.mistersecret312.rocketry_science.modifiers.PressureModifierConfig;
import net.mistersecret312.rocketry_science.modifiers.RadiationModifierConfig;
import net.mistersecret312.rocketry_science.modifiers.TemperatureModifierConfig;
import net.mistersecret312.rocketry_science.orbit_types.DefaultOrbitConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ModifierTypeInit
{
	public static final ResourceKey<Registry<ModifierType<?>>> REGISTRY_KEY =
			ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
					"planet_modifier_type"));
	public static final Registry<ModifierType<?>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).sync(true).create();
	public static final DeferredRegister<ModifierType<?>> TYPES = DeferredRegister.create(REGISTRY, RocketryScience.MODID);

	public static final DeferredHolder<ModifierType<?>, ModifierType<?>> TEMPERATURE = TYPES.register("temperature",
			() -> new ModifierType<>(TemperatureModifierConfig.CODEC));
	public static final DeferredHolder<ModifierType<?>, ModifierType<?>> PRESSURE = TYPES.register("pressure",
			() -> new ModifierType<>(PressureModifierConfig.CODEC));
	public static final DeferredHolder<ModifierType<?>, ModifierType<?>> RADIATION = TYPES.register("radiation",
			() -> new ModifierType<>(RadiationModifierConfig.CODEC));
	public static final DeferredHolder<ModifierType<?>, ModifierType<?>> GRAVITY = TYPES.register("gravity",
			() -> new ModifierType<>(GravityModifierConfig.CODEC));

	public static void register(IEventBus bus)
	{
		TYPES.register(bus);
	}
}
