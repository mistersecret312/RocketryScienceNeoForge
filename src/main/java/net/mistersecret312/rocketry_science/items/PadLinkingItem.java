package net.mistersecret312.rocketry_science.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.block_entities.IRocketPadConnective;
import net.mistersecret312.rocketry_science.block_entities.multiblock.RocketPadBlockEntity;
import net.mistersecret312.rocketry_science.init.DataComponentInit;

import java.util.List;
import java.util.UUID;

public class PadLinkingItem extends Item
{

    public PadLinkingItem(Properties pProperties)
    {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if(level.isClientSide() || player == null)
            return InteractionResult.PASS;

        if(level.getBlockEntity(pos) instanceof RocketPadBlockEntity pad)
        {
            this.setUUID(context.getItemInHand(), pad.getUUID());
            player.displayClientMessage(Component.literal("UUID stored - " + this.getUUID(context.getItemInHand())), true);

        }
        if(level.getBlockEntity(pos) instanceof IRocketPadConnective connective)
        {
            connective.setPadUUID(this.getUUID(context.getItemInHand()));
            player.displayClientMessage(Component.literal("UUID set to - " + this.getUUID(context.getItemInHand())), true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        String id = this.getUUID(stack)  == null ? "" : this.getUUID(stack).toString();
        if(id != null)
            tooltipComponents.add(Component.literal(id));
    }

    public UUID getUUID(ItemStack stack)
    {
        return stack.get(DataComponentInit.UUID);
    }

    public void setUUID(ItemStack stack, UUID uuid)
    {
        if(uuid == null)
            stack.remove(DataComponentInit.UUID);
        else stack.set(DataComponentInit.UUID, uuid);
    }
}
