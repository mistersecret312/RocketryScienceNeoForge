package net.mistersecret312.rocketry_science.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.blocks.SeparatorBlock;
import net.mistersecret312.rocketry_science.data.orbits.CelestialOrbit;
import net.mistersecret312.rocketry_science.data.orbits.Orbit;
import net.mistersecret312.rocketry_science.datapack.CelestialBody;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.init.BlockDataInit;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import net.mistersecret312.rocketry_science.util.OrbitUtil;
import net.mistersecret312.rocketry_science.util.OrbitalMath;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
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
	public RocketAssemblerBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.ROCKET_ASSEMBLER.get(), pos, blockState);
	}

	public void assembleRocket(RocketPadBlockEntity pad, Player player)
	{
		if(pad.getLevel() == null || pad.getLevel().isClientSide())
			return;

		if(!pad.isComplete())
		{
			player.displayClientMessage(Component.literal("ERROR: Rocket Pad is not fully constructed"), true);
			return;
		}

		AABB box = pad.getOnPadBox();
		RocketEntity rocketEntity = new RocketEntity(pad.getLevel());
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

					for(BiFunction<Stage, BlockPos, BlockData> func : BlockDataInit.DATA_FACTORY)
					{
						BlockData attemptedData = func.apply(currentStage, pos);
						if(attemptedData == BlockData.VOID) break;

						data = attemptedData;
						if(attemptedData != null) break;
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
		{
			player.displayClientMessage(Component.literal("ERROR: Rocket Pad is empty"), true);
			return;
		}

		rocket.stages.add(currentStage);
		rocketEntity.setRocket(rocket);
		rocketEntity.setPos(firstFound.getCenter().add(0, -0.5, 0));
		if(!rocket.stages.isEmpty())
		{
			pad.getLevel().addFreshEntity(rocketEntity);
			player.displayClientMessage(Component.literal("SUCCESS! Rocket assembled!"), true);
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
		} else player.displayClientMessage(Component.literal("ERROR: Rocket Pad is empty! Report to developer!"), true);

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

		super.saveAdditional(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.loadAdditional(tag, registries);

		this.setPadUUID(tag.getUUID("pad_id"));
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
