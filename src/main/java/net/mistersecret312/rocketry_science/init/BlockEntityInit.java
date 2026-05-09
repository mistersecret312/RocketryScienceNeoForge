package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.LaunchControllerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.SeparatorBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.fuel_tank.FuelTankBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.multiblock.LaunchTowerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.rocket_engine.LiquidRocketEngineBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockEntityInit
{
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, RocketryScience.MODID);

	public static final Supplier<BlockEntityType<RocketPadBlockEntity>> ROCKET_PAD =
			BLOCK_ENTITIES.register("rocket_pad",
					() -> BlockEntityType.Builder.of(RocketPadBlockEntity::new,
							BlockInit.ROCKET_PAD.get()).build(null));
	public static final Supplier<BlockEntityType<LaunchTowerBlockEntity>> LAUNCH_TOWER =
			BLOCK_ENTITIES.register("launch_tower",
					() -> BlockEntityType.Builder.of(LaunchTowerBlockEntity::new,
							BlockInit.LAUNCH_TOWER.get()).build(null));

	public static final Supplier<BlockEntityType<RocketAssemblerBlockEntity>> ROCKET_ASSEMBLER =
			BLOCK_ENTITIES.register("rocket_assembler",
					() -> BlockEntityType.Builder.of(RocketAssemblerBlockEntity::new,
							BlockInit.ROCKET_ASSEMBLER.get()).build(null));
	public static final Supplier<BlockEntityType<LaunchControllerBlockEntity>> LAUNCH_CONTROLLER =
			BLOCK_ENTITIES.register("launch_controller",
					() -> BlockEntityType.Builder.of(LaunchControllerBlockEntity::new,
							BlockInit.LAUNCH_CONTROLLER.get()).build(null));


	public static final Supplier<BlockEntityType<FuelTankBlockEntity>> FUEL_TANK =
			BLOCK_ENTITIES.register("fuel_tank",
					() -> BlockEntityType.Builder.of(FuelTankBlockEntity::new,
							BlockInit.FUEL_TANK.get()).build(null));
	public static final Supplier<BlockEntityType<LiquidRocketEngineBlockEntity>> ROCKET_ENGINE =
			BLOCK_ENTITIES.register("rocket_engine",
					() -> BlockEntityType.Builder.of(LiquidRocketEngineBlockEntity::new,
							BlockInit.STEEL_COMBUSTION_CHAMBER.get()).build(null));

	public static final Supplier<BlockEntityType<SeparatorBlockEntity>> SEPARATOR =
			BLOCK_ENTITIES.register("separator",
					() -> BlockEntityType.Builder.of(SeparatorBlockEntity::new,
							BlockInit.SEPARATOR.get()).build(null));

	public static void register(IEventBus bus)
	{
		BLOCK_ENTITIES.register(bus);
	}
}
