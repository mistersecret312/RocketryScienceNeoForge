package net.mistersecret312.rocketry_science.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.blocks.SeparatorBlock;
import net.mistersecret312.rocketry_science.client.screen.LaunchControllerScreen;
import net.mistersecret312.rocketry_science.client.screen.RocketAssemblyScreen;
import net.mistersecret312.rocketry_science.client.screen.SpaceMapScreen;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.data.room.Room;
import net.mistersecret312.rocketry_science.data.room.RoomManager;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.init.AttachmentTypeInit;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.mistersecret312.rocketry_science.vessel.block_data.SeparatorData;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.*;

public class ClientPacketHandler
{
	public static void syncSpacecraft(SpaceCraft craft)
	{
		OrbitUtil.addSpaceCraft(craft);
		if(Minecraft.getInstance().screen instanceof SpaceMapScreen screen)
			screen.markForRebuild();
	}

	public static void clearSpacecraft()
	{
		OrbitUtil.clearSpaceCraft();
		if(Minecraft.getInstance().screen instanceof SpaceMapScreen screen)
			screen.markForRebuild();
	}

	public static void removeSpaceCraft(UUID uuid)
	{
		OrbitUtil.removeSpaceCraft(uuid);
		if(Minecraft.getInstance().screen instanceof SpaceMapScreen screen)
			screen.markForRebuild();
	}

	public static void updateRocket(int id, Rocket rocket)
	{
		Entity entity = getEntity(id);
		if(entity instanceof RocketEntity rocketEntity)
			rocketEntity.setRocket(rocket);
	}

	public static void recieveRocketEntity(int id, Rocket rocket, String msg)
	{
		Screen screen = Minecraft.getInstance().screen;
		rocket.isInUI = true;
		if(screen instanceof LaunchControllerScreen launchControllerScreen)
			launchControllerScreen.id = id;

		if(screen instanceof RocketAssemblyScreen assemblyScreen)
		{
			assemblyScreen.rocketEntity.setRocket(rocket);
			assemblyScreen.constructionMessage = msg;

			RocketEntity renderRocket;
			if(assemblyScreen.stage != -1)
			{
				RocketEntity fakeRocketEntity = new RocketEntity(assemblyScreen.rocketEntity.level());
				LinkedHashSet<Stage> stages = new LinkedHashSet<>();
				LinkedHashSet<Stage> belowStages = new LinkedHashSet<>();
				int i = 0;
				for(Stage stage : rocket.stages)
				{
					if(assemblyScreen.stage == i)
					{
						Stage copy = new Stage(rocket, stage.palette, stage.blocks,
								stage.fluidStacks, stage.maxFluids);

						RocketEntity heightCheckRocketEntity = new RocketEntity(assemblyScreen.rocketEntity.level());
						Rocket heightCheckRocket = new Rocket(heightCheckRocketEntity, belowStages);
						for(Stage belowStage : belowStages)
							belowStage.vessel = heightCheckRocket;

						heightCheckRocketEntity.setRocket(heightCheckRocket);

						double height = heightCheckRocketEntity.makeBoundingBox().getYsize()-(i == 0 ? 1 : 0);
						HashMap<BlockPos, BlockData> blocks = new HashMap<>();
						for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
						{
							BlockPos pos = entry.getKey().offset(0, (int) -height, 0);
							BlockData data = entry.getValue();
							data.pos = pos;
							blocks.put(pos, data);
						}
						copy.blocks = blocks;
						stages.add(copy);
					}
					else
					{
						List<BlockState> palleteCopy = new ArrayList<>();
						for(BlockState state : stage.palette)
						{
							if(state.hasProperty(SeparatorBlock.EXTENDED))
								state = state.setValue(SeparatorBlock.EXTENDED, false);
							palleteCopy.add(state);
						}
						Stage belowCopy = new Stage(rocket, palleteCopy, stage.blocks, stage.fluidStacks,
								stage.maxFluids);
						for(Map.Entry<BlockPos, BlockData> entry : belowCopy.blocks.entrySet())
							entry.getValue().stage = belowCopy;

						belowStages.add(belowCopy);
					}

					i++;
				}
				Rocket fakeRocket = new Rocket(fakeRocketEntity, stages);
				fakeRocket.isInUI = true;

				fakeRocketEntity.setRocket(fakeRocket);
				renderRocket = fakeRocketEntity;
			}
			else renderRocket = assemblyScreen.rocketEntity;
			assemblyScreen.renderRocket = renderRocket;
		}
	}

	public static void updateAssembler(BlockPos pos, double progress, double maxProgress, boolean started)
	{
		ClientLevel level = Minecraft.getInstance().level;
		if(level == null)
			return;
		if(level.getBlockEntity(pos) instanceof RocketAssemblerBlockEntity assemblerBlockEntity)
		{
			assemblerBlockEntity.progress = progress;
			assemblerBlockEntity.maxProgress = maxProgress;
			assemblerBlockEntity.started = started;
		}
	}

	public static void recieveSpaceCraft(Rocket rocket)
	{
		Screen screen = Minecraft.getInstance().screen;
		rocket.isInUI = true;
		if(screen instanceof SpaceMapScreen mapScreen)
		{
			RocketEntity rocketEntity = new RocketEntity(mapScreen.getLevel());
			rocketEntity.setRocket(rocket);
			mapScreen.spaceCraftRocket = rocketEntity;
		}
	}

	public static <T extends Entity> T getEntity(int id)
	{
		ClientLevel level = Minecraft.getInstance().level;
		if(level == null)
			return null;
		Entity entity = level.getEntity(id);
		return (T) entity;
	}

	public static void updateRoom(UUID uuid, double oxygen, double volume,
								  double targetAtmosphere, double targetTemperature)
	{
		ClientLevel level = Minecraft.getInstance().level;
		if(level != null)
		{
			RoomManager manager = level.getData(AttachmentTypeInit.ROOM_MANAGER);
			Room room = manager.getRoom(uuid);
			if(room != null)
			{
				room.setCurrentOxygen(level, (float) oxygen);
				room.setVolume((float) volume);
				room.setTargetAtmosphere(level, targetAtmosphere);
				room.setTargetTemperature(level, targetTemperature);
			}
		}
	}
}
