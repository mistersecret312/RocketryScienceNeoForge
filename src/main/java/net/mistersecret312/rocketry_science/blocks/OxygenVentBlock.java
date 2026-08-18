package net.mistersecret312.rocketry_science.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.mistersecret312.rocketry_science.data.room.Room;
import net.mistersecret312.rocketry_science.data.room.RoomManager;
import net.mistersecret312.rocketry_science.init.AttachmentTypeInit;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;

public class OxygenVentBlock extends BaseEntityBlock
{
	public static final MapCodec<LaunchControllerBlock> CODEC = simpleCodec(LaunchControllerBlock::new);

	public OxygenVentBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.MODEL;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
											  Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if(stack.getItem().equals(Items.STICK) && level instanceof ServerLevel serverLevel)
		{
			RoomManager manager = serverLevel.getData(AttachmentTypeInit.ROOM_MANAGER);
			if(manager.getRoomAt(pos).isEmpty())
			{
				manager.level = serverLevel;
				manager.startScan(pos, 0.01f, pos);
			}
			else
			{
				Room room = manager.getRoomAt(pos).get();
				room.getOxygenNodes().add(pos.asLong());
			}
		}

		return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec()
	{
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return BlockEntityInit.OXYGEN_VENT.get().create(pos, state);
	}
}
