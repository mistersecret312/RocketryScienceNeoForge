package net.mistersecret312.rocketry_science.vessel;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.network.ClientPacketHandler;
import net.mistersecret312.rocketry_science.network.packets.ClientBoundRocketUpdatePacket;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.mistersecret312.rocketry_science.util.OrbitalMath;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.mistersecret312.rocketry_science.vessel.block_data.RocketEngineData;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Rocket extends VesselData
{
	public static final StreamCodec<? super RegistryFriendlyByteBuf, Rocket> STREAM_CODEC = new StreamCodec<>()
	{
		@Override
		public Rocket decode(RegistryFriendlyByteBuf buffer)
		{
			return Rocket.fromNetwork(buffer);
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buffer, Rocket rocket)
		{
			rocket.toNetwork(buffer);
		}
	};

	public VesselState state;
	public LinkedHashSet<Stage> stages;
	public RocketEntity rocket;

	public boolean canLand = false;
	public double landingDeltaV = 0;
	public double takeoffDeltaV = 0;

	public Rocket(RocketEntity rocketEntity, LinkedHashSet<Stage> stages)
	{
		this.rocket = rocketEntity;
		this.stages = stages;
		this.state = VesselState.IDLE;
	}

	@Override
	public void tick(Level level)
	{
		if(!level.isClientSide())
			PacketDistributor.sendToPlayersTrackingEntity(rocket, new ClientBoundRocketUpdatePacket(rocket.getId(), this));
		if(level.isClientSide())
			return;

		if(stages.isEmpty())
			rocket.discard();

		for(Stage stage : stages)
			stage.tick(level);

		switch(state)
		{
			case IDLE ->
			{
				if(getCurrentStage() == null)
					return;

				if(this.canLand() && this.getRocketEntity().getDeltaMovement().y < 0)
					setState(VesselState.LANDING);
			}
			case LANDING ->
			{
				land(level);
			}
			case TAKEOFF ->
			{
				takeoff(level);
			}
			case STAGING ->
			{
				stage(level);
			}
			case COASTING ->
			{
				double currentLeftDeltaV = getCurrentStage().calculateDeltaV();
				currentLeftDeltaV -= landingDeltaV;
				//getCurrentStage().consumeFuelByDeltaV(currentLeftDeltaV);

				if(this.stages.size() > 1)
				{
					for(Stage stage : this.stages)
						System.out.println("Orbital - " + stage.calculateDeltaV());

					stage(level);
				}

				if(!(this.getCurrentStage().calculateDeltaV() > 0))
				{
					setState(VesselState.IDLE);
					return;
				}

				double leoHeight = 300*1000;
				double deltaVRequired = OrbitalMath.getOrbitDeltaV(OrbitUtil.getCelestialBody(level), leoHeight)-currentLeftDeltaV;

				System.out.println("Has - " + this.getCurrentStage().calculateDeltaV() + " - Needed - " + deltaVRequired);

				if(this.getCurrentStage().calculateDeltaV() > deltaVRequired)
				{
					System.out.println("Fuel mass before " + getCurrentStage().getFuelMass());
					System.out.println("Fuel mass to be consumed " + OrbitalMath.deltaVToFuelMass(getCurrentStage(), deltaVRequired));
//					getCurrentStage().consumeFuelByDeltaV(deltaVRequired);
					System.out.println("Fuel mass after " + getCurrentStage().getFuelMass());

					System.out.println("Capable of being in orbit, leftover - " + (this.getCurrentStage().calculateDeltaV()));
//					setState(VesselState.ORBIT);
//					return;
				}
				else setState(VesselState.IDLE);

				if(this.getCurrentStage() == null)
					return;

				if(this.getCurrentStage().getFuelMass() == 0)
					stage(level);

				if(this.canLand())
					setState(VesselState.LANDING);
			}
			case ORBIT ->
			{
				double leoHeight = 300*1000;
				if(level.getServer() == null)
					return;

				this.toggleEngines(false);
			}
		}
	}

	public void takeoff(Level level)
	{
		double altitude = getAltitude(level);
		double spaceY = OrbitUtil.getSpaceHeight(level);

		if(rocket.getY() <= spaceY)
		{
			toggleEngines(true);
			double engineThrust = 0.0D;
			double hover = getHoverThrust();
			double height = this.getRocketEntity().makeBoundingBox().getYsize();
			if(altitude < height)
			{
				engineThrust = hover*1.1;
				setEngineThrust(engineThrust);
			}
			else
			{
				engineThrust = hover*(altitude/height);
				setEngineThrust(engineThrust);
			};
		}
		else
		{
			System.out.println("ORBITAL - " + altitude + " DeltaV Left - " + getCurrentStage().calculateDeltaV());
			this.toggleEngines(false);
			rocket.setDeltaMovement(0, 0, 0);
			setState(VesselState.COASTING);
			canLand = true;
		}
	}

	public void land(Level level)
	{
		double altitude = getAltitude(level);
		double velocity = rocket.getDeltaMovement().y;
		double g = 0.025;
		double twr = getMaxTWR();
		double maxNetAccel = (twr - 1.0) * g;

		double thrustLevel = 0.0;

		double finalTouchdownAltitude = this.rocket.makeBoundingBox().getYsize()*1.5d;

		double brakingDescentSpeed = -1.0;
		double safeLandingSpeed = -0.25;

		double altitudeBuffer = 20.0;

		if(altitude > OrbitUtil.getSpaceHeight(level) || velocity > 0)
		{
			toggleEngines(false);
			return;
		}

		if (altitude > 0.25 && velocity < 0 && maxNetAccel > 0)
		{
			double stoppingDistance = (velocity * velocity) / (2.0 * maxNetAccel);
			stoppingDistance = Math.max(stoppingDistance, this.rocket.makeBoundingBox().getYsize()*1.5);

			if (altitude <= (stoppingDistance + altitudeBuffer))
			{
				toggleEngines(true);

				double desiredVelocity;
				double Kp;
				double Kd;

				if (altitude <= finalTouchdownAltitude)
				{
					desiredVelocity = safeLandingSpeed;
					Kp = 0.7;
					Kd = 0.3;
				}
				else
				{
					desiredVelocity = brakingDescentSpeed;
					Kp = 0.4;
					Kd = 0.15;
				}

				double error = desiredVelocity - velocity;
				double accelCmd = Kp * error - Kd * velocity;

				double requiredThrustAccel = accelCmd + g;
				double maxThrustAccel = twr * g;

				double thrustFraction = requiredThrustAccel / maxThrustAccel;

				thrustLevel = Mth.clamp(thrustFraction, 0.0, 1.0);

			}
			else
			{
				toggleEngines(false);
				thrustLevel = 0.0;
			}

			setEngineThrust(thrustLevel);

		} else if (altitude <= 0.25) {
			toggleEngines(false);
			setState(VesselState.IDLE);
			setEngineThrust(0.0);
			System.out.println("LANDED" + " DeltaV Left - " + getCurrentStage().calculateDeltaV());
			canLand = false;
		}
	}

	public void stage(Level level)
	{
		Stage oldDetach = getCurrentStage();
		if(oldDetach != null)
		{
			RocketEntity rocketEntityNew = new RocketEntity(level);
			Rocket rocketNew = new Rocket(rocketEntityNew, new LinkedHashSet<>());
			Stage stageNew = new Stage(rocketNew);

			stageNew.palette = oldDetach.palette;
			stageNew.blocks = oldDetach.blocks;

			Iterator<Map.Entry<BlockPos, BlockData>> iterator = stageNew.blocks.entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry<BlockPos, BlockData> entry = iterator.next();
				entry.getValue().stage = stageNew;
			}

			rocketNew.stages.add(stageNew);

			this.stages.remove(oldDetach);
			rocketEntityNew.setRocket(rocketNew);

			double height = rocketEntityNew.makeBoundingBox().getYsize();

			BlockPos origin = null;
			Iterator<Stage> stages = this.stages.iterator();
			while(stages.hasNext())
			{
				Stage stage = stages.next();
				HashMap<BlockPos, BlockData> blocks = new HashMap<>();
				for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
				{
					if(origin == null)
						origin = entry.getKey();

					BlockPos pos = entry.getKey().offset(0, (int) -height, 0);
					BlockData data = entry.getValue();
					data.pos = pos;
					blocks.put(pos, data);
				}
				stage.blocks = blocks;
			}

			rocketEntityNew.setPos(this.getRocketEntity().position());
			this.getRocketEntity().setPos(this.getRocketEntity().position().add(0, height+4, 0));
			this.getRocketEntity().getRocket().setState(VesselState.COASTING);
			rocketEntityNew.getRocket().setState(VesselState.IDLE);
			rocketEntityNew.setDeltaMovement(this.getRocketEntity().getDeltaMovement().add(0, -0.8, 0));

			level.addFreshEntity(rocketEntityNew);
		}
	}

	public double getAltitude(Level level)
	{
		return rocket.position().y-level.getHeight(Heightmap.Types.MOTION_BLOCKING, rocket.blockPosition().getX(), rocket.blockPosition().getZ());
	}

	public boolean hasFuel()
	{
		Stage local = getCurrentStage();
		if(local == null)
			return false;

		for(Map.Entry<BlockPos, BlockData> entry : local.blocks.entrySet())
		{
			if(entry.getValue() instanceof RocketEngineData engineData)
			{
				if(engineData.hasFuel())
					return true;
			}
		}

		return false;
	}

	public void setEngineThrust(double thrust)
	{
		for(Stage stage : this.stages)
			for(Map.Entry<BlockPos, BlockData > entry :stage.blocks.entrySet())
			{
				if(entry.getValue() instanceof RocketEngineData engine)
				{
					engine.thrustPercentage = Math.min(1.0, thrust);
				}
			}
	}

	public Stage getCurrentStage()
	{
		Stage stage = null;
		for (Stage stageO : this.stages)
		{
			stage = stageO;
			break;
		}
		return stage;
	}

	public double getHoverThrust()
	{
		return (getMassKilogram()*9.80665)/(getMaxThrustKiloNewtons()*1000);
	}

	public void toggleEngines(boolean state)
	{
		Stage stage = getCurrentStage();
		if(stage == null) return;

		for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
		{
			if(entry.getValue() instanceof RocketEngineData engine)
			{
				engine.enabled = state;
			}
		}
	}

	public double getMaxTWR()
	{
		double thrust = getMaxThrustKiloNewtons()*1000;
		double mass = getMassKilogram()*9.80665;
		return thrust / mass;
	}

	public double getMaxThrustKiloNewtons()
	{
		double thrustkN = 0;
		Stage stage = getCurrentStage();
		if(stage == null) return 0;

		for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
		{
			if(entry.getValue() instanceof RocketEngineData engine)
			{
				thrustkN += engine.getThrustkN();
			}
		}
		return (thrustkN);
	}

	public int getAverageFuelUsage()
	{
		int fuelUse = 0;
		int amount = 0;

		Stage current = getCurrentStage();

		for(Map.Entry<BlockPos, BlockData> entry : current.blocks.entrySet())
		{
			if(entry.getValue() instanceof RocketEngineData data)
			{
				fuelUse += data.calculateMaxFuelUsage();
				amount++;
			}
		}

		if(amount == 0) return 0;
		return fuelUse / amount;
	}

	public double getAverageIsp()
	{
		double Isp = 0;
		int amount = 0;

		Stage current = this.getCurrentStage();
		if(current == null) return 0;

		for(Map.Entry<BlockPos, BlockData> entry : current.blocks.entrySet())
		{
			if(entry.getValue() instanceof RocketEngineData data)
			{
				Isp += data.getIsp();
				amount++;
			}
		}
		if(amount == 0) return 0;
		return Isp / amount;
	}

	public double getMassKilogram()
	{
		double mass = 0;
		for(Stage stage : this.stages)
			mass += stage.getTotalMass();
		return mass;
	}

	public double getMassDryKilogram()
	{
		double mass = 0;
		for(Stage stage : this.stages)
			mass += stage.getTotalDryMass();
		return mass;
	}

	public int getEngineAmount()
	{
		int amount = 0;
		Stage stage = getCurrentStage();
		if(stage == null)
			return 0;

		for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
			if(entry.getValue() instanceof RocketEngineData)
				amount++;


		return amount;
	}

	public boolean canLand()
	{
		return (canLand && getMaxTWR() > 1.0) || landingDeltaV != 0;
	}

	public void toNetwork(RegistryFriendlyByteBuf buffer)
	{
		buffer.writeInt(this.rocket.getId());
		buffer.writeEnum(this.state);
		buffer.writeCollection(stages, (writer, stage) -> stage.toNetwork((RegistryFriendlyByteBuf) writer));
		buffer.writeBoolean(this.canLand);
		buffer.writeDouble(this.landingDeltaV);
		buffer.writeDouble(this.takeoffDeltaV);
	}

	public static Rocket fromNetwork(RegistryFriendlyByteBuf buffer)
	{
		RocketEntity rocketEntity = ClientPacketHandler.getEntity(buffer.readInt());
		VesselState state = buffer.readEnum(VesselState.class);
		Rocket rocket = new Rocket(rocketEntity, new LinkedHashSet<>());
		LinkedHashSet<Stage> stages = buffer.readCollection(LinkedHashSet::new, reader -> Stage.fromNetwork(buffer, rocket));

		rocket.stages = stages;
		rocket.state = state;
		rocket.canLand = buffer.readBoolean();
		rocket.landingDeltaV = buffer.readDouble();
		rocket.takeoffDeltaV = buffer.readDouble();
		return rocket;
	}

	public CompoundTag save()
	{
		CompoundTag tag = new CompoundTag();

		tag.putString("state", state.toString().toLowerCase());

		ListTag stageTag = new ListTag();
		for(Stage stage : stages)
			stageTag.add(stage.save());
		tag.put("stages", stageTag);

		tag.putBoolean("can_land", this.canLand);
		tag.putDouble("landing_deltav", this.landingDeltaV);
		tag.putDouble("takeoff_deltav", this.takeoffDeltaV);

		return tag;
	}

	public void load(CompoundTag tag, MinecraftServer server)
	{
		this.getRocketEntity().level();
		this.state = VesselState.valueOf(tag.getString("state").toUpperCase());

		ListTag stageTag = tag.getList("stages", Tag.TAG_COMPOUND);
		LinkedHashSet<Stage> stages = new LinkedHashSet<>();
		for(Tag listTag : stageTag)
		{
			Stage stage = new Stage(this);
			stage.load((CompoundTag) listTag, server);
			stages.add(stage);
		}
		this.stages = stages;
		this.canLand = tag.getBoolean("can_land");
		this.landingDeltaV = tag.getDouble("landing_deltav");
		this.takeoffDeltaV = tag.getDouble("takeoff_deltav");
	}

	public VesselState getState()
	{
		return state;
	}

	public void setState(VesselState state)
	{
		this.state = state;
	}

	public RocketEntity getRocketEntity()
	{
		return rocket;
	}

	@Override
	public LinkedHashSet<Stage> getStages()
	{
		return stages;
	}

	@Override
	public void addStage(Stage stage)
	{
		this.stages.add(stage);
	}

	@Override
	public void removeStage(Stage stage)
	{
		this.stages.remove(stage);
	}

	@Override
	public Level level()
	{
		return rocket.level();
	}

	@Override
	public boolean isInSpace()
	{
		return getAltitude(level()) >= OrbitUtil.getSpaceHeight(level());
	}

	public void landingSimulation()
	{
		AtomicInteger takeOffFuel = new AtomicInteger(0);
		double takeoffDeltaV = takeoffSimulation(takeOffFuel);

		int fuelUsed = 0;
		int ticks = 0;
		Stage stage = getCurrentStage();

		double thrust = getMaxThrustKiloNewtons();
		double fuelFlow = getAverageFuelUsage();
		double mass = stage.getTotalMass()-takeOffFuel.get();
		double massAccounted = stage.getTotalMass()-takeOffFuel.get();

		double altitude = OrbitUtil.getSpaceHeight(rocket.level())-rocket.level().getHeight(Heightmap.Types.WORLD_SURFACE, rocket.blockPosition().getX(), rocket.blockPosition().getZ());
		double acceleration = 0;
		double velocity = 0;

		double safeLandingSpeed = -0.1;
		double g = 0.025;

		int ticksRan = 0;

		while(altitude > 0.25)
		{
			if(ticks >= 1000)
			{
				System.out.println("Simulation took too long!");
				return;
			}
			if(altitude < rocket.makeBoundingBox().getYsize() && velocity > safeLandingSpeed)
				break;

			velocity -= g;
			velocity = Mth.clamp(velocity, -4, 0);

			double twr = (thrust*1000)/(massAccounted*9.80665);
			double netAccelMax = (twr - 1.0) * g;

			double stoppingDistance = 0;
			if (netAccelMax > 0 && velocity < 0)
			{
				stoppingDistance = Math.max(rocket.makeBoundingBox().getYsize(), (velocity * velocity) / (2.0 * netAccelMax))*Math.max(1, 0.5*getMaxTWR());;
			}

			double thrustLevel = 0.0;
			if (altitude <= stoppingDistance + this.rocket.makeBoundingBox().getYsize())
			{
				double desiredVelocity = safeLandingSpeed;
				double error = desiredVelocity - velocity;

				double Kp = 0.5;
				double Kd = 0.2;

				double accelCmd = Kp * error - Kd * velocity;
				double thrustFraction = (g + accelCmd) / (twr * g);
				thrustLevel = Mth.clamp(thrustFraction, 0.0, 1.0);

				fuelUsed += (int) (fuelFlow * thrustLevel) * stage.getFuelTypeAmount() * getEngineAmount();
				massAccounted -= (int) (fuelFlow * thrustLevel) * stage.getFuelTypeAmount() * getEngineAmount();

				ticksRan++;
			}
			else thrustLevel = 0.0;

			acceleration = 0.025*twr*thrustLevel;
			velocity += acceleration;

			altitude += velocity;
			altitude = Math.max(0, altitude);

			ticks++;
		}

		double Isp = getAverageIsp();
		double massRatio = mass/(massAccounted);
		double deltaV = 9.8*Isp*Math.log(massRatio);

		System.out.println("Simulated landing fuel - " + fuelUsed);
		System.out.println("Landing DeltaV to fuel - " + OrbitalMath.deltaVToFuelMass(stage, deltaV));
		System.out.println("Takeoff DeltaV to fuel - " + OrbitalMath.deltaVToFuelMass(stage, takeoffDeltaV));
		System.out.println("Simulated deltaV - " + takeoffDeltaV + " Landing - " + deltaV);

		this.landingDeltaV = deltaV;
		this.takeoffDeltaV = takeoffDeltaV;
	}

	public double takeoffSimulation(AtomicInteger fuel)
	{
		int fuelUsed = 0;
		int ticks = 0;
		Stage stage = getCurrentStage();

		double thrust = getMaxThrustKiloNewtons();
		double height = this.getRocketEntity().makeBoundingBox().getYsize();
		double fuelFlow = getAverageFuelUsage();
		double mass = stage.getTotalMass();
		double massAccounted = stage.getTotalMass();
		double rocketMass = getMassKilogram();

		double altitude = 0;
		double acceleration = 0;
		double velocity = 0;
		double spaceY = OrbitUtil.getSpaceHeight(rocket.level())-rocket.getY();

		if(stage == null)
			return 0;

		while(altitude < spaceY)
		{
			if(ticks >= 1000)
			{
				System.out.println("Takeoff simulation took too long!");
				return 0;
			}

			velocity -= 0.025;
			double engineThrust = 0.0D;
			double hover = (rocketMass*9.80665)/(thrust*1000);

			if(altitude < height)
				engineThrust = hover*1.1;
			else
				engineThrust = hover*(altitude/height);

			engineThrust = Math.max(0, Math.min(engineThrust, 1));

			fuelUsed += (int) (fuelFlow * engineThrust) * stage.getFuelTypeAmount() * getEngineAmount();
			massAccounted -= (int) (fuelFlow*engineThrust) * stage.getFuelTypeAmount() * getEngineAmount();
			rocketMass -= (int) (fuelFlow*engineThrust) * stage.getFuelTypeAmount() * getEngineAmount();

			double twr = (thrust*1000)/(rocketMass*9.80665);
			acceleration = 0.025*twr*engineThrust;

			velocity = Math.min(RocketEntity.MAX_SPEED_UP_BT, acceleration+velocity);
			velocity = Math.max(velocity, 0);
			altitude += velocity;
			altitude = Math.max(0, altitude);

			ticks++;
		}

		double Isp = getAverageIsp();
		double massRatio = mass/(massAccounted);
		double deltaV = 9.8*Isp*Math.log(massRatio);

		fuel.set(fuelUsed);

		System.out.println("Simulated time - " + ticks);
		System.out.println("Simulated takeoff fuel - " + fuelUsed);
		return deltaV;
	}
}
