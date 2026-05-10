package net.mistersecret312.rocketry_science.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FluidInit
{
	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, RocketryScience.MODID);

	public static final DeferredHolder<Fluid, FlowingFluid> SOURCE_CRYOGENIC_HYDROGEN = FLUIDS.register("cryogenic_hydrogen",
			() -> new BaseFlowingFluid.Source(FluidTypeInit.CRYOGENIC_HYDROGEN_PROPERTIES));
	public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CRYOGENIC_HYDROGEN = FLUIDS.register("flowing_hydrogen",
			() -> new BaseFlowingFluid.Flowing(FluidTypeInit.CRYOGENIC_HYDROGEN_PROPERTIES));

	public static final DeferredHolder<Fluid, FlowingFluid> SOURCE_CRYOGENIC_OXYGEN = FLUIDS.register("cryogenic_oxygen",
			() -> new BaseFlowingFluid.Source(FluidTypeInit.CRYOGENIC_OXYGEN_PROPERTIES));
	public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CRYOGENIC_OXYGEN = FLUIDS.register("flowing_cryogenic_oxygen",
			() -> new BaseFlowingFluid.Flowing(FluidTypeInit.CRYOGENIC_OXYGEN_PROPERTIES));

	public static void register(IEventBus bus)
	{
		FLUIDS.register(bus);
	}
}
