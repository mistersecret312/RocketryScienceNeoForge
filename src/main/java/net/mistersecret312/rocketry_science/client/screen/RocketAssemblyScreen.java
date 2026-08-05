package net.mistersecret312.rocketry_science.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.client.screen.widgets.WidgetAssembleRocket;
import net.mistersecret312.rocketry_science.client.screen.widgets.WidgetStageChange;
import net.mistersecret312.rocketry_science.data.orbits.ConfiguredOrbit;
import net.mistersecret312.rocketry_science.data.orbits.OrbitConfig;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.menu.RocketAssemblyMenu;
import net.mistersecret312.rocketry_science.network.packets.ServerBoundRequestRocketEntityPacket;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.mistersecret312.rocketry_science.util.OrbitalMath;
import net.mistersecret312.rocketry_science.util.RocketFuel;
import net.mistersecret312.rocketry_science.util.Units;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.mistersecret312.rocketry_science.vessel.block_data.FuelTankData;
import net.mistersecret312.rocketry_science.vessel.block_data.RocketEngineData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.network.PacketDistributor;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;

public class RocketAssemblyScreen extends AbstractContainerScreen<RocketAssemblyMenu>
{
	public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
			"textures/gui/rocket_assembler_gui_prototype.png");

	public RocketAssemblerBlockEntity blockEntity;
	public RocketPad pad;
	public RocketEntity rocketEntity;
	public RocketEntity renderRocket;

	public int stage = -1;

	public String constructionMessage;

	public RocketAssemblyScreen(RocketAssemblyMenu menu, Inventory playerInventory, Component title)
	{
		super(menu, playerInventory, title);
		this.blockEntity = menu.blockEntity;
		this.pad = menu.pad;
		this.rocketEntity = new RocketEntity(blockEntity.getLevel());
	}

	@Override
	public void init()
	{
		super.init();
		this.clearWidgets();

		PacketDistributor.sendToServer(new ServerBoundRequestRocketEntityPacket());
		int imgX = (width - 192) / 2;
		int imgY = (height - 192) / 2;

		this.addRenderableWidget(new WidgetAssembleRocket(imgX+172, imgY+165,16,16, this));
		this.addRenderableWidget(new WidgetStageChange(imgX+102, imgY+21, 5, 7, this, 1));
		this.addRenderableWidget(new WidgetStageChange(imgX+182, imgY+21, 5, 7, this, -1));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
	{
		PoseStack poseStack = graphics.pose();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int imgX = (width - 192) / 2;
		int imgY = (height - 192) / 2;

		poseStack.pushPose();
		poseStack.translate(0, 0, 0);
		graphics.blit(TEXTURE, imgX, imgY, 0, 0, 192, 192);
		graphics.fillRenderType(RenderType.endGateway(), imgX+18, imgY+18,imgX+94, imgY+158,0);
		renderRocket(graphics, poseStack, imgX, imgY);

		graphics.blit(TEXTURE, imgX+22, imgY+166, 1, 242,  141, 13);
		poseStack.popPose();
		if(blockEntity.started)
		{
			double percentage = blockEntity.progress/blockEntity.maxProgress;
			graphics.blit(TEXTURE, imgX+22, imgY+166, 1, 228, (int) (percentage*141), 13);
		}

		if (renderRocket != null)
		{
			Rocket rocket = renderRocket.getRocket();
			if(rocket.getStages().isEmpty())
				return;

			
			Font font = Minecraft.getInstance().font;
			graphics.drawCenteredString(font, stage == -1 ? "Entire Rocket" : "Stage " + (stage + 1), imgX + 145,
					imgY + 21, -1);

			double deltaV = 0;
			for(Stage rocketStage : rocket.stages)
				deltaV += rocketStage.calculateDeltaV();

			double twr = rocket.getMaxTWR();
			double takeoffDeltaV = rocket.takeoffDeltaV;
			double landingDeltaV = rocket.landingDeltaV;
			boolean canLand = rocket.canLand() && twr > 1 && deltaV > landingDeltaV;

			NumberFormat format = NumberFormat.getNumberInstance();
			format.setMaximumFractionDigits(1);
			format.setRoundingMode(RoundingMode.HALF_UP);

			poseStack.pushPose();
			poseStack.translate(imgX+102, imgY+35, 0);
			poseStack.scale(0.8f, 0.8f, 0.8f);
			graphics.drawString(font, "DeltaV: " + format.format(deltaV) + " m/s", 0, 0, -1);
			poseStack.translate(0, 9, 0);
			graphics.drawString(font, "TWR: " + format.format(twr), 0, 0, -1);

			
			if(stage == -1)
			{
				String takeoffDV = format.format(takeoffDeltaV);
				String landingDV = format.format(landingDeltaV);

				boolean canTakeOff = !takeoffDV.equals("NaN") && deltaV > 0 && twr > 1;
				canLand = canLand && !landingDV.equals("NaN");
				poseStack.translate(0, 18, 0);
				Component canTakeoffText = Component.literal("Can Takeoff? ")
												 .append(Component.literal((canTakeOff ? "Yes" : "No")).withColor(canTakeOff ? 0x00FF00 : 0xFF0000));
				graphics.drawString(font, canTakeoffText, 0, 0, -1);

				if(canTakeOff)
				{
					poseStack.translate(0, 9, 0);
					graphics.drawString(font, "Takeoff DeltaV: " + format.format(takeoffDeltaV) + " m/s", 0, 0, -1);
					poseStack.translate(0, 9, 0);
					graphics.drawString(font, "Takeoff Fuel: " + OrbitalMath.deltaVToFuelMass(rocketEntity.getRocket().getCurrentStage(), takeoffDeltaV) + " mB", 0, 0, -1);
				}

				CelestialBody body = OrbitUtil.getCelestialBodyByDimension(pad.dimension.location(), rocketEntity.level().registryAccess());
				ConfiguredOrbit orbit;

				List<ConfiguredOrbit> orbits = new ArrayList<>(body.getSupportedOrbits());
				orbits.sort(Comparator.comparing(ConfiguredOrbit::orbit,
						Comparator.comparingDouble(OrbitConfig::getAltitude)));
				orbit = orbits.getFirst();

				double orbitDeltaV = OrbitalMath.getOrbitDeltaV(body, orbit.orbit().getAltitude());
				boolean canReachOrbit = (deltaV-takeoffDeltaV-landingDeltaV) > orbitDeltaV;
				Component canOrbitText = Component.literal("Can Orbit? ")
												 .append(Component.literal((canReachOrbit ? "Yes" : "No")).withColor(canReachOrbit ? 0x00FF00 : 0xFF0000));
				poseStack.translate(0, 18, 0);
				graphics.drawString(font, canOrbitText, 0, 0, -1);
				if(canReachOrbit)
				{
					poseStack.translate(0, 9, 0);
					graphics.drawString(font, "Orbit DeltaV: " + format.format(orbitDeltaV) + " m/s", 0, 0, -1);
				}

				Component canLandText = Component.literal("Can Land? ")
												 .append(Component.literal((canLand ? "Yes" : "No")).withColor(canLand ? 0x00FF00 : 0xFF0000));
				poseStack.translate(0, 18, 0);
				graphics.drawString(font, canLandText, 0, 0, -1);
				if(canLand)
				{
					poseStack.translate(0, 9, 0);
					graphics.drawString(font, "Landing DeltaV: " + format.format(landingDeltaV) + " m/s", 0, 0, -1);
					poseStack.translate(0, 9, 0);
					graphics.drawString(font, "Landing Fuel: " + OrbitalMath.deltaVToFuelMass(rocketEntity.getRocket().getCurrentStage(), landingDeltaV) + " mB", 0, 0, -1);
				}

				poseStack.translate(0, 18, 0);
				graphics.drawString(font, "Mass: " + rocket.getMassKilogram() + " kg", 0, 0, -1);
				poseStack.translate(0, 9, 0);
				graphics.drawString(font, "Thrust: " + format.format(rocket.getMaxThrustKiloNewtons()) + " kN", 0, 0, -1);
			}
			else
			{
				poseStack.translate(0, 18, 0);
				graphics.drawString(font, "Wet Mass: " + rocket.getMassKilogram() + " kg", 0, 0, -1);
				poseStack.translate(0, 9, 0);
				graphics.drawString(font, "Dry Mass: " + rocket.getMassDryKilogram() + " kg", 0, 0, -1);

				double atmosphericIsp = 0;
				double vacuumIsp = 0;

				RocketFuel fuelType = RocketFuel.HYDROLOX;
				for(Map.Entry<BlockPos, BlockData> entry : rocket.getCurrentStage().blocks.entrySet())
				{
					if(entry.getValue() instanceof RocketEngineData engineData)
					{
						atmosphericIsp = engineData.fuelType.getAtmosphericISP();
						vacuumIsp = engineData.fuelType.getVacuumISP();
						fuelType = engineData.fuelType;
					}
				}

				poseStack.translate(0, 18, 0);
				graphics.drawString(font, "Atm. Isp: " + atmosphericIsp + " s", 0, 0, -1);
				poseStack.translate(0, 9, 0);
				graphics.drawString(font, "Vac. Isp: " + vacuumIsp + " s", 0, 0, -1);

				poseStack.translate(0, 18, 0);
				graphics.drawString(font, Component.translatable("desc.rocketry_science.rocket_fuel."+fuelType.getSerializedName()), 0, 0, -1);
			}
			poseStack.popPose();
		}
	}

	public void renderRocket(GuiGraphics graphics, PoseStack poseStack, int imgX, int imgY)
	{
		if(renderRocket != null)
		{
			graphics.enableScissor(imgX+19, imgY+19, imgX+94, imgY+158);
			if(renderRocket.getRocket().getStages().isEmpty())
			{
				graphics.disableScissor();
				return;
			}
			float eWidth = (float) Math.max(0.1f, renderRocket.makeBoundingBox().getXsize());
			float eHeight = (float) Math.max(0.1f, renderRocket.makeBoundingBox().getYsize());

			float availableWidth = 76;
			float availableHeight = 130;

			float scale = Math.min(availableWidth / eWidth, availableHeight / eHeight);

			poseStack.pushPose();
			poseStack.translate(imgX + (109 / 2f), imgY + 155f, 50.0);

			poseStack.scale(scale, -scale, scale);

			float rotation = Minecraft.getInstance().levelRenderer.getTicks();
			poseStack.mulPose(Axis.YP.rotationDegrees(rotation % 360));

			Lighting.setupForEntityInInventory();
			EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
			entityrenderdispatcher.setRenderShadow(false);

			RenderSystem.runAsFancy(
					() -> entityrenderdispatcher.render(renderRocket, 0.0, 0.0, 0.0, 0.0F, 1.0F, poseStack,
							graphics.bufferSource(), 15728880));

			graphics.flush();
			entityrenderdispatcher.setRenderShadow(true);
			poseStack.popPose();
			Lighting.setupFor3DItems();
			graphics.disableScissor();
		}
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{

	}
}
