package net.mistersecret312.rocketry_science.blocks.states;

import net.minecraft.util.StringRepresentable;

public enum VerticalConnection implements StringRepresentable
{
    NONE("none"),
    UP("up"),
    MIDDLE("middle"),
    BOTTOM("bottom");

    String name;
    VerticalConnection(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }
}