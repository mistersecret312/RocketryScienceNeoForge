package net.mistersecret312.rocketry_science;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.mistersecret312.rocketry_science.client.renderer.block.LaunchControllerRenderer;
import net.mistersecret312.rocketry_science.client.renderer.block.RocketAssemblerRenderer;
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
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import java.util.Map;

@Mod(RocketryScience.MODID)
public class RocketryScience
{
	public static final String MODID = "rocketry_science";

	public RocketryScience(IEventBus modEventBus, ModContainer modContainer)
	{
		ItemInit.register(modEventBus);
		ItemTabInit.register(modEventBus);

		BlockInit.register(modEventBus);
		BlockEntityInit.register(modEventBus);

		OrbitTypeInit.register(modEventBus);
		OrbitRequirementInit.register(modEventBus);

		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::registerRegistry);
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
	}

	private void commonSetup(final FMLCommonSetupEvent event)
	{

	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event)
	{
		Registry<CelestialBody> registry = event.getServer().registryAccess().registryOrThrow(CelestialBody.REGISTRY_KEY);

		for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : registry.entrySet())
		{
			if(entry != null)
			{
				System.out.println("Key : " + entry.getKey().location());
			}
		}
	}

	@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents
	{
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event)
		{

		}

		@SubscribeEvent
		public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
		{
			event.registerBlockEntityRenderer(BlockEntityInit.ROCKET_ASSEMBLER.get(), RocketAssemblerRenderer::new);
			event.registerBlockEntityRenderer(BlockEntityInit.LAUNCH_CONTROLLER.get(), LaunchControllerRenderer::new);
		}
	}
}
