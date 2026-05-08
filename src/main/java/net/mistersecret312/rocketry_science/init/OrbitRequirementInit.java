package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.orbit_requirements.OrbitRequirement;
import net.mistersecret312.rocketry_science.orbit_requirements.RequirementType;
import net.mistersecret312.rocketry_science.orbit_requirements.TrueRequirementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class OrbitRequirementInit
{
	public static final ResourceKey<Registry<RequirementType<?>>> REGISTRY_KEY =
			ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
					"orbit_requirement_types"));
	public static final Registry<RequirementType<?>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).sync(true).create();
	public static final DeferredRegister<RequirementType<?>> TYPES = DeferredRegister.create(REGISTRY, RocketryScience.MODID);

	public static final DeferredHolder<RequirementType<?>, RequirementType<?>> TRUE = TYPES.register("true",
			() -> new RequirementType<>(TrueRequirementType.CODEC));

	public static void register(IEventBus bus)
	{
		TYPES.register(bus);
	}
}
