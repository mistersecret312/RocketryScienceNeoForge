package net.mistersecret312.rocketry_science.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RocketAssemblerBlockEntity extends BlockEntity implements GeoBlockEntity
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	protected static final RawAnimation SPIN = RawAnimation.begin().thenPlay("spin");

	public RocketAssemblerBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.ROCKET_ASSEMBLER.get(), pos, blockState);
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
}
