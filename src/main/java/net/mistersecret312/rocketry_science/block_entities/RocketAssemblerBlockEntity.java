package net.mistersecret312.rocketry_science.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.blocks.SeparatorBlock;
import net.mistersecret312.rocketry_science.data.orbits.CelestialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.Orbit;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPadData;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.entities.RocketAssemblerGantryEntity;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.init.BlockDataInit;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import net.mistersecret312.rocketry_science.network.packets.ClientBoundAssemblerUpdatePacket;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.mistersecret312.rocketry_science.util.OrbitalMath;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.neoforged.neoforge.network.PacketDistributor;
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
	private UUID trackedGantry = null;

	public boolean started = false;
	public double progress = 0d;
	public double maxProgress = 200d;

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
			rocket.landingSimulation();
			return "";
		}
		return "ERROR: Rocket Pad is empty! Report to developer!";
	}

	public void startAssembly()
	{
		this.triggerAnim("spin", "spin");
		this.started = true;

		if(level instanceof ServerLevel serverLevel)
			PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(this.getBlockPos()),
					new ClientBoundAssemblerUpdatePacket(this.getBlockPos(), progress, maxProgress, started));

		RocketPadData data = RocketPadData.get(level.getServer());
		RocketPad rocketPad = data.rocketPads.get(this.getPadUUID());
		Level padLevel = level.getServer().getLevel(rocketPad.getDimension());
		if(padLevel == null)
			return;

		RocketPadBlockEntity padBE = (RocketPadBlockEntity) padLevel.getBlockEntity(rocketPad.getPos());
		if(padBE != null)
		{
			AABB box = padBE.getOnPadBox().expandTowards(1, 1, 1);

			Vec3 position = box.getBottomCenter();
			RocketAssemblerGantryEntity gantry = new RocketAssemblerGantryEntity(padLevel);

			gantry.setHeight((float) box.getYsize());
			gantry.setXWidth((float) box.getXsize());
			gantry.setZWidth((float) box.getZsize());
			gantry.setProgress(0f);

			gantry.setPos(position);
			padLevel.addFreshEntity(gantry);

			this.trackedGantry = gantry.getUUID();
		}

		setChanged();
		if(this.level != null)
			this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
	}

	public void tickAssembly()
	{
		this.progress++;
		if(level == null || level.isClientSide() || level.getServer() == null)
			return;

		if(level instanceof ServerLevel serverLevel)
			PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(this.getBlockPos()),
					new ClientBoundAssemblerUpdatePacket(this.getBlockPos(), progress, maxProgress, started));

		RocketPadData data = RocketPadData.get(level.getServer());
		RocketPad rocketPad = data.rocketPads.get(this.getPadUUID());
		if(rocketPad == null)
			return;
		Level padLevel = level.getServer().getLevel(rocketPad.getDimension());
		if(padLevel == null)
			return;
		if(padLevel instanceof ServerLevel serverLevel)
		{
			Entity entity = serverLevel.getEntity(trackedGantry);
			if(entity instanceof RocketAssemblerGantryEntity gantry)
				gantry.setProgress((float) (progress/maxProgress));
		}

		if(progress >= maxProgress)
			endAssembly();
	}

	public void endAssembly()
	{
		started = false;
		progress = 0d;
		setChanged();
		if(this.level != null)
			this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);


		this.stopTriggeredAnim("spin", "spin");
		if(level == null || level.isClientSide() || level.getServer() == null)
			return;

		RocketPadData data = RocketPadData.get(level.getServer());
		RocketPad rocketPad = data.rocketPads.get(this.getPadUUID());
		if(rocketPad == null)
			return;
		Level padLevel = level.getServer().getLevel(rocketPad.getDimension());
		if(padLevel == null)
			return;
		if(padLevel instanceof ServerLevel serverLevel)
		{
			Entity entity = serverLevel.getEntity(trackedGantry);
			if(entity != null)
				entity.discard();
		}
		this.trackedGantry = null;
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

	public static void tick(Level level, BlockPos pos, BlockState blockState, RocketAssemblerBlockEntity blockEntity)
	{
		if(blockEntity.started)
			blockEntity.tickAssembly();
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
		tag.putDouble("max_progress", maxProgress);
		if(trackedGantry != null)
			tag.putUUID("tracked_gantry", trackedGantry);

		super.saveAdditional(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.loadAdditional(tag, registries);

		this.setPadUUID(tag.getUUID("pad_id"));
		this.started = tag.getBoolean("progress");
		this.progress = tag.getDouble("progress");
		this.maxProgress = tag.getDouble("max_progress");
		if(tag.contains("tracked_gantry"))
			this.trackedGantry = tag.getUUID("tracked_gantry");
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
