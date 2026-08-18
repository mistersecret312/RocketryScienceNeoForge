package net.mistersecret312.rocketry_science.client.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.data.room.Room;
import net.mistersecret312.rocketry_science.data.room.RoomManager;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.environment.EnvironmentData;
import net.mistersecret312.rocketry_science.init.AttachmentTypeInit;
import net.mistersecret312.rocketry_science.util.EnvironmentUtil;
import net.mistersecret312.rocketry_science.util.OrbitUtil;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Optional;

public class EnvironmentDataOverlay implements LayeredDraw.Layer
{
	public static final EnvironmentDataOverlay INSTANCE = new EnvironmentDataOverlay();
	@Override
	public void render(GuiGraphics graphics, DeltaTracker deltaTracker)
	{
		Player player = Minecraft.getInstance().player;
		if(player == null)
			return;
		Level level = player.level();
		if(!player.getInventory().getArmor(3).is(Items.IRON_HELMET))
			return;

		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();

		int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		Font font = Minecraft.getInstance().font;

		CelestialBody body = OrbitUtil.getCelestialBody(level);
		EnvironmentData data = EnvironmentUtil.getEnvironment(level, player.blockPosition());
		if(body == null || data == null)
			return;
		RoomManager manager = level.getData(AttachmentTypeInit.ROOM_MANAGER);
		Optional<Room> roomOptional = manager.getRoomAt(player.blockPosition());
		if(roomOptional.isPresent())
		{
			Room room = roomOptional.get();
		}

		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(1);
		format.setMinimumFractionDigits(0);

		double temperature = EnvironmentUtil.getTemperatureCelsius(level, player.blockPosition());
		String temperatureString = format.format(temperature);

		double gravity = EnvironmentUtil.getGravity(body);
		format.setMaximumFractionDigits(3);
		String gravityString = format.format(gravity);
		format.setMaximumFractionDigits(1);

		double pressure = EnvironmentUtil.getPressure(level, player.blockPosition());
		format.setMaximumFractionDigits(2);
		String pressureString = format.format(pressure);
		format.setMaximumFractionDigits(1);

		double oxygenPresence = 0;
		double roomOxygen = 0;
		double roomVolume = 0;
		if(roomOptional.isPresent())
		{
			Room room = roomOptional.get();
			oxygenPresence = (room.getCurrentOxygen()/room.getVolume())*100;
			roomOxygen = room.getCurrentOxygen();
			roomVolume = room.getVolume();
		}
		String oxygenString = format.format(oxygenPresence);
		String roomOxygenString = format.format(roomOxygen);
		String roomVolumeString = format.format(roomVolume);

		poseStack.translate(width/1.25f, height/8f, 0);
		graphics.drawString(font, Component.translatable("rocketry_science.celestial_body").append(": ").append(body.getTranslatableName()),
				0, 0, -1);
		poseStack.translate(0, 9, 0);
		graphics.drawString(font, "Temperature: " + temperatureString + " C°", 0, 0, -1);
		poseStack.translate(0, 9, 0);
		graphics.drawString(font, "Gravity: " + gravityString + " g", 0, 0, -1);
		poseStack.translate(0, 9, 0);
		graphics.drawString(font, "Pressure: " + pressureString + " atm", 0, 0, -1);

		poseStack.translate(0, 18, 0);
		graphics.drawString(font,"Air: " + oxygenString + "%", 0, 0, -1);
		poseStack.translate(0, 9,0);
		graphics.drawString(font, "O2: " + roomOxygenString, 0, 0, -1);
		poseStack.translate(0, 9,0);
		graphics.drawString(font, "Volume: " + roomVolumeString, 0, 0, -1);

		poseStack.popPose();
	}
}
