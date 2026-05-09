package net.mistersecret312.rocketry_science.menus;

import net.minecraft.world.item.ItemStack;
import net.mistersecret312.rocketry_science.items.CombustionChamberItem;
import net.mistersecret312.rocketry_science.items.TurboPumpItem;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class RocketComponentSlotHandler extends SlotItemHandler
{
    public ComponentType type;

    public RocketComponentSlotHandler(IItemHandler itemHandler, ComponentType type, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
        this.type = type;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack)
    {
        if(stack.getItem() instanceof TurboPumpItem && this.type == ComponentType.TURBOPUMP)
            return true;
        return stack.getItem() instanceof CombustionChamberItem && this.type == ComponentType.COMBUSTION_CHAMBER;
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }

    public enum ComponentType
    {
        COMBUSTION_CHAMBER,
        TURBOPUMP;
    }
}
