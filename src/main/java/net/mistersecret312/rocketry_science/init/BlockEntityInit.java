package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockEntityInit
{
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, RocketryScience.MODID);

	public static final Supplier<BlockEntityType<RocketAssemblerBlockEntity>> ROCKET_ASSEMBLER =
			BLOCK_ENTITIES.register("rocket_assembler",
					() -> BlockEntityType.Builder.of(RocketAssemblerBlockEntity::new,
							BlockInit.ROCKET_ASSEMBLER.get()).build(null));


	public static void register(IEventBus bus)
	{
		BLOCK_ENTITIES.register(bus);
	}
}
