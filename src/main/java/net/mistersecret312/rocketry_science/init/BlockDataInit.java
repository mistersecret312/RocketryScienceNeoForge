package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.orbit_requirements.RequirementType;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.mistersecret312.rocketry_science.vessel.block_data.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class BlockDataInit
{
	public static final ResourceKey<Registry<BlockDataType<?>>> REGISTRY_KEY =
			ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
					"vessel_block_data"));
	public static final Registry<BlockDataType<?>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).sync(true).create();
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockDataType<?>> STREAM_CODEC =
			ByteBufCodecs.registry(REGISTRY_KEY);

	public static final DeferredRegister<BlockDataType<?>> TYPES = DeferredRegister.create(REGISTRY, RocketryScience.MODID);

	public static final List<BiFunction<Stage, BlockPos, BlockData>> DATA_FACTORY = new ArrayList<>();
	public static final HashMap<BlockDataType<?>, ResourceLocation> CLASSES = new HashMap<>();

	public static final DeferredHolder<BlockDataType<?>, BlockDataType<RocketEngineData>> ROCKET_ENGINE
			= registerBlockData(RocketEngineData::new, "rocket_engine");
	public static final DeferredHolder<BlockDataType<?>, BlockDataType<FuelTankData>> FUEL_TANK
			= registerBlockData(FuelTankData::new, "fuel_tank");
	public static final DeferredHolder<BlockDataType<?>, BlockDataType<SeparatorData>> SEPARATOR
			= registerBlockData(SeparatorData::new, "separator");

	public static final DeferredHolder<BlockDataType<?>, BlockDataType<BlockData>> BASE = registerBlockData(BlockData::new, "base");

	public static <T extends BlockData> DeferredHolder<BlockDataType<?>, BlockDataType<T>> registerBlockData(Supplier<T> data, String id)
	{
		BlockDataType<T> type = new BlockDataType<>(data, id);
		DeferredHolder<BlockDataType<?>, BlockDataType<T>> object = TYPES.register(id, () -> type);
		DATA_FACTORY.add(data.get().create());
		CLASSES.put(type, object.getId());
		return object;
	}

	public static void register(IEventBus bus)
	{
		TYPES.register(bus);
	}
}
