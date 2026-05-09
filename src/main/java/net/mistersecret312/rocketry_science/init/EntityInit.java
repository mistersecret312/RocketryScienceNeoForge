package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EntityInit
{
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
			DeferredRegister.create(Registries.ENTITY_TYPE, RocketryScience.MODID);

	public static final DeferredHolder<EntityType<?>, EntityType<RocketEntity>> ROCKET =
			ENTITY_TYPES.register("rocket",
					() -> EntityType.Builder.<RocketEntity>of(RocketEntity::new, MobCategory.MISC)
											.sized(0.75f, 0.75f)
											.build(ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
													"rocket").toString()));

	public static void register(IEventBus bus)
	{
		ENTITY_TYPES.register(bus);
	}

}
