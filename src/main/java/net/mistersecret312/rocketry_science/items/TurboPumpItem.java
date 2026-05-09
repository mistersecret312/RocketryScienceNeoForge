package net.mistersecret312.rocketry_science.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.mistersecret312.rocketry_science.init.DataComponentInit;
import net.mistersecret312.rocketry_science.util.RocketMaterial;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TurboPumpItem extends Item
{
    public TurboPumpItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag)
    {
        RocketMaterial material = getMaterial(stack);
        if(material == null)
            return;

        tooltipComponents.add(Component.translatable("desc.rocketry_science.rocket_material."+material.getSerializedName()).withStyle(ChatFormatting.DARK_PURPLE));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static ItemStack create(TurboPumpItem item, RocketMaterial material)
    {
        ItemStack stack = new ItemStack(item);
        item.setMaterial(stack, material);

        return stack;
    }

    @Nullable
    public RocketMaterial getMaterial(ItemStack stack)
    {
        return stack.getOrDefault(DataComponentInit.ROCKET_MATERIAL, RocketMaterial.STAINLESS_STEEL);
    }

    public void setMaterial(ItemStack stack, RocketMaterial material)
    {
        stack.set(DataComponentInit.ROCKET_MATERIAL, material);
    }
}
