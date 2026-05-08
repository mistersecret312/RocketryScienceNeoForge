package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.orbits.OrbitType;
import net.mistersecret312.rocketry_science.orbit_requirements.OrbitRequirement;
import net.mistersecret312.rocketry_science.orbit_types.DefaultOrbitConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class OrbitTypeInit
{
	public static final ResourceKey<Registry<OrbitType<?>>> REGISTRY_KEY =
			ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
					"orbit_types"));
	public static final Registry<OrbitType<?>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).sync(true).create();
	public static final DeferredRegister<OrbitType<?>> TYPES = DeferredRegister.create(REGISTRY, RocketryScience.MODID);

	public static final DeferredHolder<OrbitType<?>, OrbitType<?>> DEFAULT = TYPES.register("default",
			() -> new OrbitType<>(DefaultOrbitConfig.CODEC));

	public static void register(IEventBus bus)
	{
		TYPES.register(bus);
	}
}
