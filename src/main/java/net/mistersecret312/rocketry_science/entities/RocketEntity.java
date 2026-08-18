package net.mistersecret312.rocketry_science.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.rocketry_science.init.EntityDataSerializersInit;
import net.mistersecret312.rocketry_science.init.EntityInit;
import net.mistersecret312.rocketry_science.util.RocketUtil;
import net.mistersecret312.rocketry_science.vessel.Rocket;
import net.mistersecret312.rocketry_science.vessel.VesselState;
import net.mistersecret312.rocketry_science.vessel.Stage;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

public class RocketEntity extends Entity
{
	public static final double MAX_SPEED_UP_BT = 8.0;
	public static final double MAX_SPEED_DOWN_BT = -4.0;

	private static final String ROCKET_DATA = "vessel_data";
	private static final EntityDataAccessor<Rocket> ROCKET =
			SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializersInit.ROCKET.get());

	public boolean mustRemove = false;
	public int removeTick = 0;

	public RocketEntity(EntityType<RocketEntity> type, Level level)
	{
		super(type, level);
	}

	public RocketEntity(Level level)
	{
		super(EntityInit.ROCKET.get(), level);
	}

	@Override
	public void tick()
	{
		super.tick();
		this.move(MoverType.SELF, this.getDeltaMovement());

		Rocket rocket = getRocket();
		if(rocket.rocket == null)
			rocket.rocket = this;

		rocket.tick(level());

		if(level().getGameTime() % 20 == 0)
			this.setBoundingBox(makeBoundingBox());

		if(level().isClientSide())
			return;

		if (!this.isNoGravity())
			this.addDeltaMovement(new Vec3(0.0D, -0.025D, 0.0D));
		if(mustRemove)
		{
			removeTick++;
			if(removeTick == 3)
				discard();
		}

		this.setDeltaMovement(0, Math.max(Math.min(this.getDeltaMovement().y, MAX_SPEED_UP_BT), MAX_SPEED_DOWN_BT), 0);

		this.setDeltaMovement(getDeltaMovement().multiply(0.8, 1, 0.8));

	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag)
	{
		Rocket rocket = new Rocket(this, new LinkedHashSet<>());
		rocket.load(tag.getCompound(ROCKET_DATA), level().getServer());
		this.entityData.set(ROCKET, rocket);

		this.mustRemove = tag.getBoolean("must_remove");
		this.removeTick = tag.getInt("remove_tick");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag)
	{
		tag.put(ROCKET_DATA, this.entityData.get(ROCKET).save());
		tag.putInt("remove_tick", removeTick);
		tag.putBoolean("must_remove", mustRemove);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder)
	{
		builder.define(ROCKET, new Rocket(this, new LinkedHashSet<>()));
	}

	@Override
	public AABB makeBoundingBox()
	{
		AABB aabb = null;
		for (Stage stage : this.getRocket().getStages())
		{
			for (Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
			{
				if(aabb == null)
					aabb = entry.getValue().affectBoundingBox(new AABB(this.position(),
							this.position()), this);
				else aabb = entry.getValue().affectBoundingBox(aabb, this);
			}
		}

		if(aabb == null)
			return new AABB(this.getOnPos().above());

		this.setBoundingBox(aabb);
		return aabb;
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand)
	{
		if(player.level().isClientSide())
			return InteractionResult.SUCCESS;

		if(player.getItemInHand(hand).is(Items.STICK))
		{
			getRocket().setState(VesselState.TAKEOFF);
			getRocket().canLand = true;
			return InteractionResult.SUCCESS;
		}

		BlockHitResult hitResult = getTargetedBlockHit(player);

		if(hitResult != null)
		{
			ItemStack stack = player.getItemInHand(hand);
			if(!stack.isEmpty() && stack.getItem() instanceof BlockItem)
			{
				UseOnContext context = new UseOnContext(player, hand, hitResult);
				InteractionResult placeResult = stack.useOn(context);

				if(placeResult.consumesAction())
					return placeResult;
			}
		}

		if(player.isShiftKeyDown())
		{
			for(Stage stage : this.getRocket().getStages())
			{
				for(Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
				{
					entry.getValue().placeInLevel(player.level(), entry.getKey().offset(this.getOnPos().above()));
				}
			}
			this.markForRemoval();
		}

		return InteractionResult.PASS;
	}

	public BlockHitResult getTargetedBlockHit(Player player)
	{
		Vec3 cameraPos = player.getEyePosition();
		double reach = player.blockInteractionRange();
		Vec3 viewVector = player.getViewVector(1.0F);
		Vec3 endPos = cameraPos.add(viewVector.scale(reach));

		Vec3 rocketPos = this.position();
		Rocket rocket = this.getRocket();

		BlockHitResult closestHit = null;
		double minDistance = reach * reach;

		for (Stage stage : rocket.getStages())
		{
			for (Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
			{
				BlockPos relativePos = entry.getKey();

				AABB blockAABB = entry.getValue().getIndividualBoundingBox().move(
						rocketPos.x + relativePos.getX(),
						rocketPos.y + relativePos.getY(),
						rocketPos.z + relativePos.getZ()
				);

				Optional<Vec3> hitOpt = blockAABB.clip(cameraPos, endPos);
				if (hitOpt.isPresent())
				{
					Vec3 hitVec = hitOpt.get();
					double distSq = cameraPos.distanceToSqr(hitVec);
					if (distSq < minDistance)
					{
						minDistance = distSq;

						Direction hitDir = Direction.UP;
						double bestDist = Double.MAX_VALUE;

						double[] distances = {
								Math.abs(hitVec.y - blockAABB.minY), // DOWN
								Math.abs(hitVec.y - blockAABB.maxY), // UP
								Math.abs(hitVec.z - blockAABB.minZ), // NORTH
								Math.abs(hitVec.z - blockAABB.maxZ), // SOUTH
								Math.abs(hitVec.x - blockAABB.minX), // WEST
								Math.abs(hitVec.x - blockAABB.maxX)  // EAST
						};

						Direction[] directions = {
								Direction.DOWN, Direction.UP, Direction.NORTH,
								Direction.SOUTH, Direction.WEST, Direction.EAST
						};

						for (int i = 0; i < 6; i++)
							if (distances[i] < bestDist)
							{
								bestDist = distances[i];
								hitDir = directions[i];
							}

						BlockPos targetWorldPos = BlockPos.containing(
								hitVec.x + hitDir.getStepX() * 0.05,
								hitVec.y + hitDir.getStepY() * 0.05,
								hitVec.z + hitDir.getStepZ() * 0.05
						);

						closestHit = new BlockHitResult(hitVec, hitDir, targetWorldPos, false);
					}
				}
			}
		}
		return closestHit;
	}

	public BlockData getTargetedBlockData(Player player)
	{
		Vec3 cameraPos = player.getEyePosition();
		double reach = player.blockInteractionRange();
		Vec3 viewVector = player.getViewVector(1.0F);
		Vec3 endPos = cameraPos.add(viewVector.scale(reach));

		Vec3 rocketPos = this.position();
		Rocket rocket = this.getRocket();

		BlockData closestData = null;
		double minDistance = reach * reach;

		for (Stage stage : rocket.getStages())
		{
			for (Map.Entry<BlockPos, BlockData> entry : stage.blocks.entrySet())
			{
				BlockPos relativePos = entry.getKey();

				AABB blockAABB = entry.getValue().getIndividualBoundingBox().move(
						rocketPos.x + relativePos.getX(),
						rocketPos.y + relativePos.getY(),
						rocketPos.z + relativePos.getZ()
				);

				Optional<Vec3> hitOpt = blockAABB.clip(cameraPos, endPos);
				if (hitOpt.isPresent())
				{
					Vec3 hitVec = hitOpt.get();
					double distSq = cameraPos.distanceToSqr(hitVec);
					if (distSq < minDistance)
					{
						minDistance = distSq;
						double bestDist = Double.MAX_VALUE;

						double[] distances = {
								Math.abs(hitVec.y - blockAABB.minY), // DOWN
								Math.abs(hitVec.y - blockAABB.maxY), // UP
								Math.abs(hitVec.z - blockAABB.minZ), // NORTH
								Math.abs(hitVec.z - blockAABB.maxZ), // SOUTH
								Math.abs(hitVec.x - blockAABB.minX), // WEST
								Math.abs(hitVec.x - blockAABB.maxX)  // EAST
						};

						for (int i = 0; i < 6; i++)
							if (distances[i] < bestDist)
								bestDist = distances[i];

						closestData = entry.getValue();
					}
				}
			}
		}
		return closestData;
	}


	@Override
	public @Nullable ItemStack getPickResult()
	{
		if(level().isClientSide())
			return RocketUtil.getPickedBlock(this);

		return null;
	}

	@Override
	public Component getName()
	{
		if(level().isClientSide())
			return RocketUtil.getPickedBlock(this).getHoverName();

		return super.getName();
	}

	public void markForRemoval()
	{
		this.mustRemove = true;
		this.removeTick = 0;
	}

	public Rocket getRocket()
	{
		return this.entityData.get(ROCKET);
	}

	public void setRocket(Rocket rocket)
	{
		rocket.rocket = this;
		this.entityData.set(ROCKET, rocket);
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return true;
	}

	@Override
	public boolean isPickable()
	{
		return true;
	}

	@Override
	public boolean isPushable()
	{
		return true;
	}

	@Override
	public boolean isNoGravity()
	{
		return false;
	}
}
