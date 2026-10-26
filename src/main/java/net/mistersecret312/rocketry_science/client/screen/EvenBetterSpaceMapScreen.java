package net.mistersecret312.rocketry_science.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.client.OrbitRenderer;
import net.mistersecret312.rocketry_science.client.RenderBlitUtil;
import net.mistersecret312.rocketry_science.client.screen.spacemap.MapCelestialBody;
import net.mistersecret312.rocketry_science.data.orbits.ConfiguredOrbit;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.datapack.CelestialBody.GalacticData;
import net.mistersecret312.rocketry_science.datapack.CelestialBody.DisplayData;
import net.mistersecret312.rocketry_science.datapack.CelestialBody.DisplayData.CollapseData;
import net.mistersecret312.rocketry_science.datapack.CelestialBody.OrbitData;
import net.mistersecret312.rocketry_science.datapack.Galaxy;
import net.mistersecret312.rocketry_science.environment.*;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.*;
import java.util.List;

public class EvenBetterSpaceMapScreen extends Screen
{
	private double zoom = 1;
	private double cameraX = 0;
	private double cameraY = 0;

	public final Level level;
	private CelestialBody focusedBody = null;
	private CelestialBody trackedBody = null;

	private static final int UP_W = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_W);
	private static final int LEFT_A = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_A);
	private static final int DOWN_S = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_S);
	private static final int RIGHT_D = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_D);

	private static final int UP_ARROW = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_UP);
	private static final int LEFT_ARROW = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_LEFT);
	private static final int DOWN_ARROW = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_DOWN);
	private static final int RIGHT_ARROW = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_RIGHT);

	private static final int ZOOM_Z = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_Z);
	private static final int UNZOOM_X = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_X);

	private final Set<Integer> activeKeys = new HashSet<>();

	private final List<MapCelestialBody> systemList = new ArrayList<>();
	private final List<Galaxy> galaxyList = new ArrayList<>();

	public EvenBetterSpaceMapScreen(Level level, CelestialBody body)
	{
		super(Component.empty());
		this.level = level;

		this.focusedBody = body;
		this.trackedBody = body;
	}

	public EvenBetterSpaceMapScreen(Level level)
	{
		super(Component.empty());
		this.level = level;
	}

	@Override
	protected void init()
	{
		systemList.clear();
		galaxyList.clear();

		Registry<Galaxy> galaxyRegistry = level.registryAccess().registryOrThrow(Galaxy.REGISTRY_KEY);
		for (Map.Entry<ResourceKey<Galaxy>, Galaxy> entry : galaxyRegistry.entrySet())
			galaxyList.add(entry.getValue());

		Registry<CelestialBody> systemRegistry = OrbitUtil.getCelestialRegistry(level.registryAccess());
		for(Map.Entry<ResourceKey<CelestialBody>, CelestialBody> entry : systemRegistry.entrySet())
			if(entry.getValue().getGalacticData().isPresent())
				systemList.add(new MapCelestialBody(entry.getValue(), level));
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		PoseStack poseStack = graphics.pose();
		trackBody();
		renderBackground(graphics, mouseX, mouseY, partialTick);
//		renderDebugData(graphics, poseStack, mouseX, mouseY);

		handleKeyboard();

		setupUniverseRender(poseStack);
		renderGalaxyLayer(poseStack);
		renderSolarSystems(graphics, poseStack);
		poseStack.popPose();

		renderWindow(graphics, mouseX, mouseY);
	}

	private void renderWindow(GuiGraphics graphics, int mouseX, int mouseY)
	{
		if(focusedBody == null)
			return;

		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();

		ResourceLocation window = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID,
				"textures/gui/spacemap/planetwindow2bg.png");
		int sizeY = 256;
		int sizeX = 145;
		int widthBoost = 40;

		poseStack.translate(width/2f, height/2f, 0);
		poseStack.scale(0.75f, 0.75f, 1f);

		int orbitColor = -1;
		if(focusedBody.getOrbitData().isPresent())
			orbitColor = focusedBody.getOrbitData().get().orbitColor();

		Color color = new Color(orbitColor);
		RenderBlitUtil.blitGui(poseStack, window, -sizeX/2, -sizeY/2,
				sizeX+widthBoost,sizeY,
				47,0,
				sizeX,sizeY,
				256, 256,
				color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, 1f);
		renderMainBodyData(graphics, poseStack, sizeX, sizeY);
		renderBodyPropertyData(graphics, poseStack, window,
				sizeX, sizeY, mouseX, mouseY,false);
		renderBodyResourceData(graphics, poseStack, window, mouseX, mouseY);
		poseStack.popPose();
	}

	public void renderMainBodyData(GuiGraphics graphics, PoseStack poseStack, int sizeX, int sizeY)
	{
		String name = focusedBody.getTranslatableName().getString();
		ResourceLocation body = null;
		int bodySize = 64;
		if(focusedBody.getDisplayData().isPresent())
		{
			body = focusedBody.getDisplayData().get().icon().orElse(null);
			if(focusedBody.getDisplayData().get().collapseData().isPresent())
			{
				CollapseData collapseData = focusedBody.getDisplayData().get().collapseData().get();
				boolean isCollapsed = getCollapseAlpha(collapseData.systemSize()*this.zoom,
						collapseData.fadeStartPixels(), collapseData.fadeEndPixels()) > 0.99f;
				if(isCollapsed)
				{

					name = Component.translatable("celestial_body."+focusedBody.name.replace(':', '.') + ".collapsed").getString();
					if(collapseData.collapsedIcon().isPresent())
					{
						body = collapseData.collapsedIcon().get();
						bodySize = 32;
					}
				}
			}
		}
		if(body != null)
		{
			poseStack.pushPose();
			poseStack.translate(-sizeX/2f+24, -sizeY/2f+24, 0);
			RenderBlitUtil.blitGui(poseStack, body, -bodySize/2, -bodySize/2,
					bodySize, bodySize,
					0, 0, bodySize, bodySize,
					bodySize, bodySize,
					1f, 1f, 1f, 1f);
			graphics.drawString(font, name,
					24, -12, -1);

			String type = Component.translatable("rocketry_science.celestial_body.type."+focusedBody.getType().toLowerCase()).getString();
			graphics.drawString(font, type,
					24, -2, -1);

			if(focusedBody.getStarData().isPresent())
			{
				String spectral = Component.translatable("rocketry_science.star.spectral_class").getString();
				graphics.drawString(font, spectral+focusedBody.getStarData().get().getSpectralClass(),
						24, 8, -1);
			}
			poseStack.popPose();
		}
	}

	public void renderBodyPropertyData(GuiGraphics graphics, PoseStack poseStack,
									   ResourceLocation window, int sizeX, int sizeY,
									   int mouseX, int mouseY,
									   boolean textRating)
	{

		EnvironmentData environment = focusedBody.getEnvironment();
		if(environment == null) return;

		poseStack.pushPose();
		poseStack.translate(-sizeX / 2f + 24, -sizeY / 2f + 52, 0);
		poseStack.scale(1.5f, 1.5f, 1f);
		RenderBlitUtil.blitGui(poseStack, window, 0, 0, 16, 16, 16, 32, 16, 16, 256, 256, 1f, 1f, 1f, 1f);

		TemperatureRating ratingTemp = TemperatureRating.getRating(environment.temperature.getAverageTemperature());
		int distance = ratingTemp.compareTo(TemperatureRating.NORMAL);
		int value = 2 + Mth.abs(distance);
		poseStack.pushPose();
		poseStack.translate(0, 16, 0);
		RenderBlitUtil.blitGui(poseStack, window, 0, 0, 16, 10, 0, 51, 16, 10, 256, 256, 1f, 1f, 1f, 1f);
		RenderBlitUtil.blitGui(poseStack, window, 0, 0, 1 + 5 * value, 10, 0, 61, 1 + 5 * value, 8, 256, 256, 1f, 1f,
				1f, 1f);
		if(textRating)
			graphics.drawString(font, String.valueOf(Mth.abs(distance)), 16, 0, -1);

		float posX = (width/2f)+(-sizeX/2f+35)*0.75f;
		float posY = (height/2f)+(-sizeY/2f+63)*0.75f;
		int radius = 10;

		int minX = (int) (posX - radius);
		int minY = (int) (posY - radius);
		int maxX = (int) (posX + radius);
		int maxY = (int) (posY + radius);
		if(mouseX > minX && mouseX < maxX && mouseY > minY && mouseY < maxY)
			graphics.renderTooltip(font, Component.literal("Temperature"), -16, 16);

		poseStack.popPose();

		poseStack.translate(24, 0, 0);
		RenderBlitUtil.blitGui(poseStack, window, 0, -1, 16, 16, 0, 15, 16, 16, 256, 256, 1f, 1f, 1f, 1f);
		GravityRating ratingG = GravityRating.getRating(environment.gravity);
		distance = ratingG.compareTo(GravityRating.NORMAL);
		value = 2 + Mth.abs(distance);

		poseStack.pushPose();
		poseStack.translate(0, 16, 0);
		RenderBlitUtil.blitGui(poseStack, window, 0, 0, 16, 10, 0, 51, 16, 10, 256, 256, 1f, 1f, 1f, 1f);
		RenderBlitUtil.blitGui(poseStack, window, 0, 0, 1 + 5 * value, 10, 0, 61, 1 + 5 * value, 8, 256, 256, 1f, 1f,
				1f, 1f);
		if(textRating)
			graphics.drawString(font, String.valueOf(Mth.abs(distance)), 16, 0, -1);

		posX = (width/2f)+(-sizeX/2f+35+36)*0.75f;
		posY = (height/2f)+(-sizeY/2f+63)*0.75f;

		minX = (int) (posX - radius);
		minY = (int) (posY - radius);
		maxX = (int) (posX + radius);
		maxY = (int) (posY + radius);
		if(mouseX > minX && mouseX < maxX && mouseY > minY && mouseY < maxY)
			graphics.renderTooltip(font, Component.literal("Gravity"), -16, 16);

		poseStack.popPose();

		poseStack.translate(24, 0, 0);
		RenderBlitUtil.blitGui(poseStack, window, 0, -1, 16, 16, 16, 15, 16, 16, 256, 256, 1f, 1f, 1f, 1f);
		PressureRating ratingP = PressureRating.getRating(environment.getPressureSeaLevel());
		distance = ratingP.compareTo(PressureRating.NORMAL);
		value = 2 + Mth.abs(distance);

		poseStack.pushPose();
		poseStack.translate(0, 16, 0);
		RenderBlitUtil.blitGui(poseStack, window, 0, 0, 16, 10, 0, 51, 16, 10, 256, 256, 1f, 1f, 1f, 1f);
		RenderBlitUtil.blitGui(poseStack, window, 0, 0, 1 + 5 * value, 10, 0, 61, 1 + 5 * value, 8, 256, 256, 1f, 1f,
				1f, 1f);
		if(textRating)
			graphics.drawString(font, String.valueOf(Mth.abs(distance)), 16, 0, -1);
		posX = (width/2f)+(-sizeX/2f+35+72)*0.75f;
		posY = (height/2f)+(-sizeY/2f+63)*0.75f;

		minX = (int) (posX - radius);
		minY = (int) (posY - radius);
		maxX = (int) (posX + radius);
		maxY = (int) (posY + radius);
		if(mouseX > minX && mouseX < maxX && mouseY > minY && mouseY < maxY)
			graphics.renderTooltip(font, Component.literal("Atmospheric Pressure"), -16, 16);

		poseStack.popPose();

		poseStack.translate(24, 0, 0);
		RenderBlitUtil.blitGui(poseStack, window, 0, -1, 16, 16, 0, 31, 16, 16, 256, 256, 1f, 1f, 1f, 1f);
		RadiationRating ratingR = RadiationRating.getRating(environment.getRadiation());
		distance = ratingR.compareTo(RadiationRating.NONE);
		value = Mth.abs(distance);

		poseStack.pushPose();
		poseStack.translate(0, 16, 0);
		RenderBlitUtil.blitGui(poseStack, window, 0, 0, 16, 10, 0, 51, 16, 10, 256, 256, 1f, 1f, 1f, 1f);
		RenderBlitUtil.blitGui(poseStack, window, 0, 0, 1 + 5 * value, 10, 0, 61, 1 + 5 * value, 8, 256, 256, 1f, 1f,
				1f, 1f);
		if(textRating)
			graphics.drawString(font, String.valueOf(Mth.abs(distance)), 16, 0, -1);
		posX = (width/2f)+(-sizeX/2f+35+108)*0.75f;
		posY = (height/2f)+(-sizeY/2f+63)*0.75f;

		minX = (int) (posX - radius);
		minY = (int) (posY - radius);
		maxX = (int) (posX + radius);
		maxY = (int) (posY + radius);
		if(mouseX > minX && mouseX < maxX && mouseY > minY && mouseY < maxY)
		{
			graphics.renderTooltip(font, Component.literal("Radiation"), -16, 16);
			graphics.fill(minX-64, minY, maxX-64, maxY, 0xFFFFFFFF);
		}
		poseStack.popPose();

		poseStack.popPose();
	}

	public void renderBodyResourceData(GuiGraphics graphics, PoseStack poseStack,
									  ResourceLocation window, int mouseX, int mouseY)
	{
		List<Pair<Item, Integer>> resources = new ArrayList<>();
		resources.add(new Pair<>(Items.RAW_COPPER, 0xFFCB6D51));
		resources.add(new Pair<>(Items.RAW_IRON, 0xFFCB6DFF));
		resources.add(new Pair<>(Items.RAW_GOLD, 0xFFCBFFFF));

		poseStack.pushPose();
		int i = 0;
		for(Pair<Item, Integer> pair : resources)
		{
			Color pairColor = new Color(pair.getSecond());
			ItemStack stack = new ItemStack(pair.getFirst());
			poseStack.pushPose();
			poseStack.translate(-64+i*38, 40, 0);
			poseStack.scale(2f, 2f, 1f);
			RenderBlitUtil.blitGui(poseStack, window,
					0, 0,
					16, 16,
					240, 17,
					16, 16,
					256, 256,
					pairColor.getRed()/255f, pairColor.getGreen()/255f,
					pairColor.getBlue()/255f, 1f);
			RenderBlitUtil.blitGui(poseStack, window,
					0, 0,
					16, 16,
					240, 0,
					16, 16,
					256, 256,
					1f,1f,1f,1f);
			poseStack.scale(0.75f, 0.75f, 1f);

			float posX = (width/2f)+(-35)+(i*28);
			float posY = (height/2f)+(42);
			int radius = 12;

			int minX = (int) (posX - radius);
			int minY = (int) (posY - radius);
			int maxX = (int) (posX + radius);
			int maxY = (int) (posY + radius);
			if(mouseX > minX && mouseY > minY && mouseX < maxX && mouseY < maxY)
				graphics.renderTooltip(font, stack.getHoverName(), -16, 0);

			graphics.renderItem(stack, 3, 3);
			poseStack.popPose();
			i++;
		}
		poseStack.popPose();
	}

	private void trackBody()
	{
		if (this.trackedBody != null)
		{
			Vector2d targetPos = getAbsoluteWorldPosition(this.focusedBody);
			if (targetPos != null)
			{
				this.cameraX = targetPos.x;
				this.cameraY = targetPos.y;
			}
		}
	}

	private void setupUniverseRender(PoseStack poseStack)
	{
		double screenCenterX = getTrueX(width / 2f);
		double screenCenterY = getTrueY(height / 2f);
		Vector2d zeroPoint = getContextualZeroPoint(screenCenterX, screenCenterY);

		double localCamX = cameraX - zeroPoint.x;
		double localCamY = cameraY - zeroPoint.y;

		poseStack.pushPose();
		poseStack.translate(width / 2f, height / 2f, 0);
		poseStack.scale((float) zoom, (float) zoom, 1f);
		poseStack.translate(-localCamX, -localCamY, 0);
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/gui/starmap.png");
		PoseStack poseStack = graphics.pose();

		float starFieldAlpha = getInterstellarSystemFade(this.zoom);

		poseStack.pushPose();
		graphics.fill(0, 0, width, height, 0xFF000000);
		RenderBlitUtil.blitGui(poseStack, rl,
				0, 0,
				(float) (cameraX * 0.0005), (float) (cameraY * 0.0005),
				width, height,
				512, 512,
				1f, 1f, 1f, starFieldAlpha);
		poseStack.popPose();
	}

	public void renderGalaxyLayer(PoseStack poseStack)
	{
		float galaxyAlpha = getGalaxyAlpha(this.zoom);
		if (galaxyAlpha <= 0.02f) return;

		for (Galaxy galaxy : galaxyList)
		{
			poseStack.pushPose();
			Vector2d pos = galaxy.getPosition();

			double screenCenterX = getTrueX(width / 2f);
			double screenCenterY = getTrueY(height / 2f);
			Vector2d zeroPoint = getContextualZeroPoint(screenCenterX, screenCenterY);

			poseStack.translate(pos.x - zeroPoint.x, pos.y - zeroPoint.y, 0);

			float invZoom = 1024 / 0.02f;
			poseStack.scale(invZoom, invZoom, 1f);

			int galaxyIconSize = 128;
			ResourceLocation galaxyIcon = galaxy.getIcon();
			RenderBlitUtil.blitGui(poseStack, galaxyIcon,
					-galaxyIconSize/2, -galaxyIconSize/2,
					galaxyIconSize, galaxyIconSize,
					0, 0,
					galaxyIconSize, galaxyIconSize,
					galaxyIconSize, galaxyIconSize,
					1f, 1f, 1f, galaxyAlpha);

			poseStack.popPose();
		}
	}

	public void renderSolarSystems(GuiGraphics graphics, PoseStack poseStack)
	{
		float starFieldAlpha = getInterstellarSystemFade(this.zoom);
		if (starFieldAlpha > 0.02f)
		{
			for (MapCelestialBody system : systemList)
			{
				Vector2d pos = getUniversalSystemPosition(system.body);
				if (isOffScreen(pos.x, pos.y, system.cullRadius))
					continue;

				poseStack.pushPose();

				double screenCenterX = getTrueX(width / 2f);
				double screenCenterY = getTrueY(height / 2f);
				Vector2d zeroPoint = getContextualZeroPoint(screenCenterX, screenCenterY);

				poseStack.translate(pos.x - zeroPoint.x, pos.y - zeroPoint.y, 0);

				Optional<DisplayData> display = system.body.getDisplayData();
				Optional<CollapseData> collapseData = display.flatMap(DisplayData::collapseData);

				if (collapseData.isPresent())
				{
					CollapseData collapse = collapseData.get();
					double currentScreenPixels = collapse.systemSize() * this.zoom;
					float collapseAlpha = getCollapseAlpha(currentScreenPixels, collapse.fadeStartPixels(), collapse.fadeEndPixels());
					boolean isCollapsed = (collapseAlpha >= 0.99f);

					if (!isCollapsed)
					{
						renderCollapsedSystemNode(graphics, system, collapseData.get(), collapseAlpha);
						renderSystemRootNode(graphics, system, starFieldAlpha);
						renderBodyNode(graphics, system, starFieldAlpha, 0);
					}
					else renderCollapsedSystemNode(graphics, system, collapseData.get(), starFieldAlpha);
				}
				else
				{
					renderSystemRootNode(graphics, system, starFieldAlpha);
					renderBodyNode(graphics, system, starFieldAlpha, 0);
				}

				poseStack.popPose();
			}
		}
	}

	private void renderSystemRootNode(GuiGraphics graphics, MapCelestialBody system, float alpha)
	{
		Optional<ResourceLocation> icon = system.body.getDisplayData().flatMap(DisplayData::icon);
		double iconScale = system.body.getDisplayData().map(DisplayData::iconScale).orElse(1.0);
		int iconSize = system.body.getDisplayData().map(DisplayData::size).orElse(32)*2;
		int starSize = (int) (iconSize * iconScale);
		starSize = (int) Math.max(starSize, starSize / this.zoom);

		if (icon.isPresent())
		{
			RenderBlitUtil.blitGui(graphics.pose(), icon.get(), -starSize / 2, -starSize / 2,
					starSize, starSize, 0, 0, starSize, starSize, starSize, starSize, 1f, 1f, 1f, alpha);
		}
	}

	private void renderCollapsedSystemNode(GuiGraphics graphics, MapCelestialBody system, CelestialBody.DisplayData.CollapseData collapse, float alpha)
	{
		ResourceLocation icon = collapse.collapsedIcon().or(() -> system.body.getDisplayData().flatMap(CelestialBody.DisplayData::icon)).orElse(null);

		if (icon != null)
		{
			int size = (int) (64 / 0.02);
			RenderBlitUtil.blitGui(graphics.pose(), icon, -size / 2, -size / 2,
					size, size, 0, 0, size, size, size, size, 1f, 1f, 1f, alpha);
		}
	}

	private void renderBodyNode(GuiGraphics graphics, MapCelestialBody node, float alpha, int depth)
	{
		int scaleMod = Mth.clamp(depth, 1, 3) * 2;

		for (MapCelestialBody child : node.children)
		{
			if (child.body.getOrbitData().isEmpty())
				continue;
			OrbitData orbitData = child.body.getOrbitData().get();

			double trueOrbitRadius = orbitData.getMapAltitude();
			float clutterAlpha = getClutterAlpha(trueOrbitRadius, this.zoom);
			float finalChildAlpha = alpha * clutterAlpha;

			if (finalChildAlpha <= 0.02f)
				continue;

			float angle = (float) child.body.getOrbit().getAngle(level.getGameTime());
			int rawColor = orbitData.orbitColor();
			Color clr = (rawColor != -1) ? new Color(rawColor) : new Color(1f, 1f, 1f, 0.5f);
			Color orbitColor = new Color(clr.getRed() / 255f, clr.getGreen() / 255f, clr.getBlue() / 255f, finalChildAlpha);

			boolean isClockwise = !orbitData.isRetrograde();

			OrbitRenderer.drawOrbitCircle(graphics, 0, 0, (float) trueOrbitRadius, 1.0f, this.zoom, orbitColor.getRGB(),
					(float) Math.toRadians(angle), isClockwise);
		}

		for(ConfiguredOrbit orbit : node.body.getSupportedOrbits())
		{
			float prbitRadius = (float) orbit.orbit().getMapAltitude();
			if((prbitRadius == 0))
				prbitRadius = (float) orbit.orbit().getAltitude();

			float pclutterAlpha = getClutterAlpha(prbitRadius/2, this.zoom);
			float finalAlpha = alpha * pclutterAlpha;
			if(!node.body.equals(focusedBody))
				continue;

			Color color = new Color(0f, 0f, 1f, finalAlpha);
			OrbitRenderer.drawPotentialOrbit(graphics, 0, 0, prbitRadius, 1.0f, this.zoom, color.getRGB(), 256);
		}

		for (MapCelestialBody child : node.children)
		{
			Vector2d childOffset = child.body.getOrbit().getVisualPosition(level.getGameTime());
			double trueOrbitRadius = child.body.getOrbitData().map(OrbitData::getMapAltitude).orElse(0.0);

			float clutterAlpha = getClutterAlpha(trueOrbitRadius, this.zoom);
			float finalChildAlpha = alpha * clutterAlpha;

			if (finalChildAlpha <= 0.02f)
				continue;

			Optional<ResourceLocation> childIcon = child.body.getDisplayData().flatMap(DisplayData::icon);
			Optional<CollapseData> collapseData = child.body.getDisplayData().flatMap(DisplayData::collapseData);
			double iconScale = child.body.getDisplayData().map(DisplayData::iconScale).orElse(1.0);
			int iconSize = child.body.getDisplayData().map(DisplayData::size).orElse(32);

			int childSize = (int) (iconSize * iconScale);

			graphics.pose().pushPose();
			graphics.pose().translate(childOffset.x, childOffset.y, 0);

			if(collapseData.isPresent())
			{
				CollapseData data = collapseData.get();
				if(data.collapsedIcon().isEmpty() && childIcon.isPresent())
				{
					float collapseAlpha = getCollapseAlpha(data.systemSize()*zoom, data.fadeStartPixels(), data.fadeEndPixels());
					graphics.pose().pushPose();

					float invZoom = (float) Math.max(1.0f / (float) this.zoom * 4 / scaleMod / iconScale, 0.25f);
					graphics.pose().scale(invZoom, invZoom, 1f);
					RenderBlitUtil.blitGui(graphics.pose(), childIcon.get(), -childSize / 2, -childSize / 2,
							childSize, childSize, 0, 0, childSize, childSize, childSize, childSize, 1f, 1f, 1f, collapseAlpha);

					graphics.pose().popPose();
				}
			}
			else if (childIcon.isPresent())
			{
				graphics.pose().pushPose();
				float invZoom = (float) Math.max(1.0f / (float) this.zoom * 4 / scaleMod / iconScale, 0.25f);
				graphics.pose().scale(invZoom, invZoom, 1f);

				RenderBlitUtil.blitGui(graphics.pose(), childIcon.get(), -childSize / 2, -childSize / 2,
						childSize, childSize, 0, 0, childSize, childSize, childSize, childSize, 1f, 1f, 1f, finalChildAlpha);
				graphics.pose().popPose();
			}

			renderBodyNode(graphics, child, finalChildAlpha, depth + 1);

			graphics.pose().popPose();
		}
	}

	public void renderDebugData(GuiGraphics graphics, PoseStack poseStack, int mouseX, int mouseY)
	{
		poseStack.pushPose();

		poseStack.translate(width/2f, 4, 0);
		String msg = new StringFormatterMessageFactory().newMessage("X: %.2f, Y: %.2f, Zoom: %.5f", cameraX, cameraY, zoom).getFormattedMessage();
		graphics.drawCenteredString(Minecraft.getInstance().font, msg, 0, 0, -1);
		String msg2 = new StringFormatterMessageFactory().newMessage("True X: %.2f, True Y: %.2f", getTrueX(width/2f), getTrueY(height/2f)).getFormattedMessage();
		graphics.drawCenteredString(Minecraft.getInstance().font, msg2, 0, 10, -1);
		String msg3 = new StringFormatterMessageFactory().newMessage("Mouse X: %d, Mouse Y: %d", mouseX, mouseY).getFormattedMessage();
		graphics.drawCenteredString(Minecraft.getInstance().font, msg3, 0, 20, -1);

		CelestialBody hoverHit = getHoveredBody(mouseX, mouseY);
		String msg4 = new StringFormatterMessageFactory().newMessage("Hovered: %s",
				hoverHit == null ? "None" : hoverHit.getName()).getFormattedMessage();
		graphics.drawCenteredString(Minecraft.getInstance().font, msg4, 0, 30, -1);

		String msgStar = "";
		if(getSolarSystemAtScreenCenter() != null)
			msgStar = getSolarSystemAtScreenCenter().body.getTranslatableName().getString();

		if(getGalaxyAtScreenCenter() != null)
			msgStar += " " + getGalaxyAtScreenCenter().getName();

		graphics.drawCenteredString(Minecraft.getInstance().font, msgStar, 0, 40, -1);

		Vector2d coords = getContextualCoordinates(getTrueX(width/2f), getTrueY(height/2f));
		Vector2d zero = getContextualZeroPoint(getTrueX(width/2f), getTrueY(height/2f));
		String msg5 = new StringFormatterMessageFactory().newMessage("(%.2f %.2f) (%.2f %.2f)",
				coords.x, coords.y, zero.x, zero.y).getFormattedMessage();
		graphics.drawCenteredString(Minecraft.getInstance().font, msg5, 0, 50, -1);


		poseStack.popPose();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
	{
		double zoomFactor = 1.1;
		double oldZoom = this.zoom;

		if (scrollY > 0)
			this.zoom *= zoomFactor;
		else if (scrollY < 0)
			this.zoom /= zoomFactor;

		double min = 1e-5;
		double max = 50D;

		this.zoom = Mth.clamp(this.zoom, min, max);
		if (this.zoom == oldZoom)
			return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

		double worldMouseX = cameraX + (mouseX - (width / 2f)) / oldZoom;
		double worldMouseY = cameraY + (mouseY - (height / 2f)) / oldZoom;

		cameraX = worldMouseX - (mouseX - (width / 2f)) / this.zoom;
		cameraY = worldMouseY - (mouseY - (height / 2f)) / this.zoom;

		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
	{
		if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY))
			return true;

		if (button == 0 || button == 2)
		{
			this.trackedBody = null;
			cameraX -= dragX / this.zoom;
			cameraY -= dragY / this.zoom;

			return true;
		}

		return false;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		CelestialBody hit = getHoveredBody(mouseX, mouseY);
		if (hit != null)
		{
			if(button == 0)
			{
				if(focusedBody != null && focusedBody.equals(hit))
					this.trackedBody = hit;
				this.focusedBody = hit;
				return true;
			}
		}

		double trueX = getTrueX(mouseX);
		double trueY = getTrueY(mouseY);

		Galaxy galaxy = getGalaxyAt(trueX, trueY);
		float alpha = getGalaxyAlpha(zoom);
		if(galaxy != null && alpha > 0.02f)
		{
			cameraX = galaxy.getPosition().x;
			cameraY = galaxy.getPosition().y;
		}

		return super.mouseClicked(trueX, trueY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		double trueX = getTrueX(mouseX);
		double trueY = getTrueY(mouseY);
		return super.mouseReleased(trueX, trueY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if (scanCode > 0)
			activeKeys.add(scanCode);
		activeKeys.add(keyCode);

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers)
	{
		if (scanCode > 0)
			activeKeys.remove(scanCode);
		activeKeys.remove(keyCode);

		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	private boolean isDown(int scanCode, int keyCode)
	{
		return activeKeys.contains(scanCode) || activeKeys.contains(keyCode);
	}

	public void handleKeyboard()
	{
		if (activeKeys.isEmpty())
			return;

		double speed = 6 / this.zoom;
		double moveX = 0;
		double moveY = 0;

		if (isDown(UP_W, UP_ARROW))
			moveY -= 1;
		if (isDown(DOWN_S, DOWN_ARROW))
			moveY += 1;
		if (isDown(LEFT_A, LEFT_ARROW))
			moveX -= 1;
		if (isDown(RIGHT_D, RIGHT_ARROW))
			moveX += 1;

		Vector2d vec = new Vector2d(moveX, moveY);
		if (vec.length() > 1)
		{
			vec = vec.normalize();
			moveX = vec.x;
			moveY = vec.y;
		}
		if (Screen.hasShiftDown())
		{
			moveX *= 4;
			moveY *= 4;
		}

		if (moveX != 0 || moveY != 0)
		{
			this.trackedBody = null;
			cameraX += moveX * speed;
			cameraY += moveY * speed;
		}

		boolean zoomIn = isDown(ZOOM_Z, GLFW.GLFW_KEY_Z);
		boolean zoomOut = isDown(UNZOOM_X, GLFW.GLFW_KEY_X);

		if (zoomIn ^ zoomOut)
		{
			double zoomStep = Screen.hasShiftDown() ? 1.06 : 1.025;
			if (zoomIn)
				this.zoom *= zoomStep;
			if (zoomOut)
				this.zoom /= zoomStep;

			this.zoom = Mth.clamp(this.zoom, 1e-5, 50.0);
		}
	}

	public double getTrueX(double mouseX)
	{
		return cameraX + (mouseX - (width / 2f)) / zoom;
	}

	public double getTrueY(double mouseY)
	{
		return cameraY + (mouseY - (height / 2f)) / zoom;
	}

	public boolean isOffScreen(double worldX, double worldY, double radius)
	{
		double screenLeft = getTrueX(0);
		double screenRight = getTrueX(this.width);
		double screenTop = getTrueY(0);
		double screenBottom = getTrueY(this.height);

		boolean isRightOfScreen = (worldX - radius) > screenRight;
		boolean isLeftOfScreen = (worldX + radius) < screenLeft;
		boolean isBelowScreen = (worldY - radius) > screenBottom;
		boolean isAboveScreen = (worldY + radius) < screenTop;

		return isRightOfScreen || isLeftOfScreen || isBelowScreen || isAboveScreen;
	}

	@Nullable
	public CelestialBody getHoveredBody(double mouseX, double mouseY)
	{
		double worldMouseX = getTrueX(mouseX);
		double worldMouseY = getTrueY(mouseY);

		if (getGalaxyAlpha(this.zoom) > 0.05f)
			return null;

		for (MapCelestialBody system : systemList)
		{
			if (system.body.getGalacticData().isEmpty())
				continue;

			Vector2d sysPos = getUniversalSystemPosition(system.body);
			if (isOffScreen(sysPos.x, sysPos.y, system.cullRadius))
				continue;

			Optional<CollapseData> collapseData = system.body.getDisplayData().flatMap(DisplayData::collapseData);
			boolean isCollapsed = false;

			if (collapseData.isPresent())
			{
				CollapseData collapse = collapseData.get();
				double currentScreenPixels = collapse.systemSize() * this.zoom;
				float collapseAlpha = getCollapseAlpha(currentScreenPixels, collapse.fadeStartPixels(), collapse.fadeEndPixels());
				isCollapsed = (collapseAlpha >= 0.99f);
			}

			if (isCollapsed)
			{
				double size = system.body.getDisplayData().map(DisplayData::size).orElse(32);

				double radius = collapseData.map(CollapseData::systemSize).orElse(size/0.02);

				double minX = (sysPos.x - radius);
				double maxX = (sysPos.x + radius);
				double minY = (sysPos.y - radius);
				double maxY = (sysPos.y + radius);
				if(getTrueX(mouseX) > minX && getTrueX(mouseX) < maxX && getTrueY(mouseY) > minY && getTrueY(mouseY) < maxY)
					return system.body;
			}
			else
			{
				CelestialBody hitChild = findHitRecursive(system, worldMouseX, worldMouseY, sysPos.x, sysPos.y, 1.0f, 0);
				if (hitChild != null)
					return hitChild;

				double rootIconScale = system.body.getDisplayData().map(DisplayData::iconScale).orElse(1.0);
				double size = system.body.getDisplayData().map(DisplayData::size).orElse(32);
				double rootRadius = (size * rootIconScale) / 2.0;

				double minX = (sysPos.x - rootRadius);
				double maxX = (sysPos.x + rootRadius);
				double minY = (sysPos.y - rootRadius);
				double maxY = (sysPos.y + rootRadius);
				if(getTrueX(mouseX) > minX && getTrueX(mouseX) < maxX && getTrueY(mouseY) > minY && getTrueY(mouseY) < maxY)
					return system.body;
			}
		}
		return null;
	}

	private CelestialBody findHitRecursive(MapCelestialBody node,
															double mouseX, double mouseY,
															double parentWorldX, double parentWorldY,
															double currentScale, int depth)
	{
		for (MapCelestialBody child : node.children)
		{
			if (child.body.getOrbitData().isEmpty())
				continue;

			Vector2d offset = child.body.getOrbit().getVisualPosition(level.getGameTime());
			double trueOrbitRadius = offset.length();
			if (getClutterAlpha(trueOrbitRadius, this.zoom) <= 0.02f)
				continue;

			double childWorldX = parentWorldX + (offset.x * currentScale);
			double childWorldY = parentWorldY + (offset.y * currentScale);

			CelestialBody childHit = findHitRecursive(child, mouseX, mouseY, childWorldX, childWorldY, currentScale, 0);
			if(childHit != null)
				return childHit;

			int childDepth = depth + 1;
			int scaleMod = Mth.clamp(childDepth, 1, 3) * 4;
			int iconSize = child.body.getDisplayData().map(DisplayData::size).orElse(32);
			int baseSize = iconSize / scaleMod;
			double iconScale = child.body.getDisplayData().map(DisplayData::iconScale).orElse(1.0);
			double radius = (baseSize * iconScale) / 2.0;

			float invZoom = (float) Math.max(1.0f / (float) this.zoom * 4 / scaleMod / iconScale, 0.25f);

			double minX =  (childWorldX - radius*invZoom*2);
			double maxX =  (childWorldX + radius*invZoom*2);
			double minY =  (childWorldY - radius*invZoom*2);
			double maxY =  (childWorldY + radius*invZoom*2);
			if(mouseX > minX && mouseX < maxX && mouseY > minY && mouseY < maxY)
				return child.body;
		}

		return null;
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	public double getZoom()
	{
		return zoom;
	}

	@Nullable
	private Vector2d getAbsoluteWorldPosition(CelestialBody target)
	{
		if (target == null)
			return null;

		for (MapCelestialBody body : systemList)
		{
			if(body.body.getGalacticData().isEmpty())
				return null;

			Vector2d sysPos = getUniversalSystemPosition(body.body);

			if (body.body.equals(target))
				return sysPos;

			Vector2d pos = searchPositionRecursive(body, target, sysPos.x, sysPos.y, 1.0);
			if (pos != null)
				return pos;
		}
		return null;
	}

	public Vector2d getUniversalSystemPosition(CelestialBody body)
	{
		return body.getGalacticData().map(data -> {
			Galaxy galaxy = level.registryAccess().registryOrThrow(Galaxy.REGISTRY_KEY).get(data.galaxy());
			if (galaxy != null)
				return new Vector2d(galaxy.getPosition().x + data.galPos().x, galaxy.getPosition().y + data.galPos().y);

			return data.galPos();
		}).orElse(new Vector2d());
	}

	@Nullable
	public MapCelestialBody getSolarSystemAt(double worldX, double worldY)
	{
		for (MapCelestialBody system : systemList)
		{
			if (system.body.getGalacticData().isEmpty())
				continue;

			Galaxy galaxy = level.registryAccess().registryOrThrow(Galaxy.REGISTRY_KEY).get(system.body.getGalacticData().get().galaxy());
			if(galaxy == null)
				return null;

			Vector2d pos = system.body.getGalacticData().get().galPos();
			double radius = system.cullRadius;

			double dx = worldX - (pos.x + galaxy.getPosition().x);
			double dy = worldY - (pos.y + galaxy.getPosition().y);
			if ((dx * dx + dy * dy) <= (radius * radius))
				return system;
		}
		return null;
	}

	@Nullable
	public Galaxy getGalaxyAt(double worldX, double worldY)
	{
		for (Galaxy galaxy : galaxyList)
		{
			Vector2d pos = galaxy.getPosition();
			double length = new Vector2d(worldX, worldY).sub(pos).length();
			double radius = galaxy.getSize();
			if (length <= radius)
				return galaxy;
		}
		return null;
	}

	public Vector2d getWorldScreenCenter()
	{
		return new Vector2d(getTrueX(width/2f), getTrueY(height/2f));
	}

	@Nullable
	public MapCelestialBody getSolarSystemAtScreenCenter()
	{
		Vector2d screenCenter = getWorldScreenCenter();
		return getSolarSystemAt(screenCenter.x, screenCenter.y);
	}

	@Nullable
	public Galaxy getGalaxyAtScreenCenter()
	{
		Vector2d screenCenter = getWorldScreenCenter();
		return getGalaxyAt(screenCenter.x, screenCenter.y);
	}

	public Vector2d getContextualCoordinates(double worldX, double worldY)
	{
		MapCelestialBody activeSystem = getSolarSystemAt(worldX, worldY);
		if (activeSystem != null && activeSystem.body.getGalacticData().isPresent())
		{
			Vector2d sysPos = activeSystem.body.getGalacticData().get().galPos();
			return new Vector2d(worldX - sysPos.x, worldY - sysPos.y);
		}

		Galaxy activeGalaxy = getGalaxyAt(worldX, worldY);
		if (activeGalaxy != null)
			return new Vector2d(worldX - activeGalaxy.getPosition().x, worldY - activeGalaxy.getPosition().y);

		return new Vector2d(worldX, worldY);
	}

	public Vector2d getContextualZeroPoint(double worldX, double worldY)
	{
		MapCelestialBody activeSystem = getSolarSystemAt(worldX, worldY);
		if (activeSystem != null && activeSystem.body.getGalacticData().isPresent())
		{
			GalacticData data = activeSystem.body.getGalacticData().get();
			Galaxy galaxy = level.registryAccess().registryOrThrow(Galaxy.REGISTRY_KEY).get(data.galaxy());
			if (galaxy != null)
				return new Vector2d(galaxy.getPosition().x + data.galPos().x, galaxy.getPosition().y + data.galPos().y);

			return new Vector2d(data.galPos().x, data.galPos().y);
		}

		Galaxy activeGalaxy = getGalaxyAt(worldX, worldY);
		if (activeGalaxy != null)
			return activeGalaxy.getPosition();

		return new Vector2d(0, 0);
	}

	@Nullable
	private Vector2d searchPositionRecursive(MapCelestialBody node, CelestialBody target, double parentX, double parentY, double currentScale) {
		for (MapCelestialBody child : node.children)
		{
			Vector2d offset = child.body.getOrbit().getVisualPosition(level.getGameTime());

			double childWorldX = parentX + (offset.x * currentScale);
			double childWorldY = parentY + (offset.y * currentScale);

			if (child.body.equals(target))
				return new Vector2d(childWorldX, childWorldY);

			Vector2d found = searchPositionRecursive(child, target, childWorldX, childWorldY, currentScale);
			if (found != null)
				return found;
		}
		return null;
	}

	public static float getGalaxyAlpha(double zoom) {
		double fadeStart = 0.0003;
		double fadeEnd = 0.0001;
		double alpha = (fadeStart - zoom) / (fadeStart - fadeEnd);
		return (float) Mth.clamp(alpha, 0.0, 1.0);
	}

	public static float getInterstellarSystemFade(double zoom)
	{
		double fadeStart = 0.0003;
		double fadeEnd = 0.0001;
		double alpha = (zoom - fadeEnd) / (fadeStart - fadeEnd);
		return (float) Mth.clamp(alpha, 0.0, 1.0);
	}

	public static float getCollapseAlpha(double currentScreenPixels, double fadeStartPixels, double fadeEndPixels)
	{
		if (fadeStartPixels <= 0 || fadeEndPixels <= 0)
			return 1.0f;

		double alpha = (currentScreenPixels - fadeEndPixels) / (fadeStartPixels - fadeEndPixels);
		return (float) Mth.clamp(alpha, 0.0, 1.0);
	}

	public static float getPlanetAlpha(double zoom, double radius)
	{
		double currentScreenPixels = radius * zoom;
		double fadeStart = 0.2;
		double fadeEnd = 0.4;
		double alpha = (currentScreenPixels - fadeStart) / (fadeEnd - fadeStart);
		return (float) Mth.clamp(alpha, 0.02, 1.0);
	}

	public static float getClutterAlpha(double trueOrbitRadius, double zoom)
	{
		double screenOrbitPixels = trueOrbitRadius * zoom;
		double fadeStart = 10.0;
		double fadeEnd = 25.0;
		double alpha = (screenOrbitPixels - fadeStart) / (fadeEnd - fadeStart);
		return (float) Mth.clamp(alpha, 0.02, 1.0);
	}
}
