package net.mistersecret312.rocketry_science.init;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.VesselData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class EntityDataSerializersInit
{
	public static final DeferredRegister<EntityDataSerializer<?>> SERIALIZERS = DeferredRegister.create(
			NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, RocketryScience.MODID);

	public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Rocket>> ROCKET =
			SERIALIZERS.register("rocket", () -> EntityDataSerializer.forValueType(Rocket.STREAM_CODEC));

	public static void register(IEventBus bus)
	{
		SERIALIZERS.register(bus);
	}

}
