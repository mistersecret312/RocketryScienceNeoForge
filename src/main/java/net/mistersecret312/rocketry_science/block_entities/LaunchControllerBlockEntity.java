package net.mistersecret312.rocketry_science.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class LaunchControllerBlockEntity extends BlockEntity implements GeoBlockEntity, IRocketPadConnective
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private UUID uuid = UUID.randomUUID();

	public LaunchControllerBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.LAUNCH_CONTROLLER.get(), pos, blockState);
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
		tag.putUUID("pad_id", this.getUUID());

		super.saveAdditional(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.loadAdditional(tag, registries);

		this.setUUID(tag.getUUID("pad_id"));
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{

	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}

	public UUID getUUID()
	{
		return uuid;
	}

	public void setUUID(UUID uuid)
	{
		this.uuid = uuid;
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
