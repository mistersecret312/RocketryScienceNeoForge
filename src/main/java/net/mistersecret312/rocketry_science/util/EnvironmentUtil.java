package net.mistersecret312.rocketry_science.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.rocketry_science.RocketryScience;
import net.mistersecret312.rocketry_science.data.SpaceCraft;
import net.mistersecret312.rocketry_science.data.room.Room;
import net.mistersecret312.rocketry_science.data.room.RoomManager;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.environment.EnvironmentData;
import net.mistersecret312.rocketry_science.environment.modifiers.ModifierConfig;
import net.mistersecret312.rocketry_science.init.AttachmentTypeInit;
import net.mistersecret312.rocketry_science.init.BlockInit;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.VesselData;

public class EnvironmentUtil
{
	public static final EnvironmentData EARTH = new EnvironmentData(1, 1, 0, new EnvironmentData.TemperatureGradient(260, 300));
	public static final EnvironmentData LUNA = new EnvironmentData(0, 0.165, 0.06, new EnvironmentData.TemperatureGradient(140, 400));

	public static EnvironmentData getEnvironment(Entity entity)
	{
		return getEnvironment(entity.level(), entity.blockPosition());
	}

	public static EnvironmentData getEnvironment(Level level, BlockPos pos)
	{
		RoomManager roomManager = level.getData(AttachmentTypeInit.ROOM_MANAGER);
		if(roomManager.getRoomAt(pos).isPresent())
		{
			Room room = roomManager.getRoomAt(pos).get();
			return new EnvironmentData(room.getTargetAtmosphere(), getEnvironment(level).getGravity(), 0,
					new EnvironmentData.TemperatureGradient(room.getTargetTemperature(), room.getTargetTemperature()));
		}
		return getEnvironment(level);
	}

	public static EnvironmentData getEnvironment(Level level)
	{
		CelestialBody body = OrbitUtil.getCelestialBody(level);
		return getEnvironment(body);
	}

	public static EnvironmentData getEnvironment(CelestialBody body)
	{
		if(body == null)
			return new EnvironmentData(EARTH);
		if(body.getEnvironment() != null)
			return body.getEnvironment();

		return new EnvironmentData(body.hasAtmosphere() ? EARTH : LUNA);
	}

	public static double getTemperatureKelvin(Level level, BlockPos pos)
	{
		EnvironmentData environmentData = getEnvironment(level);
		double angle = level.getTimeOfDay(1f);
		double skyLight = (Mth.cos((float) (angle*2*Math.PI))+1)/2;
		double temperature = environmentData.getTemperatureGradient().getTemperature(skyLight);

		RoomManager roomManager = level.getData(AttachmentTypeInit.ROOM_MANAGER);
		if(roomManager.getRoomAt(pos).isPresent())
		{
			Room room = roomManager.getRoomAt(pos).get();
			double percentage = room.getFilledPercentage();
			temperature = Mth.clampedLerp(temperature, room.getTargetTemperature(), percentage);
		}

		return temperature;
	}

	public static double getTemperatureCelsius(Level level, BlockPos pos)
	{
		return getTemperatureKelvin(level, pos)-273d;
	}

	public static double getTemperatureFahrenheit(Level level, BlockPos pos)
	{
		return 1.8d*getTemperatureCelsius(level, pos)+32d;
	}
	
	public static double getSeaLevelPressure(Level level)
	{
		return getEnvironment(level).getPressureSeaLevel();
	}

	public static double getPressure(VesselData vessel)
	{
		Level level = vessel.level();
		double height = level.getSeaLevel();
		if(vessel instanceof SpaceCraft)
			height = OrbitUtil.getSpaceHeight(level)*2;
		else if(vessel instanceof Rocket rocket)
			height = rocket.getRocketEntity().position().y;

		return getPressure(level, new BlockPos(0, (int) height, 0));
	}

	public static double getPressure(Level level, BlockPos pos)
	{
		double spaceY = OrbitUtil.getSpaceHeight(level);
		double seaLevelY = level.getSeaLevel();
		double seaLevelPressure = getSeaLevelPressure(level);
		if(seaLevelPressure == 0)
		{
			RoomManager manager = level.getData(AttachmentTypeInit.ROOM_MANAGER);
			if(manager.getRoomAt(pos).isPresent())
			{
				Room room = manager.getRoomAt(pos).get();
				double targetPressure = room.getTargetAtmosphere();
				return Mth.clampedLerp(0, targetPressure, room.getFilledPercentage());
			}
		}
		if(pos.getY() >= spaceY || seaLevelPressure == 0)
			return 0d;

		double vacuumThreshold = 0.001;
		double scaleHeight = (seaLevelY - spaceY) / Math.log(vacuumThreshold / seaLevelPressure);

		double pressure =  seaLevelPressure * Math.exp(-(pos.getY() - seaLevelY) / scaleHeight);
		if(level.dimension().equals(Level.OVERWORLD) && pressure >= 1.0d)
			return 1.0d;

		RoomManager manager = level.getData(AttachmentTypeInit.ROOM_MANAGER);
		if(manager.getRoomAt(pos).isPresent())
		{
			Room room = manager.getRoomAt(pos).get();
			double targetPressure = room.getTargetAtmosphere();
			pressure = Mth.clampedLerp(pressure, targetPressure, room.getFilledPercentage());
		}

		return pressure;
	}

	public static double getGravity(CelestialBody body)
	{
		return getEnvironment(body).getGravity();
	}
	public static double getGravityMS2(CelestialBody body)
	{
		return getEnvironment(body).getGravity()*9.8d;
	}
	public static double getGravitationalParameter(CelestialBody body)
	{
		return getGravityMS2(body)*body.getRadius()*body.getRadius();
	}

	public static void airAffect(LivingEntity living)
	{
		Level level = living.level();
		if(!level.isClientSide())
		{
			CelestialBody body = OrbitUtil.getCelestialBody(level);
			if(body == null)
				return;
			RoomManager manager = level.getData(AttachmentTypeInit.ROOM_MANAGER);
			boolean canBreathe = body.isBreathableAtmosphere();
			if(manager.getRoomAt(living.blockPosition()).isPresent())
			{
				Room room = manager.getRoomAt(living.blockPosition()).get();
				room.setCurrentOxygen(level, room.getCurrentOxygen()-1);
				canBreathe = room.getFilledPercentage() >= 0.1;
			}

			if(!canBreathe && level.getGameTime() % 20 == 0)
				living.hurt(living.damageSources().drown(), 1);
		}
	}

	public static void modifiersAffect(Entity entity)
	{
		CelestialBody body = OrbitUtil.getCelestialBody(entity.level());
		if(body != null)
		{
			for(ModifierConfig modifier : body.getModifiers())
				modifier.tick(entity);
		}
	}

	public static void modifiersAffect(Level level)
	{
		CelestialBody body = OrbitUtil.getCelestialBody(level);
		if(body != null)
		{
			for(ModifierConfig modifier : body.getModifiers())
				modifier.tick(level);
		}
	}

	public static boolean isPermeable(BlockState state)
	{
		return state.isAir() || state.is(RocketryScience.AIR_FLOWS_THROUGH) || state.is(BlockInit.OXYGEN_VENT);
	}

	public static boolean canGasFlow(BlockGetter level, BlockPos fromPos, BlockPos toPos, Direction flowDir)
	{
		BlockState fromState = level.getBlockState(fromPos);
		BlockState toState = level.getBlockState(toPos);

		boolean fromPermeable = isPermeable(fromState);
		boolean toPermeable = isPermeable(toState);

		if (fromPermeable && toPermeable)
			return true;

		VoxelShape fromShape = fromPermeable ? Shapes.empty() : fromState.getCollisionShape(level, fromPos);
		VoxelShape toShape = toPermeable ? Shapes.empty() : toState.getCollisionShape(level, toPos);

		if (!fromPermeable && Block.isFaceFull(fromShape, flowDir))
			return false;

		if (!toPermeable && Block.isFaceFull(toShape, flowDir.getOpposite()))
			return false;

		return !Shapes.faceShapeOccludes(fromShape, toShape);
	}
}
