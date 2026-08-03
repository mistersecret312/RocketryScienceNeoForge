package net.mistersecret312.rocketry_science.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.blocks.SeparatorBlock;
import net.mistersecret312.rocketry_science.data.orbits.CelestialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.Orbit;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPadData;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.init.BlockDataInit;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.mistersecret312.rocketry_science.util.OrbitalMath;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.function.BiFunction;

public class RocketAssemblerBlockEntity extends BlockEntity implements GeoBlockEntity, IRocketPadConnective
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	protected static final RawAnimation SPIN = RawAnimation.begin().thenPlay("spin");

	private UUID uuid = UUID.randomUUID();

	public boolean started = false;
	public double progress = 0d;

	public RocketAssemblerBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.ROCKET_ASSEMBLER.get(), pos, blockState);
	}

	public String assembleRocket(RocketPadBlockEntity pad, RocketEntity rocketEntity, boolean simulate)
	{
		if(pad.getLevel() == null)
			return "ERROR : Rocket Pad is not real!";

		if(!pad.isComplete())
			return "ERROR: Rocket Pad is not fully constructed";

		AABB box = pad.getOnPadBox();
		Rocket rocket = new Rocket(rocketEntity, new LinkedHashSet<>());
		Stage currentStage = new Stage(rocket);
		BlockPos firstFound = null;
		for(double y = box.minY; y <= box.maxY; y++)
		{
			boolean foundSeparator = false;
			for(double x = box.minX; x <= box.maxX; x++)
				for(double z = box.minZ; z <= box.maxZ; z++)
				{
					BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
					BlockState state = pad.getLevel().getBlockState(pos);
					if(state.isAir() || state.is(Blocks.BEDROCK)) continue;

					if(firstFound == null) firstFound = pos;

					BlockData data = BlockData.VOID;

					for(TriFunction<Stage, BlockPos, Boolean, BlockData> func : BlockDataInit.DATA_FACTORY)
					{
						BlockData attemptedData = func.apply(currentStage, pos, simulate);
						if(attemptedData == BlockData.VOID)
							break;

						data = attemptedData;
						if(attemptedData != null)
							break;
					}

					if(data != BlockData.VOID && data != null)
					{
						if(!currentStage.palette.contains(state)) currentStage.palette.add(state);

						data.pos = pos.subtract(firstFound);
						currentStage.blocks.put(data.pos, data);
					}

					if(state.getBlock() instanceof SeparatorBlock) foundSeparator = true;
				}
			if(foundSeparator)
			{
				rocket.stages.add(currentStage);
				currentStage = new Stage(rocket);
			}
		}

		if(firstFound == null)
			return "ERROR: Rocket Pad is empty";

		rocket.stages.add(currentStage);
		rocketEntity.setRocket(rocket);
		rocketEntity.setPos(firstFound.getCenter().add(0, -0.5, 0));
		if(!rocket.stages.isEmpty())
		{
			int stageI = 0;
			double deltaV = 0;
			for(Stage stage : rocket.stages)
			{
				double stageDelta = stage.calculateDeltaV();
				System.out.println("Stage[" + stageI + "] = " + stageDelta);
				deltaV += stageDelta;
				stageI++;
			}

			System.out.println("Rocket Total DeltaV - " + deltaV);

			System.out.println("Rocket TWR - " + rocket.getMaxTWR());
			rocket.landingSimulation();

			double leoHeight = 300*1000;
			double deltaVToOrbit = OrbitalMath.getLaunchDeltaV(OrbitUtil.getCelestialBody(pad.getLevel()), leoHeight);
			System.out.println("Target Orbit DeltaV Requirement - " + deltaVToOrbit);

			CelestialBody body = OrbitUtil.getCelestialBody(pad.getLevel());
			CelestialOrbit orbit = body.getOrbit();
			double radius = body.getRadius();
			return "";
		}
		return "ERROR: Rocket Pad is empty! Report to developer!";
	}

	public void startAssembly()
	{
		this.triggerAnim("spin", "spin");
		this.started = true;
	}

	public void tickAssembly()
	{
		this.progress++;
		if(progress >= 200)
			endAssembly();
	}

	public void endAssembly()
	{
		started = false;
		progress = 0d;

		this.stopTriggeredAnim("spin", "spin");
		if(level == null || level.isClientSide() || level.getServer() == null)
			return;

		RocketPadData data = RocketPadData.get(level.getServer());
		RocketPad rocketPad = data.rocketPads.get(this.getPadUUID());
		Level padLevel = level.getServer().getLevel(rocketPad.getDimension());
		if(padLevel == null)
			return;

		RocketPadBlockEntity padBE = (RocketPadBlockEntity) padLevel.getBlockEntity(rocketPad.getPos());
		if(padBE != null)
		{
			RocketEntity rocketEntity = new RocketEntity(padLevel);
			String msg = assembleRocket(padBE, rocketEntity, false);
			if(msg.isEmpty())
				padLevel.addFreshEntity(rocketEntity);
			else
			{
				AABB box = new AABB(rocketPad.getPos()).inflate(5);
				for(Entity entity : level.getEntities((Entity) null, box, entity -> entity instanceof Player))
				{
					if(entity instanceof Player player)
						player.displayClientMessage(Component.literal(msg), true);
				}
			}
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries)
	{
		return this.saveWithoutMetadata(registries);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		tag.putUUID("pad_id", this.getPadUUID());
		tag.putBoolean("started", started);
		tag.putDouble("progress", progress);

		super.saveAdditional(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.loadAdditional(tag, registries);

		this.setPadUUID(tag.getUUID("pad_id"));
		this.started = tag.getBoolean("progress");
		this.progress = tag.getDouble("progress");
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		AnimationController<RocketAssemblerBlockEntity> controller =
				new AnimationController<>(this, "spin", 0, this::spinController);
		controller.triggerableAnim("spin", SPIN);
		controllers.add(controller);
	}

	private PlayState spinController(AnimationState<RocketAssemblerBlockEntity> state)
	{
		return PlayState.STOP;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}

	@Override
	public void setPadUUID(UUID uuid)
	{
		this.uuid = uuid;
	}

	@Override
	public UUID getPadUUID()
	{
		return this.uuid;
	}
}
