package net.mistersecret312.rocketry_science.client.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.mistersecret312.rocketry_science.client.OrbitRenderer;
import net.mistersecret312.rocketry_science.client.screen.SpaceMapScreen;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.data.orbits.ArtificialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.TransferOrbit;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.network.packets.ServerBoundRequestSpacecraftEntityPacket;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector2d;

import java.text.NumberFormat;
import java.util.Map;
import java.util.UUID;

public class WidgetSpaceCraft extends AbstractWidget implements Renderable
{
	private final UUID craftUUID;
	private final SpaceCraft craft;
	private final ClientLevel level;
	private final SpaceMapScreen screen;

	public WidgetSpaceCraft(int width, int height, SpaceMapScreen screen, UUID craft)
	{
		super(0, 0, width, height, Component.empty());
		this.craftUUID = craft;
		this.craft = OrbitUtil.SPACECRAFT.get(craftUUID);
		this.level = screen.getLevel();
		if(this.craft.level() == null)
			this.craft.setLevel(level);
		this.screen = screen;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		if(craft.getOrbit() == null)
			return;

		CelestialBody orbitBody = OrbitUtil.getCelestialBody(craft.getOrbit().getParent(level.registryAccess()), level);
		if(!orbitBody.equals(screen.selectedBody))
			return;

		PoseStack poseStack = graphics.pose();
		Vector2d parentPos = getParentPosition();
		Vector2d craftPos = getCraftPosition();

		poseStack.pushPose();
		poseStack.translate(parentPos.x, parentPos.y, 0);

		if (craft.getOrbit() instanceof ArtificialOrbit)
		{
			OrbitRenderer.drawOrbitCircle(graphics, 0, 0, (float) craftPos.length(), 0.5f, 0xFF00C8FF);
			graphics.renderItem(Items.STICK.getDefaultInstance(), (int) craftPos.x - 8, (int) craftPos.y - 8);
		}
		else if (craft.getOrbit() instanceof TransferOrbit transfer)
		{
			Vector2d arrivalPosition = new Vector2d();
			Vector2d departurePosition = new Vector2d();

			CelestialBody common = OrbitUtil.getCelestialBody(transfer.getParent(level.registryAccess()), level);
			CelestialBody departureBody = OrbitUtil.getCelestialBody(transfer.getDeparture().getBody(), level);
			CelestialBody arrivalBody = OrbitUtil.getCelestialBody(transfer.getArrival().getBody(), level);

			if (transfer.getArrival().getBody().equals(transfer.getDeparture().getBody()))
			{
				ArtificialOrbit departureOrbit = new ArtificialOrbit(transfer.getDeparture().getBody(), null, transfer.getDeparture().getOrbit());
				departurePosition = OrbitUtil.getOrderPosition(transfer.getDeparture().getTick(), departureOrbit, level.registryAccess(), common);

				ArtificialOrbit arrivalOrbit = new ArtificialOrbit(transfer.getArrival().getBody(), null, transfer.getArrival().getOrbit());
				arrivalPosition = OrbitUtil.getOrderPosition(transfer.getArrival().getTick(), arrivalOrbit, level.registryAccess(), common);

				departurePosition.div(5);
				arrivalPosition.div(5);

				OrbitRenderer.drawOrbitCircle(graphics, 0, 0, (float) departurePosition.length(), 0.5f, 0xFF00C8FF);
				OrbitRenderer.drawOrbitCircle(graphics, 0, 0, (float) arrivalPosition.length(), 0.5f, 0xFF00C8FF);
			}
			else
			{
				if (departureBody.getOrbit() != null)
					departurePosition = OrbitUtil.getOrderPosition(transfer.getDeparture().getTick(), departureBody.getOrbit(), level.registryAccess(), screen.selectedBody);
				if (arrivalBody.getOrbit() != null)
					arrivalPosition = OrbitUtil.getOrderPosition(transfer.getArrival().getTick(), arrivalBody.getOrbit(), level.registryAccess(), screen.selectedBody);

				departurePosition.mul(orbitBody.getOrbitScale());
				arrivalPosition.mul(orbitBody.getOrbitScale());
			}

			OrbitRenderer.drawOrbitTransfer(graphics, departurePosition, arrivalPosition, 0xFF00C8FF);

			graphics.renderItem(Blocks.COBBLESTONE.asItem().getDefaultInstance(), (int) craftPos.x - 8, (int) craftPos.y - 8);

			NumberFormat format = NumberFormat.getNumberInstance();
			format.setMaximumFractionDigits(1);
			graphics.drawString(Minecraft.getInstance().font, "progress: " + format.format(transfer.getProgress(level.getGameTime()) * 100) + "%",
					(int) craftPos.x, (int) (craftPos.y - 16), -1);
		}

		// Handle Hover and Name Rendering
		double absX = parentPos.x + craftPos.x;
		double absY = parentPos.y + craftPos.y;
		double bounds = 8;

		if (mouseX >= absX - bounds && mouseY >= absY - bounds && mouseX <= absX + bounds && mouseY <= absY + bounds)
		{
			// Offset Y by 26 so it doesn't overlap with the transfer progress text
			graphics.drawString(Minecraft.getInstance().font, Component.literal(craft.getName()),
					(int) craftPos.x - Minecraft.getInstance().font.width(craft.getName()) / 2,
					(int) craftPos.y - 26, -1, true);
		}

		poseStack.popPose();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(craft.getOrbit() == null)
			return false;
		CelestialBody orbitBody = OrbitUtil.getCelestialBody(craft.getOrbit().getParent(level.registryAccess()), level);
		if(!orbitBody.equals(screen.selectedBody))
			return false;

		Vector2d absPos = new Vector2d(getParentPosition()).add(getCraftPosition());
		double bounds = 8;

		if(mouseX >= absPos.x - bounds && mouseY >= absPos.y - bounds)
			if(mouseX <= absPos.x + bounds && mouseY <= absPos.y + bounds)
			{
				this.playDownSound(Minecraft.getInstance().getSoundManager());

				if(screen.selectedCraft == null || !screen.selectedCraft.equals(this.craftUUID))
				{
					screen.selectedCraft = this.craftUUID;
					screen.panX = -absPos.x * screen.getZoom();
					screen.panY = -absPos.y * screen.getZoom();

					PacketDistributor.sendToServer(new ServerBoundRequestSpacecraftEntityPacket(this.craftUUID));
				}
				else screen.selectedCraft = null;

				return true;
			}
		return false;
	}

	private Vector2d getParentPosition()
	{
		CelestialBody orbitBody = OrbitUtil.getCelestialBody(craft.getOrbit().getParent(level.registryAccess()), level);
		Vector2d position = new Vector2d();
		if(orbitBody.getOrbit() != null)
			position = OrbitUtil.getOrderPosition(level.getGameTime(), orbitBody.getOrbit(), level.registryAccess(), screen.selectedBody);

		if(!screen.selectedBody.equals(screen.star))
			position.mul(10);

		return position;
	}

	private Vector2d getCraftPosition()
	{
		if (craft.getOrbit() instanceof ArtificialOrbit)
		{
			Vector2d orbitPos = craft.getOrbit().getPosition(level.getGameTime(), level.registryAccess());
			return new Vector2d(orbitPos.x / 5, orbitPos.y / 5);
		}
		else if (craft.getOrbit() instanceof TransferOrbit transfer)
		{
			Vector2d arrivalPosition = new Vector2d();
			Vector2d departurePosition = new Vector2d();

			CelestialBody common = OrbitUtil.getCelestialBody(transfer.getParent(level.registryAccess()), level);
			CelestialBody departureBody = OrbitUtil.getCelestialBody(transfer.getDeparture().getBody(), level);
			CelestialBody arrivalBody = OrbitUtil.getCelestialBody(transfer.getArrival().getBody(), level);
			CelestialBody orbitBody = OrbitUtil.getCelestialBody(craft.getOrbit().getParent(level.registryAccess()), level);

			if (transfer.getArrival().getBody().equals(transfer.getDeparture().getBody()))
			{
				ArtificialOrbit departureOrbit = new ArtificialOrbit(transfer.getDeparture().getBody(), null, transfer.getDeparture().getOrbit());
				departurePosition = OrbitUtil.getOrderPosition(transfer.getDeparture().getTick(), departureOrbit, level.registryAccess(), common);

				ArtificialOrbit arrivalOrbit = new ArtificialOrbit(transfer.getArrival().getBody(), null, transfer.getArrival().getOrbit());
				arrivalPosition = OrbitUtil.getOrderPosition(transfer.getArrival().getTick(), arrivalOrbit, level.registryAccess(), common);

				departurePosition.div(5);
				arrivalPosition.div(5);
			}
			else
			{
				if (departureBody.getOrbit() != null)
					departurePosition = OrbitUtil.getOrderPosition(transfer.getDeparture().getTick(), departureBody.getOrbit(), level.registryAccess(), screen.selectedBody);
				if (arrivalBody.getOrbit() != null)
					arrivalPosition = OrbitUtil.getOrderPosition(transfer.getArrival().getTick(), arrivalBody.getOrbit(), level.registryAccess(), screen.selectedBody);

				departurePosition.mul(orbitBody.getOrbitScale());
				arrivalPosition.mul(orbitBody.getOrbitScale());
			}

			double x = Mth.lerp(transfer.getProgress(level.getGameTime()), departurePosition.x, arrivalPosition.x);
			double y = Mth.lerp(transfer.getProgress(level.getGameTime()), departurePosition.y, arrivalPosition.y);
			return new Vector2d(x, y);
		}
		return new Vector2d();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
	{
		defaultButtonNarrationText(narrationElementOutput);
	}
}