package net.mistersecret312.rocketry_science.client.screen.widgets;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.mistersecret312.rocketry_science.client.OrbitRenderer;
import net.mistersecret312.rocketry_science.client.screen.SpaceMapScreen;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.data.orbits.ArtificialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.TransferOrbit;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import org.joml.Vector2d;

import java.text.NumberFormat;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class WidgetCelestialBody extends AbstractWidget implements Renderable
{
	private final CelestialBody body;
	private final ClientLevel level;
	private final SpaceMapScreen screen;

	public WidgetCelestialBody(int width, int height, SpaceMapScreen screen, CelestialBody body)
	{
		super(0, 0, width, height, Component.empty());
		this.body = body;
		this.level = screen.getLevel();
		this.screen = screen;

		for(Map.Entry<UUID, SpaceCraft> entry : OrbitUtil.SPACECRAFT.entrySet())
		{
			SpaceCraft craft = entry.getValue();
			if(craft.getOrbit() == null)
				return;

			CelestialBody orbitBody = OrbitUtil.getCelestialBody(craft.getOrbit().getParent(level.registryAccess()), level);
			if(body.equals(orbitBody))
			{
				WidgetSpaceCraft widgetSpaceCraft = new WidgetSpaceCraft(width, height, screen, craft.getUUID());
				screen.addRenderableWidget(widgetSpaceCraft, true);
			}
		}
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		PoseStack poseStack = graphics.pose();
		Vector2d position = getBodyPosition();

		if(!screen.selectedBody.equals(screen.star))
			position.mul(10);
		poseStack.translate(position.x, position.y, 0);

		if(body.getOrbit() != null && !screen.selectedBody.equals(body))
		{
			OrbitRenderer.drawOrbitCircle(graphics, (float) -position.x, (float) -position.y, (float) position.length(),
					1.5f, screen.getZoom(),-1);

		}

		poseStack.pushPose();
		poseStack.scale(width/16f, height/16f, 1);
		graphics.blit(body.getIcon(), -16, -16, 0, 0, 32, 32, 32, 32);
		poseStack.popPose();

		double bounds = 8*height/16f;
		if(mouseX >= position.x-bounds && mouseY >= position.y-bounds)
			if(mouseX <= position.x+bounds && mouseY <= position.y+bounds)
				graphics.drawString(Minecraft.getInstance().font, Component.literal(body.getName()), -Minecraft.getInstance().font.width(body.getName())/2,
						(int) (-bounds*2), -1, true);

		poseStack.translate(-position.x, -position.y, 0);
	}

	protected void renderOrbits(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		for(Map.Entry<UUID, SpaceCraft> entry : OrbitUtil.SPACECRAFT.entrySet())
		{
			SpaceCraft craft = entry.getValue();
			if(craft.getOrbit() == null)
				return;

			CelestialBody orbitBody = OrbitUtil.getCelestialBody(craft.getOrbit().getParent(level.registryAccess()), level);
			Vector2d orbitPos = craft.getOrbit().getPosition(level.getGameTime(), level.registryAccess());

			if(body.equals(orbitBody) && screen.selectedBody.equals(orbitBody))
			{
				if(craft.getOrbit() instanceof ArtificialOrbit)
				{
					OrbitRenderer.drawOrbitCircle(graphics, (float) 0, (float) 0, (float) orbitPos.length() / 5,
							0.5f, screen.getZoom(), 0xFF00C8FF);
					graphics.renderItem(Items.STICK.getDefaultInstance(), (int) orbitPos.x / 5 -8, (int) orbitPos.y / 5 -8);
				}
				else if(craft.getOrbit() instanceof TransferOrbit transfer)
				{
					Vector2d arrivalPosition = new Vector2d();
					Vector2d departurePosition = new Vector2d();

					CelestialBody common = OrbitUtil.getCelestialBody(transfer.getParent(level.registryAccess()), level);

					CelestialBody departureBody = OrbitUtil.getCelestialBody(transfer.getDeparture().getBody(), level);
					CelestialBody arrivalBody = OrbitUtil.getCelestialBody(transfer.getArrival().getBody(), level);

					if(transfer.getArrival().getBody().equals(transfer.getDeparture().getBody()))
					{
						ArtificialOrbit departureOrbit = new ArtificialOrbit(transfer.getDeparture().getBody(), null, transfer.getDeparture().getOrbit());
						departurePosition = OrbitUtil.getOrderPosition(transfer.getDeparture().getTick(),
								departureOrbit, level.registryAccess(), common);

						ArtificialOrbit arrivalOrbit = new ArtificialOrbit(transfer.getArrival().getBody(), null, transfer.getArrival().getOrbit());
						arrivalPosition = OrbitUtil.getOrderPosition(transfer.getArrival().getTick(),
								arrivalOrbit, level.registryAccess(), common);

						departurePosition.div(5);
						arrivalPosition.div(5);

						OrbitRenderer.drawOrbitCircle(graphics, (float) 0, (float) 0, (float) departurePosition.length(),
								0.5f, screen.getZoom(), 0xFF00C8FF);

						OrbitRenderer.drawOrbitCircle(graphics, (float) 0, (float) 0, (float) arrivalPosition.length(),
								0.5f, screen.getZoom(), 0xFF00C8FF);

					}
					else
					{
						if(departureBody.getOrbit() != null)
							departurePosition = OrbitUtil.getOrderPosition(transfer.getDeparture().getTick(),
									departureBody.getOrbit(), level.registryAccess(), screen.selectedBody);
						if(arrivalBody.getOrbit() != null)
							arrivalPosition = OrbitUtil.getOrderPosition(transfer.getArrival().getTick(),
									arrivalBody.getOrbit(), level.registryAccess(), screen.selectedBody);

						departurePosition.mul(body.getOrbitScale());
						arrivalPosition.mul(body.getOrbitScale());
					}


					double x = Mth.lerp(transfer.getProgress(level.getGameTime()), departurePosition.x, arrivalPosition.x);
					double y = Mth.lerp(transfer.getProgress(level.getGameTime()), departurePosition.y, arrivalPosition.y);

					Vector2d pos = new Vector2d(x,y);
					graphics.renderItem(Blocks.COBBLESTONE.asItem().getDefaultInstance(),
							(int) (pos.x -8), (int) (pos.y -8));

					NumberFormat format = NumberFormat.getNumberInstance();
					format.setMaximumFractionDigits(1);
					graphics.drawString(Minecraft.getInstance().font, "progress: " + format.format(transfer.getProgress(level.getGameTime())*100) + "%",
							(int) pos.x, (int) (pos.y-16), -1);
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		Vector2d position = getBodyPosition();
		if(!screen.selectedBody.equals(screen.star))
			position.mul(10);
		double bounds = 8;
		if(mouseX >= position.x-bounds && mouseY >= position.y-bounds)
			if(mouseX <= position.x+bounds && mouseY <= position.y+bounds)
			{
				this.playDownSound(Minecraft.getInstance().getSoundManager());
				if(screen.focusBody != null && screen.focusBody.equals(body))
				{
					screen.selectedBody = screen.focusBody;
					screen.markForRebuild();
					position = getBodyPosition();

					screen.panX = -position.x*screen.getZoom();
					screen.panY = -position.y*screen.getZoom();
				}
				else screen.focusBody = body;
			}
		return false;
	}

	public Vector2d getBodyPosition()
	{
		Vector2d position = new Vector2d();
		if(body.getOrbit() != null)
			position = OrbitUtil.getOrderPosition(level.getGameTime(),
					body.getOrbit(), level.registryAccess(), screen.selectedBody);

		return position;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
	{
		defaultButtonNarrationText(narrationElementOutput);
	}
}
