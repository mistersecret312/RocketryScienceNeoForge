package net.mistersecret312.rocketry_science;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.mistersecret312.rocketry_science.client.model.PlumeModel;
import net.mistersecret312.rocketry_science.client.renderer.block.FuelTankRenderer;
import net.mistersecret312.rocketry_science.client.renderer.block.LaunchControllerRenderer;
import net.mistersecret312.rocketry_science.client.renderer.block.RocketAssemblerRenderer;
import net.mistersecret312.rocketry_science.client.renderer.block.SeparatorRenderer;
import net.mistersecret312.rocketry_science.client.renderer.entity.RocketRenderer;
import net.mistersecret312.rocketry_science.client.screen.CombustionChamberScreen;
import net.mistersecret312.rocketry_science.client.vessel.BlockDataRendererRegistry;
import net.mistersecret312.rocketry_science.client.vessel.block_data.BlockDataRenderer;
import net.mistersecret312.rocketry_science.client.vessel.block_data.FuelTankDataRenderer;
import net.mistersecret312.rocketry_science.client.vessel.block_data.RocketEngineDataRenderer;
import net.mistersecret312.rocketry_science.client.vessel.block_data.SeparatorDataRenderer;
import net.mistersecret312.rocketry_science.data.SpaceCraftData;
import net.mistersecret312.rocketry_science.data.orbiting_objects.SpaceCraft;
import net.mistersecret312.rocketry_science.data.orbits.ArtificialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.ConfiguredOrbit;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.init.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import java.util.Map;
import java.util.UUID;

@Mod(RocketryScience.MODID)
public class RocketryScience
{
	public static final String MODID = "rocketry_science";

	public static final TagKey<Fluid> OXYGEN = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(MODID, "liquid_oxygen"));
	public static final TagKey<Fluid> HYDROGEN = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(MODID, "liquid_hydrogen"));

	public RocketryScience(IEventBus modEventBus, ModContainer modContainer)
	{
		DataComponentInit.register(modEventBus);
		MenuInit.register(modEventBus);

		ItemInit.register(modEventBus);
		ItemTabInit.register(modEventBus);

		BlockInit.register(modEventBus);
		BlockEntityInit.register(modEventBus);

		FluidTypeInit.register(modEventBus);
		FluidInit.register(modEventBus);

		EntityInit.register(modEventBus);
		EntityDataSerializersInit.register(modEventBus);

		OrbitTypeInit.register(modEventBus);
		OrbitRequirementInit.register(modEventBus);
		BlockDataInit.register(modEventBus);

		modEventBus.addListener(NetworkInit::registerPackets);
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::registerRegistry);
		modEventBus.addListener(this::registerCapabilities);
		NeoForge.EVENT_BUS.register(this);

		modEventBus.addListener((DataPackRegistryEvent.NewRegistry event) ->
			{
				event.dataPackRegistry(CelestialBody.REGISTRY_KEY, CelestialBody.CODEC, CelestialBody.CODEC);
			});
	}

	public void registerRegistry(NewRegistryEvent event)
	{
		event.register(OrbitRequirementInit.REGISTRY);
		event.register(OrbitTypeInit.REGISTRY);
		event.register(BlockDataInit.REGISTRY);
	}

	private void commonSetup(final FMLCommonSetupEvent event)
	{

	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event)
	{
		Registry<CelestialBody> registry = event.getServer().registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY);
		ResourceKey<CelestialBody> THE_EARTH = ResourceKey.create(CelestialBody.REGISTRY_KEY,
				ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "earth"));

		if(true)
			return;

		CelestialBody earth = registry.get(THE_EARTH);
		if(earth != null)
		{
			SpaceCraftData data = SpaceCraftData.get(event.getServer());
			data.addFreshSpaceCraft(UUID.randomUUID());

			SpaceCraft craft = data.addFreshSpaceCraft(UUID.randomUUID());
			ConfiguredOrbit configuredOrbit = earth.getSupportedOrbits().getFirst();
			ArtificialOrbit orbit = new ArtificialOrbit(THE_EARTH, craft, configuredOrbit);
			data.addOrbitToSpacecraft(craft.getUUID(), orbit);
		}

		for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
		{
			if(entry != null)
			{
				System.out.println("Key : " + entry.getKey().location());
			}
		}
	}

	public void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
				Capabilities.FluidHandler.BLOCK,
				BlockEntityInit.FUEL_TANK.get(),
				(be, context) ->
					{
						if(be.fluidCapability == null)
							be.refreshCapability();
						return be.fluidCapability;
					});

		event.registerBlockEntity(
				Capabilities.FluidHandler.BLOCK,
				BlockEntityInit.ROCKET_ENGINE.get(),
				(be, context) ->
					{
						if(be.fluidHolder == null)
							be.refreshCapability();
						return be.fluidHolder;
					});
	}

	@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents
	{
		public static PlumeModel plumeModel;

		@SubscribeEvent
		public static void bakeModels(EntityRenderersEvent.RegisterLayerDefinitions event)
		{
			event.registerLayerDefinition(PlumeModel.LAYER_LOCATION, PlumeModel::createBodyLayer);
		}

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event)
		{
			BlockDataRendererRegistry.register(BlockDataInit.BASE.get(), new BlockDataRenderer());
			BlockDataRendererRegistry.register(BlockDataInit.FUEL_TANK.get(), new FuelTankDataRenderer());
			BlockDataRendererRegistry.register(BlockDataInit.ROCKET_ENGINE.get(), new RocketEngineDataRenderer());
			BlockDataRendererRegistry.register(BlockDataInit.SEPARATOR.get(), new SeparatorDataRenderer());
		}

		@SubscribeEvent
		public static void registerScreens(RegisterMenuScreensEvent event) {
			event.register(MenuInit.COMBUSTION_CHAMBER.get(), CombustionChamberScreen::new);
		}

		@SubscribeEvent
		public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
		{
			event.registerEntityRenderer(EntityInit.ROCKET.get(), context ->
				{
					plumeModel = new PlumeModel(context.bakeLayer(PlumeModel.LAYER_LOCATION));
					return new RocketRenderer(context);
				});

			event.registerBlockEntityRenderer(BlockEntityInit.FUEL_TANK.get(), FuelTankRenderer::new);
			event.registerBlockEntityRenderer(BlockEntityInit.SEPARATOR.get(), SeparatorRenderer::new);

			event.registerBlockEntityRenderer(BlockEntityInit.ROCKET_ASSEMBLER.get(), RocketAssemblerRenderer::new);
			event.registerBlockEntityRenderer(BlockEntityInit.LAUNCH_CONTROLLER.get(), LaunchControllerRenderer::new);
		}
	}
}
