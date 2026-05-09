package net.mistersecret312.rocketry_science.data.rocket_pad;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class RocketPad
{
    public static final String DIMENSION = "dimension";
    public static final String POS = "pos";

    public BlockPos pos;
    public ResourceKey<Level> dimension;

    public RocketPad(BlockPos pos, ResourceKey<Level> dimension)
    {
        this.pos = pos;
        this.dimension = dimension;
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();

        tag.put(POS, NbtUtils.writeBlockPos(pos));
        tag.putString(DIMENSION, dimension.location().toString());

        return tag;
    }

    public static RocketPad load(CompoundTag tag)
    {
        BlockPos pos = NbtUtils.readBlockPos(tag, POS).get();
        ResourceKey<Level> dimension = stringToDimension(tag.getString(DIMENSION));

        return new RocketPad(pos, dimension);
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public ResourceKey<Level> getDimension()
    {
        return dimension;
    }

    public static ResourceKey<Level> stringToDimension(String dimensionString)
    {
        String[] split = dimensionString.split(":");

        if(split.length > 1)
            return ResourceKey.create(ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "dimension")),
                    ResourceLocation.fromNamespaceAndPath(split[0], split[1]));

        return null;
    }
}
