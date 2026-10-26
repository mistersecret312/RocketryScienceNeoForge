package net.mistersecret312.rocketry_science.client.screen;

//public class BetterSpaceMapScreen extends Screen
//{
//	public static final double EARTH_ORBIT = 100d;
//	public static final double KM_PER_AU = 149597870.7d;
//	public static final double KM_TO_UI = EARTH_ORBIT / KM_PER_AU;
//
//	private double zoom = 1;
//	public double panX = 0;
//	public double panY = 0;
//
//	public final Level level;
//	private final List<MapSolarSystem> systemList = new ArrayList<>();
//	private final List<CelestialBody> favoriteBodies = new ArrayList<>();
//
//	private CelestialBody focusedBody = null;
//	private CelestialBody trackedBody = null;
//
//	private static final int UP_W = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_W);
//	private static final int LEFT_A = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_A);
//	private static final int DOWN_S = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_S);
//	private static final int RIGHT_D = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_D);
//
//	private static final int UP_ARROW = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_UP);
//	private static final int LEFT_ARROW = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_LEFT);
//	private static final int DOWN_ARROW = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_DOWN);
//	private static final int RIGHT_ARROW = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_RIGHT);
//
//	private static final int ZOOM_Z = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_Z);
//	private static final int UNZOOM_X = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_X);
//
//	private final Set<Integer> activeKeys = new HashSet<>();
//
//	public BetterSpaceMapScreen(Level level)
//	{
//		super(Component.empty());
//		this.level = level;
//	}
//
//	@Override
//	protected void init()
//	{
//		panX = 0;
//		panY = 0;
//
//		systemList.clear();
//		Registry<SolarSystem> systemRegistry = OrbitUtil.getSystemRegistry(level);
//
//		for(Map.Entry<ResourceKey<SolarSystem>, SolarSystem> entry : systemRegistry.entrySet())
//			systemList.add(new MapSolarSystem(entry.getValue(), level));
//	}
//
//	@Override
//	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
//	{
//		if (this.trackedBody != null)
//		{
//			Vector2d targetPos = getAbsoluteWorldPosition(this.focusedBody);
//			if (targetPos != null)
//			{
//				this.panX = -(targetPos.x * this.zoom);
//				this.panY = -(targetPos.y * this.zoom);
//			}
//		}
//		renderBackground(graphics, mouseX, mouseY, partialTick);
//		handleKeyboard();
//
//		PoseStack poseStack = graphics.pose();
//		poseStack.pushPose();
//		poseStack.translate(width/2f, height/2f, 0);
//
//		poseStack.pushPose();
//
//		poseStack.translate(0, -height/2f+4, 0);
//		String msg = new StringFormatterMessageFactory().newMessage("X: %.2f, Y: %.2f, Zoom: %.4f", panX, panY, zoom).getFormattedMessage();
//		graphics.drawCenteredString(Minecraft.getInstance().font, msg, 0, 0, -1);
//		String msg2 = new StringFormatterMessageFactory().newMessage("True X: %.2f, True Y: %.2f", getTrueX(width/2f), getTrueY(height/2f)).getFormattedMessage();
//		graphics.drawCenteredString(Minecraft.getInstance().font, msg2, 0, 10, -1);
//		String msg3 = new StringFormatterMessageFactory().newMessage("Mouse X: %d, Mouse Y: %d", mouseX, mouseY).getFormattedMessage();
//		graphics.drawCenteredString(Minecraft.getInstance().font, msg3, 0, 20, -1);
//
//		HoverInfo hoverHit = getHoveredBody(mouseX, mouseY);
//		String msg4 = new StringFormatterMessageFactory().newMessage("Hovered: %s",
//				hoverHit == null ? "None" : bodyKeyToName(hoverHit.body.getName())).getFormattedMessage();
//		graphics.drawCenteredString(Minecraft.getInstance().font, msg4, 0, 30, -1);
//
//		List<Breadcrumb> breadcrumbs = getLocationBreadcrumbs();
//
//		int textX = -width/2+10;
//		int textY = 10;
//		Font font = Minecraft.getInstance().font;
//
//		for (int i = 0; i < breadcrumbs.size(); i++)
//		{
//			Breadcrumb bc = breadcrumbs.get(i);
//
//			int wordWidth = font.width(bc.name);
//			textX += width/2;
//			boolean isHoveringText = mouseX >= textX && mouseX <= textX + wordWidth &&
//											 mouseY >= textY && mouseY <= textY + font.lineHeight;
//			textX -= width/2;
//			int color = (isHoveringText && !bc.name.equals("Deep Space")) ? 0xFFFF00 : 0xFFFFFF;
//
//			graphics.drawString(font, bc.name, textX, textY, color);
//			textX += wordWidth;
//
//			if (i < breadcrumbs.size() - 1)
//			{
//				graphics.drawString(font, " : ", textX, textY, 0x555555);
//				textX += font.width(" : ");
//			}
//		}
//
//		poseStack.popPose();
//
//		poseStack.translate(panX, panY, 0);
//		poseStack.scale((float) zoom, (float) zoom, 1f);
//
//		float galaxyAlpha = getGalaxyAlpha(this.zoom);
//		float starFieldAlpha = getInterstellarSystemFade(this.zoom);
//
//		if (galaxyAlpha > 0.02f)
//		{
//			graphics.pose().pushPose();
//
//			float invZoom = 1024 / 0.02f;
//			graphics.pose().scale(invZoom, invZoom, 1f);
//
//			int galaxyIconSize = 128;
//			ResourceLocation galaxyIcon = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/misc/galaxy_mw.png");
//			RenderBlitUtil.blitGui(graphics.pose(), galaxyIcon,
//					-galaxyIconSize / 2, -galaxyIconSize / 2 - 16,
//					galaxyIconSize, galaxyIconSize,
//					0, 0,
//					galaxyIconSize, galaxyIconSize,
//					galaxyIconSize, galaxyIconSize,
//					1f, 1f, 1f, galaxyAlpha);
//
//			graphics.pose().popPose();
//		}
//
//		if(starFieldAlpha > 0.02f)
//		{
//			for(MapSolarSystem system : systemList)
//			{
//				Vector2d pos = system.system.getPosition();
//
//				if(isOffScreen(pos.x, pos.y, system.cullRadius)) continue;
//
//				graphics.pose().pushPose();
//				graphics.pose().translate(pos.x, pos.y, 0);
//
//				float sysAlpha = getInterstellarStarAlpha(this.zoom, system.collapseRadius);
//				float planetAlpha = getPlanetAlpha(this.zoom, system.collapseRadius);
//
//				if(sysAlpha >= 0.03f)
//				{
//					int starSize = (int) (64 / 0.02);
//					String[] split = system.system.getName().split(":");
//					String systemName = Component.translatable("solar_system." + split[0] + "." + split[1]).getString();
//					Color color = new Color(1f, 1f, 1f, sysAlpha);
//					poseStack.pushPose();
//					poseStack.translate(0, -starSize / 2f, 0f);
//					poseStack.scale(starSize / 64f, starSize / 64f, 1f);
//					graphics.drawCenteredString(font, Component.literal(systemName), 0, 0, color.getRGB());
//					poseStack.popPose();
//					RenderBlitUtil.blitGui(poseStack, system.system.getIcon(), (int) (-starSize / 2f),
//							(int) (-starSize / 2f), starSize, starSize, 0, 0, starSize, starSize, starSize, starSize,
//							1f, 1f, 1f, sysAlpha);
//				}
//
//				if(planetAlpha > 0.02f) renderBodyNode(graphics, system.star, planetAlpha, 0);
//
//				graphics.pose().popPose();
//			}
//		}
//
//		poseStack.popPose();
//		renderFavoritesBar(graphics, mouseX, mouseY);
//	}
//
//	private void renderBodyNode(GuiGraphics graphics, MapCelestialBody node, float alpha, int depth)
//	{
//		int scaleMod = Mth.clamp(depth, 1, 3)*2;
//		int baseSize = 32 / scaleMod;
//
//		graphics.pose().pushPose();
//		float invZoom = Math.max(1.0f / (float) this.zoom * 4 / scaleMod, 0.25f);
//		graphics.pose().scale(invZoom, invZoom, 1f);
//
//		if(node.body.getIcon().isPresent())
//		{
//			RenderBlitUtil.blitGui(graphics.pose(), node.body.getIcon().get(), -baseSize / 2, -baseSize / 2, baseSize,
//					baseSize, 0, 0, baseSize, baseSize, baseSize, baseSize, 1f, 1f, 1f, alpha);
//		}
//		else if(!node.children.isEmpty() && node.body.getName().contains("barycenter"))
//		{
//			CelestialBody child = node.children.getFirst().body;
//			Vector2d childOffset = child.getOrbit().getPosition(level.getGameTime(), level.registryAccess());
//
//			double trueOrbitRadius = childOffset.length() * node.body.getOrbitScale();
//
//			float clutterAlpha = 1-getClutterAlpha(trueOrbitRadius, this.zoom);
//			float finalChildAlpha = alpha * clutterAlpha;
//
//			if (finalChildAlpha > 0.02f && child.getIcon().isPresent())
//			{
//				RenderBlitUtil.blitGui(graphics.pose(), child.getIcon().get(), -baseSize / 2, -baseSize / 2, baseSize,
//						baseSize, 0, 0, baseSize, baseSize, baseSize, baseSize, 1f, 1f, 1f, finalChildAlpha);
//			}
//		}
//
//		graphics.pose().popPose();
//
//		if(false)
//		{
//			for(ConfiguredOrbit orbit : node.body.getSupportedOrbits())
//			{
//				float trueOrbitRadius = (float) (orbit.orbit().getAltitude() * KM_TO_UI * 25500 * node.body.getOrbitScale());
//				float clutterAlpha = getClutterAlpha(trueOrbitRadius, this.zoom);
//				float finalAlpha = alpha * clutterAlpha;
//
//				Color color = new Color(0f, 0f, 1f, finalAlpha);
//				graphics.pose().pushPose();
//				graphics.pose().translate(0, -trueOrbitRadius, 0);
//				graphics.pose().scale(0.01f, 0.01f, 1);
//				Color nameColor = new Color(1f, 1f, 1f, finalAlpha);
//				graphics.drawCenteredString(Minecraft.getInstance().font, Component.literal(orbit.orbit().getName() + " " + node.body.getName()),
//						0, 0, nameColor.getRGB());
//				graphics.pose().popPose();
//				OrbitRenderer.drawPotentialOrbit(graphics, 0, 0, trueOrbitRadius,1.0f, this.zoom, color.getRGB(), 256);
//			}
//		}
//
//		for(MapCelestialBody child : node.children)
//		{
//			Vector2d childOffset = child.body.getOrbit().getPosition(level.getGameTime(), level.registryAccess());
//
//			double trueOrbitRadius = childOffset.length() * node.body.getOrbitScale();
//
//			float clutterAlpha = getClutterAlpha(trueOrbitRadius, this.zoom);
//			float finalChildAlpha = alpha * clutterAlpha;
//
//			if (finalChildAlpha <= 0.02f)
//				continue;
//
//			float angle = (float) child.body.getOrbit().getAngle(level.getGameTime());
//
//			Color clr = new Color(child.body.getOrbitColor());
//			Color orbitColor = new Color(clr.getRed()/255f, clr.getGreen()/255f, clr.getBlue()/255f, finalChildAlpha);
//			boolean isClockwise = !child.body.isRetrograde();
//
//			OrbitRenderer.drawOrbitCircle(graphics, 0, 0, (float) trueOrbitRadius, 1.0f, this.zoom, orbitColor.getRGB(),
//					(float) Math.toRadians(angle), isClockwise);
//
//			float orbitScale = (float) node.body.getOrbitScale();
//
//			childOffset = child.body.getOrbit().getVisualPosition(level.getGameTime());
//
//			graphics.pose().pushPose();
//			graphics.pose().scale(orbitScale, orbitScale, 1.0f);
//			graphics.pose().translate(childOffset.x, childOffset.y, 0);
//
//			renderBodyNode(graphics, child, finalChildAlpha, depth+1);
//
//			graphics.pose().popPose();
//		}
//	}
//
//	@Override
//	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
//	{
//		ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "textures/gui/starmap.png");
//		PoseStack poseStack =  graphics.pose();
//
//		float starFieldAlpha = getInterstellarSystemFade(this.zoom);
//
//		poseStack.pushPose();
//		graphics.fill(0, 0, width, height, 0xFF000000);
//		RenderBlitUtil.blitGui(poseStack, rl,
//				0, 0,
//				(float) (-panX / zoom*0.0005), (float) (-panY / zoom * 0.0005),
//				width, height,
//				512, 512,
//				1f, 1f, 1f, starFieldAlpha);
//		poseStack.popPose();
//	}
//
//	@Override
//	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
//	{
//		double zoomFactor = 1.1;
//		double oldZoom = this.zoom;
//
//		if (scrollY > 0)
//			this.zoom *= zoomFactor;
//		else if (scrollY < 0)
//			this.zoom /= zoomFactor;
//
//		double min = 1e-4;
//		double max = 50D;
//
//		this.zoom = Mth.clamp(this.zoom, min, max);
//		if(this.zoom == oldZoom)
//			return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
//
//		double centeredMouseX = mouseX - (width / 2f);
//		double centeredMouseY = mouseY - (height / 2f);
//
//		double worldMouseX = (centeredMouseX - this.panX) / oldZoom;
//		double worldMouseY = (centeredMouseY - this.panY) / oldZoom;
//
//		this.panX = centeredMouseX - (worldMouseX * this.zoom);
//		this.panY = centeredMouseY - (worldMouseY * this.zoom);
//
//		return true;
//	}
//
//	@Override
//	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
//	{
//		if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY))
//			return true;
//
//		if (button == 0 || button == 2)
//		{
//			this.trackedBody = null;
//			this.focusedBody = null;
//			this.panX += dragX;
//			this.panY += dragY;
//
//			return true;
//		}
//
//		return false;
//	}
//
//	@Override
//	public boolean mouseClicked(double mouseX, double mouseY, int button)
//	{
//		CelestialBody hoveredFav = getHoveredFavorite(mouseX, mouseY);
//		if (hoveredFav != null)
//		{
//			if (button == 0)
//			{
//				if(this.focusedBody != null && this.focusedBody.equals(hoveredFav))
//					this.trackedBody = hoveredFav;
//				this.focusedBody = hoveredFav;
//			}
//			else if (button == 1)
//				this.favoriteBodies.remove(hoveredFav);
//			return true;
//		}
//
//		if (button == 0)
//		{
//			Font font = Minecraft.getInstance().font;
//			int textX = 10;
//			int textY = 10;
//
//			if (mouseY >= textY && mouseY <= textY + font.lineHeight)
//			{
//				List<Breadcrumb> breadcrumbs = getLocationBreadcrumbs();
//
//				for (int i = 0; i < breadcrumbs.size(); i++)
//				{
//					Breadcrumb bc = breadcrumbs.get(i);
//					int wordWidth = font.width(bc.name());
//
//					if (mouseX >= textX && mouseX <= textX + wordWidth)
//					{
//						this.focusedBody = bc.body();
//						this.trackedBody = bc.body();
//						return true;
//					}
//
//					textX += wordWidth;
//					if (i < breadcrumbs.size() - 1)
//						textX += font.width(" : ");
//				}
//			}
//		}
//
//		HoverInfo hit = getHoveredBody(mouseX, mouseY);
//		if (hit != null)
//		{
//			if(button == 1)
//			{
//				if(this.favoriteBodies.contains(hit.body()))
//					this.favoriteBodies.remove(hit.body());
//				else this.favoriteBodies.add(hit.body());
//			}
//			if(button == 0)
//			{
//				if(focusedBody != null && focusedBody.equals(hit.body))
//					this.trackedBody = hit.body();
//				this.focusedBody = hit.body();
//				return true;
//			}
//		}
//
//		double trueX = getTrueX(mouseX);
//		double trueY = getTrueY(mouseY);
//		return super.mouseClicked(trueX, trueY, button);
//	}
//
//	@Override
//	public boolean mouseReleased(double mouseX, double mouseY, int button)
//	{
//		double trueX = getTrueX(mouseX);
//		double trueY = getTrueY(mouseY);
//		return super.mouseReleased(trueX, trueY, button);
//	}
//
//	@Override
//	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
//	{
//		if (scanCode > 0)
//			activeKeys.add(scanCode);
//		activeKeys.add(keyCode);
//
//		return super.keyPressed(keyCode, scanCode, modifiers);
//	}
//
//	@Override
//	public boolean keyReleased(int keyCode, int scanCode, int modifiers)
//	{
//		if (scanCode > 0)
//			activeKeys.remove(scanCode);
//		activeKeys.remove(keyCode);
//
//		return super.keyReleased(keyCode, scanCode, modifiers);
//	}
//
//	private boolean isDown(int scanCode, int keyCode)
//	{
//		return activeKeys.contains(scanCode) || activeKeys.contains(keyCode);
//	}
//
//	public void handleKeyboard()
//	{
//		if (activeKeys.isEmpty()) return;
//
//		double speed = 24;
//
//		double moveX = 0;
//		double moveY = 0;
//
//		if (isDown(UP_W, UP_ARROW))
//			moveY += 1;
//		if (isDown(DOWN_S, DOWN_ARROW))
//			moveY -= 1;
//		if (isDown(LEFT_A, LEFT_ARROW))
//			moveX += 1;
//		if (isDown(RIGHT_D, RIGHT_ARROW))
//			moveX -= 1;
//		Vector2d vec = new Vector2d(moveX*speed, moveY*speed);
//		if(vec.length() > 1)
//		{
//			vec = vec.normalize();
//			moveX = vec.x;
//			moveY = vec.y;
//		}
//		if(Screen.hasShiftDown())
//		{
//			moveX *= 4;
//			moveY *= 4;
//		}
//
//		if (moveX != 0 || moveY != 0)
//		{
//			this.trackedBody = null;
//			this.focusedBody = null;
//
//			this.panX += moveX;
//			this.panY += moveY;
//		}
//
//		boolean zoomIn = isDown(ZOOM_Z, GLFW.GLFW_KEY_Z);
//		boolean zoomOut = isDown(UNZOOM_X, GLFW.GLFW_KEY_X);
//
//		if (zoomIn ^ zoomOut)
//		{
//			double zoomStep = Screen.hasShiftDown() ? 1.06 : 1.025;
//			double oldZoom = this.zoom;
//
//			if (zoomIn)
//				this.zoom *= zoomStep;
//			if (zoomOut)
//				this.zoom /= zoomStep;
//
//			this.zoom = Mth.clamp(this.zoom, 1e-4, 50.0);
//
//			if (this.zoom != oldZoom)
//			{
//				double worldMouseX = this.panX / oldZoom;
//				double worldMouseY = this.panY / oldZoom;
//
//				this.panX = worldMouseX * this.zoom;
//				this.panY = worldMouseY * this.zoom;
//			}
//		}
//	}
//
//	public double getTrueX(double mouseX)
//	{
//		return (mouseX - (width / 2f) - panX) / zoom;
//	}
//
//	public double getTrueY(double mouseY)
//	{
//		return (mouseY - (height / 2f) - panY) / zoom;
//	}
//
//	public boolean isOffScreen(double worldX, double worldY, double radius)
//	{
//		double screenLeft = getTrueX(0);
//		double screenRight = getTrueX(this.width);
//		double screenTop = getTrueY(0);
//		double screenBottom = getTrueY(this.height);
//
//		boolean isRightOfScreen = (worldX - radius) > screenRight;
//		boolean isLeftOfScreen = (worldX + radius) < screenLeft;
//		boolean isBelowScreen = (worldY - radius) > screenBottom;
//		boolean isAboveScreen = (worldY + radius) < screenTop;
//
//		return isRightOfScreen || isLeftOfScreen || isBelowScreen || isAboveScreen;
//	}
//
//	@Nullable
//	public HoverInfo getHoveredBody(double mouseX, double mouseY)
//	{
//		double worldMouseX = getTrueX(mouseX);
//		double worldMouseY = getTrueY(mouseY);
//
//		for (MapSolarSystem system : systemList)
//		{
//			if (getInterstellarStarAlpha(this.zoom, system.collapseRadius) >= 0.03f)
//			{
//				int starSize = (int) (64/0.02);
//				if (isHoveringBody(worldMouseX, worldMouseY, system.system.getPosition().x, system.system.getPosition().y,
//						(int) (starSize * zoom)))
//					return new HoverInfo(system.star.body, "Solar System");
//				continue;
//			}
//
//			HoverInfo hit = findHitRecursive(system.star, worldMouseX, worldMouseY,
//					system.system.getPosition().x, system.system.getPosition().y,
//					1.0f, 0);
//			if (hit != null)
//				return hit;
//		}
//		return null;
//	}
//
//	private HoverInfo findHitRecursive(MapCelestialBody node,
//									   double mouseX, double mouseY,
//									   double parentWorldX, double parentWorldY,
//									   double currentScale, int depth)
//	{
//		int scaleMod = Mth.clamp(depth, 1, 3)*2;
//		int baseSize = 32 / scaleMod;
//
//		if (isHoveringBody(mouseX, mouseY, parentWorldX, parentWorldY, baseSize))
//			return new HoverInfo(node.body, node.body.getType());
//
//
//		for (MapCelestialBody child : node.children)
//		{
//			Vector2d offset = child.body.getOrbit().getVisualPosition(level.getGameTime());
//			double trueOrbitRadius = offset.length() * node.body.getOrbitScale();
//			if (getClutterAlpha(trueOrbitRadius, this.zoom) <= 0.02f)
//				continue;
//
//			double childWorldX = parentWorldX + (offset.x * currentScale) * node.body.getOrbitScale();
//			double childWorldY = parentWorldY + (offset.y * currentScale) * node.body.getOrbitScale();
//
//			double nextScale = currentScale * node.body.getOrbitScale();
//
//			HoverInfo hit = findHitRecursive(child, mouseX, mouseY,
//					childWorldX, childWorldY,
//					nextScale, depth + 1);
//			if (hit != null)
//				return hit;
//		}
//		return null;
//	}
//
//	private boolean isHoveringBody(double mouseX, double mouseY, double bodyX, double bodyY, int visualSizePixels)
//	{
//		double interactionRadius = (visualSizePixels / 2.0) / this.zoom;
//
//		double dx = mouseX - bodyX;
//		double dy = mouseY - bodyY;
//		return (dx * dx + dy * dy) <= (interactionRadius * interactionRadius);
//	}
//
//	@Override
//	public boolean isPauseScreen()
//	{
//		return false;
//	}
//
//	public double getZoom()
//	{
//		return zoom;
//	}
//
//	public static float getPlanetAlpha(double zoom, double radius)
//	{
//		double currentScreenPixels = radius * zoom;
//
//		double fadeStart = 0.2;
//		double fadeEnd = 0.4;
//
//		double alpha = (currentScreenPixels - fadeStart) / (fadeEnd - fadeStart);
//		return (float) Mth.clamp(alpha, 0.02, 1.0);
//	}
//
//	public static float getGalaxyAlpha(double zoom)
//	{
//		double fadeStart = 0.0003;
//		double fadeEnd = 1e-4;
//		double alpha = (fadeStart - zoom) / (fadeStart - fadeEnd);
//		return (float) Mth.clamp(alpha, 0.0, 1.0);
//	}
//
//	public static float getInterstellarSystemFade(double zoom)
//	{
//		double fadeStart = 0.0003;
//		double fadeEnd = 0.0001;
//		double alpha = (zoom - fadeEnd) / (fadeStart - fadeEnd);
//		return (float) Mth.clamp(alpha, 0.0, 1.0);
//	}
//
//	public static float getInterstellarStarAlpha(double zoom)
//	{
//		double currentScreenPixels = 8 * zoom;
//
//		double fadeStart = 0.2;
//		double fadeEnd = 0.4;
//
//		double alpha = 1.0 - ((currentScreenPixels - fadeStart) / (fadeEnd - fadeStart));
//		return (float) Mth.clamp(alpha, 0.02, 1.0);
//	}
//
//	public static float getSpacecraftAlpha(double zoom)
//	{
//		double fadeStart = 4.0;
//		double fadeEnd = 5.0;
//
//		double alpha = (zoom - fadeStart) / (fadeEnd - fadeStart);
//		return (float) Mth.clamp(alpha, 0.02, 1.0);
//	}
//
//	public static float getClutterAlpha(double trueOrbitRadius, double zoom)
//	{
//		double screenOrbitPixels = trueOrbitRadius * zoom;
//
//		double fadeStart = 10.0;
//		double fadeEnd = 25.0;
//
//		double alpha = (screenOrbitPixels - fadeStart) / (fadeEnd - fadeStart);
//		return (float) Mth.clamp(alpha, 0.02, 1.0);
//	}
//
//	public String bodyKeyToName(String key)
//	{
//		String[] split = key.split(":");
//		if(split.length == 1)
//			return key;
//		return Component.translatable("celestial_body."+split[0]+"."+split[1]).getString();
//	}
//
//	@Nullable
//	private Vector2d getAbsoluteWorldPosition(CelestialBody target)
//	{
//		if (target == null)
//			return null;
//
//		for (MapSolarSystem system : systemList)
//		{
//			Vector2d sysPos = system.system.getPosition();
//
//			if (system.star.body.equals(target))
//				return sysPos;
//
//			Vector2d pos = searchPositionRecursive(system.star, target, sysPos.x, sysPos.y, 1.0);
//			if (pos != null)
//				return pos;
//		}
//		return null;
//	}
//
//	@Nullable
//	private Vector2d searchPositionRecursive(MapCelestialBody node, CelestialBody target, double parentX, double parentY, double currentScale) {
//		for (MapCelestialBody child : node.children)
//		{
//			Vector2d offset = child.body.getOrbit().getVisualPosition(level.getGameTime());
//
//			double childWorldX = parentX + (offset.x * currentScale) * node.body.getOrbitScale();
//			double childWorldY = parentY + (offset.y * currentScale) * node.body.getOrbitScale();
//
//			if (child.body.equals(target))
//				return new Vector2d(childWorldX, childWorldY);
//
//			double nextScale = currentScale * node.body.getOrbitScale();
//			Vector2d found = searchPositionRecursive(child, target, childWorldX, childWorldY, nextScale);
//			if (found != null)
//				return found;
//		}
//		return null;
//	}
//
//	public List<Breadcrumb> getLocationBreadcrumbs()
//	{
//		if (this.focusedBody != null)
//			return buildPathFromHierarchy(this.focusedBody);
//
//		List<Breadcrumb> path = new ArrayList<>();
//		path.add(new Breadcrumb("Deep Space", null));
//
//		double camX = getTrueX(width / 2f);
//		double camY = getTrueY(height / 2f);
//
//		MapSolarSystem closestSystem = null;
//		double closestDistance = Double.MAX_VALUE;
//		for (MapSolarSystem system : systemList)
//		{
//			if (getInterstellarStarAlpha(this.zoom, system.collapseRadius) >= 0.03f)
//				continue;
//
//			Vector2d pos = system.system.getPosition();
//			double distToSystem = Math.sqrt((camX - pos.x)*(camX - pos.x) + (camY - pos.y)*(camY - pos.y));
//
//			if(distToSystem <= closestDistance)
//			{
//				closestDistance = distToSystem;
//				closestSystem = system;
//			}
//		}
//
//		if (closestSystem != null && closestDistance <= closestSystem.cullRadius)
//		{
//			path.clear();
//			String[] split = closestSystem.system.getName().split(":");
//			path.add(new Breadcrumb(Component.translatable("solar_system."+split[0]+"."+split[1]).getString(),
//					closestSystem.star.body));
//		}
//
//		return path;
//	}
//
//	private List<Breadcrumb> buildPathFromHierarchy(CelestialBody target)
//	{
//		List<Breadcrumb> path = new ArrayList<>();
//		CelestialBody current = target;
//
//		while (current != null)
//		{
//			boolean isStar = false;
//			String name = current.getName();
//			for(MapSolarSystem system : systemList)
//			{
//				if(current.equals(system.star.body))
//				{
//					isStar = true;
//					name = system.system.getName();
//				}
//			}
//
//			if(isStar)
//			{
//				String[] split = name.split(":");
//				path.add(new Breadcrumb(Component.translatable("solar_system."+split[0]+"."+split[1]).getString(), current));
//			}
//			else path.add(new Breadcrumb(bodyKeyToName(name), current));
//
//			ResourceKey<CelestialBody> parentKey = current.getOrbit() != null ? current.getOrbit().getParent(level.registryAccess()) : null;
//			if (parentKey != null)
//				current = OrbitUtil.getCelestialRegistry(level.registryAccess()).get(parentKey);
//
//			else current = null;
//		}
//
//		Collections.reverse(path);
//		return path;
//	}
//
//	private static final int FAV_ICON_SIZE = 32;
//	private static final int FAV_PADDING = 6;
//	private static final int FAV_BOTTOM_MARGIN = 15;
//
//	@Nullable
//	private CelestialBody getHoveredFavorite(double mouseX, double mouseY)
//	{
//		if (favoriteBodies.isEmpty())
//			return null;
//
//		int totalWidth = favoriteBodies.size() * FAV_ICON_SIZE + (favoriteBodies.size() - 1) * FAV_PADDING;
//		int startX = (width - totalWidth) / 2;
//		int startY = height - FAV_ICON_SIZE - FAV_BOTTOM_MARGIN;
//
//		if (mouseY >= startY && mouseY <= startY + FAV_ICON_SIZE)
//			for (int i = 0; i < favoriteBodies.size(); i++)
//			{
//				int iconX = startX + i * (FAV_ICON_SIZE + FAV_PADDING);
//				if (mouseX >= iconX && mouseX <= iconX + FAV_ICON_SIZE)
//					return favoriteBodies.get(i);
//			}
//
//		return null;
//	}
//
//	private void renderFavoritesBar(GuiGraphics graphics, int mouseX, int mouseY)
//	{
//		if (favoriteBodies.isEmpty())
//			return;
//
//		int totalWidth = favoriteBodies.size() * FAV_ICON_SIZE + (favoriteBodies.size() - 1) * FAV_PADDING;
//		int startX = (width - totalWidth) / 2;
//		int startY = height - FAV_ICON_SIZE - FAV_BOTTOM_MARGIN;
//
//		graphics.fill(startX - 8, startY - 6, startX + totalWidth + 8, startY + FAV_ICON_SIZE + 6, 0x90000000);
//
//		for (int i = 0; i < favoriteBodies.size(); i++)
//		{
//			CelestialBody body = favoriteBodies.get(i);
//			int iconX = startX + i * (FAV_ICON_SIZE + FAV_PADDING);
//
//			boolean isHovering = mouseX >= iconX && mouseX <= iconX + FAV_ICON_SIZE &&
//										 mouseY >= startY && mouseY <= startY + FAV_ICON_SIZE;
//
//			boolean isFocused = body.equals(this.focusedBody);
//			if (isFocused)
//				graphics.fill(iconX - 2, startY - 2, iconX + FAV_ICON_SIZE + 2, startY + FAV_ICON_SIZE + 2, 0x88FFFFFF); // Strong white border
//			else if (isHovering)
//				graphics.fill(iconX - 2, startY - 2, iconX + FAV_ICON_SIZE + 2, startY + FAV_ICON_SIZE + 2, 0x44FFFFFF); // Faint hover highlight
//
//			if(body.getIcon().isPresent())
//			{
//				RenderBlitUtil.blitGui(graphics.pose(), body.getIcon().get(), iconX, startY, FAV_ICON_SIZE, FAV_ICON_SIZE, 0,
//						0, 32, 32, 32, 32, 1f, 1f, 1f, 1f);
//			}
//			if (isHovering)
//				graphics.renderTooltip(Minecraft.getInstance().font, Component.literal(bodyKeyToName(body.getName())), mouseX, mouseY);
//		}
//	}
//
//	public record HoverInfo(CelestialBody body, String type) {}
//	public record Breadcrumb(String name, @Nullable CelestialBody body) {}
//}
