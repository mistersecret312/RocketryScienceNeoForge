package net.mistersecret312.rocketry_science.block_entities.rocket_engine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class RocketEngineBlockEntity extends BlockEntity
{
    public static final int COMBUSTION_CHAMBER_CAPACITY = 1000;
    public boolean isBuilt = false;

    public double mass;
    public double thrust;
    public double efficiency;

    public int animTick = 0;
    public int soundTick = 0;
    public int frame = 0;

    public double runtime = 0;

    public RocketEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
    }

    public boolean hasPropellantMixture()
    {
        return true;
    }

    @Nullable
    public BlockState getNozzle()
    {
        return null;
    }

    @Nullable
    public BlockPos getNozzlePos()
    {
        return null;
    }

    public void setMass(double mass)
    {
        this.mass = mass;
    }

    public void setEfficiency(double efficiency)
    {
        this.efficiency = efficiency;
    }

    public void setThrust(double thrust)
    {
        this.thrust = thrust;
    }

    public double getMass()
    {
        return mass;
    }

    public double getEfficiency()
    {
        return efficiency;
    }

    public double getThrust()
    {
        return thrust;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registryAccess)
    {
        super.saveAdditional(tag, registryAccess);
        tag.putBoolean("is_built", this.isBuilt);
        tag.putDouble("mass", this.mass);
        tag.putDouble("thrust", this.thrust);
        tag.putDouble("efficiency", this.efficiency);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registryAccess)
    {
        super.loadAdditional(tag, registryAccess);
        this.isBuilt = tag.getBoolean("is_built");
        this.mass = tag.getDouble("mass");
        this.thrust = tag.getDouble("thrust");
        this.efficiency = tag.getDouble("efficiency");
    }
}
