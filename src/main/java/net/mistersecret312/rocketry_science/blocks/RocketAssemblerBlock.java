package net.mistersecret312.rocketry_science.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.rocketry_science.block_entities.RocketAssemblerBlockEntity;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPad;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPadData;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;

public class RocketAssemblerBlock extends BaseEntityBlock
{
	public static final MapCodec<RocketAssemblerBlock> CODEC = simpleCodec(RocketAssemblerBlock::new);

	public RocketAssemblerBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
											  Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if(stack.getItem().equals(Items.STICK) && !level.isClientSide() && level.getBlockEntity(pos) instanceof RocketAssemblerBlockEntity constructor)
		{
			if(constructor.getPadUUID() == null || level.getServer() == null)
				return ItemInteractionResult.FAIL;

			RocketPadData data = RocketPadData.get(level.getServer());
			RocketPad rocketPad = data.rocketPads.get(constructor.getPadUUID());
			if(rocketPad == null)
				return ItemInteractionResult.FAIL;

			BlockPos padPos = rocketPad.getPos();
			Level padLevel = level.getServer().getLevel(rocketPad.getDimension());
			if(padLevel == null)
				return ItemInteractionResult.FAIL;

			RocketPadBlockEntity pad = (RocketPadBlockEntity) padLevel.getBlockEntity(padPos);
			if(pad != null)
				constructor.assembleRocket(pad, player);
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
											   BlockHitResult hitResult)
	{
		if(level.getBlockEntity(pos) instanceof RocketAssemblerBlockEntity assembler)
		{
			if(player.isShiftKeyDown())
				assembler.stopTriggeredAnim("spin", "spin");
			else assembler.triggerAnim("spin", "spin");
		}

		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
	{
		return BlockEntityInit.ROCKET_ASSEMBLER.get().create(blockPos, blockState);
	}
}
