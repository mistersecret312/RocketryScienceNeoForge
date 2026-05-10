package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.fluid.LiquidMaterialFluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Vector3f;

public class FluidTypeInit
{
	public static final ResourceLocation CRYOGENIC_STILL = ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still");
	public static final ResourceLocation CRYOGENIC_FLOW = ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow");
	public static final ResourceLocation CRYOGENIC_OVERLAY = ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_overlay");
	public static final ResourceLocation CRYOGENIC_UNDERWATER = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/underwater.png");

	public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, RocketryScience.MODID);

	public static final DeferredHolder<FluidType, FluidType> CRYOGENIC_HYDROGEN_TYPE = registerFluidType("cryogenic_hydrogen",
			new LiquidMaterialFluid(CRYOGENIC_STILL, CRYOGENIC_FLOW, CRYOGENIC_OVERLAY, CRYOGENIC_UNDERWATER, 0xFF99CCFF,
					new Vector3f(153f / 255f, 204f / 255f, 1.0f),
					FluidType.Properties.create().lightLevel(0).viscosity(8).density(15).canExtinguish(false)));

	public static final DeferredHolder<FluidType, FluidType> CRYOGENIC_OXYGEN_TYPE = registerFluidType("cryogenic_oxygen",
			new LiquidMaterialFluid(CRYOGENIC_STILL, CRYOGENIC_FLOW, CRYOGENIC_OVERLAY, CRYOGENIC_UNDERWATER,0xFFFF6666,
					new Vector3f(1.0f, 102f / 255f, 102f / 255f),
					FluidType.Properties.create().lightLevel(0).viscosity(8).density(15).canExtinguish(false)));

	private static DeferredHolder<FluidType, FluidType> registerFluidType(String name, FluidType fluidType) {
		return FLUID_TYPES.register(name, () -> fluidType);
	}

	public static final BaseFlowingFluid.Properties CRYOGENIC_HYDROGEN_PROPERTIES = new BaseFlowingFluid.Properties(
			FluidTypeInit.CRYOGENIC_HYDROGEN_TYPE, FluidInit.SOURCE_CRYOGENIC_HYDROGEN, FluidInit.FLOWING_CRYOGENIC_HYDROGEN)
																							.slopeFindDistance(4).levelDecreasePerBlock(1).block(BlockInit.CRYOGENIC_HYDROGEN)
																							.bucket(ItemInit.LIQUID_HYDROGEN_BUCKET);

	public static final BaseFlowingFluid.Properties CRYOGENIC_OXYGEN_PROPERTIES = new BaseFlowingFluid.Properties(
			FluidTypeInit.CRYOGENIC_OXYGEN_TYPE, FluidInit.SOURCE_CRYOGENIC_OXYGEN, FluidInit.FLOWING_CRYOGENIC_OXYGEN)
																						  .slopeFindDistance(4).levelDecreasePerBlock(1).block(BlockInit.CRYOGENIC_OXYGEN)
																						  .bucket(ItemInit.LIQUID_OXYGEN_BUCKET);



	public static void register(IEventBus bus)
	{
		FLUID_TYPES.register(bus);
	}
}
