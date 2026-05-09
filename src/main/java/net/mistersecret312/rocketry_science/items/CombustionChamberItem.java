package net.mistersecret312.rocketry_science.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.mistersecret312.rocketry_science.init.DataComponentInit;
import net.mistersecret312.rocketry_science.util.RocketFuel;
import net.mistersecret312.rocketry_science.util.RocketMaterial;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CombustionChamberItem extends Item
{

    public CombustionChamberItem(Properties properties)
    {
        super(properties);
    }

    public static ItemStack create(CombustionChamberItem item, RocketFuel fuel, RocketMaterial material)
    {
        ItemStack stack = new ItemStack(item);
        item.setFuelType(stack, fuel);
        item.setMaterial(stack, material);

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag)
    {
        RocketFuel fuelType = getFuelType(stack);
        RocketMaterial material = getMaterial(stack);
        if(fuelType == null || material == null)
            return;

        tooltipComponents.add(Component.translatable("desc.rocketry_science.rocket_fuel."+fuelType.getSerializedName()).withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("desc.rocketry_science.rocket_material."+material.getSerializedName()).withStyle(ChatFormatting.DARK_PURPLE));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Nullable
    public RocketFuel getFuelType(ItemStack stack)
    {
        return stack.getOrDefault(DataComponentInit.ROCKET_FUEL, RocketFuel.HYDROLOX);
    }

    public void setFuelType(ItemStack stack, RocketFuel fuel)
    {
        stack.set(DataComponentInit.ROCKET_FUEL, fuel);
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
