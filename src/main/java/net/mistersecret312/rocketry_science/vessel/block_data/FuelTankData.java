package net.mistersecret312.rocketry_science.vessel.block_data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.rocketry_science.block_entities.fuel_tank.FuelTankBlockEntity;
import net.mistersecret312.rocketry_science.init.BlockInit;
import net.mistersecret312.rocketry_science.util.RocketFuel;
import net.mistersecret312.rocketry_science.block_entities.fuel_tank.RocketFuelTank;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.init.BlockDataInit;
import net.mistersecret312.rocketry_science.vessel.Stage;

import java.util.function.BiFunction;

public class FuelTankData extends BlockData
{
    public int height;
    public int width;

    public RocketFuel fuel;
    public int capacity;
    public RocketFuelTank tank;

    public FuelTankData(Stage stage, int state, BlockPos pos, CompoundTag tag)
    {
        super(stage, state, pos, tag);
    }

    public FuelTankData()
    {
    }

    @Override
    public void tick(Level level)
    {

    }

    @Override
    public double getMass()
    {
        double fluidMass = 0;
        for(int tank = 0; tank < this.tank.getTanks(); tank++)
        {
            fluidMass += this.tank.getFluidInTank(tank).getAmount();
        }

        double hullMass = getDryMass();

        return fluidMass+hullMass;
    }

    @Override
    public double getDryMass()
    {
        return this.width*width*height*500;
    }

    @Override
    public BlockDataType<FuelTankData> getType()
    {
        return BlockDataInit.FUEL_TANK.get();
    }

    @Override
    public void initializeData(Stage stage)
    {
        height = this.extraData.getInt("Height");
        width = this.extraData.getInt("Size");
        fuel = RocketFuel.valueOf(this.extraData.getString("fuel_type").toUpperCase());
        capacity = width*width*height;
        tank = new RocketFuelTank(fuel.getPropellants(), capacity)
        {
            @Override
            protected void onContentsChanged()
            {
                extraData.put("TankContent", this.writeToNBT(new CompoundTag(), stage.vessel.level().registryAccess()));
            }
        };

        tank.readFromNBT(this.extraData.getCompound("TankContent"), stage.vessel.level().registryAccess());
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buffer)
    {
        super.toNetwork(buffer);
        buffer.writeInt(this.height);
        buffer.writeInt(this.width);
        RocketFuel.STREAM_CODEC.encode(buffer, this.fuel);
        RocketFuelTank.STREAM_CODEC.encode(buffer, this.tank);
    }

    @Override
    public void fromNetwork(RegistryFriendlyByteBuf buffer, BlockPos pos, Stage stage)
    {
        super.fromNetwork(buffer, pos, stage);
        this.height = buffer.readInt();
        this.width = buffer.readInt();
        this.fuel = RocketFuel.STREAM_CODEC.decode(buffer);
        this.tank = RocketFuelTank.STREAM_CODEC.decode(buffer);
    }

    public AABB affectBoundingBox(AABB aabb, RocketEntity rocket)
    {
        double minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        double minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        switch(width)
        {
            case 1:
                minX = Math.min(aabb.minX, rocket.position().x+pos.getX()-0.5);
                minZ = Math.min(aabb.minZ, rocket.position().z+pos.getZ()-0.5);
                maxX = Math.max(aabb.maxX, rocket.position().x+pos.getX()+0.5);
                maxZ = Math.max(aabb.maxZ, rocket.position().z+pos.getZ()+0.5);
                break;
            case 2:
                minX = Math.min(aabb.minX, rocket.position().x+pos.getX()-0.5);
                minZ = Math.min(aabb.minZ, rocket.position().z+pos.getZ()-0.5);
                maxX = Math.max(aabb.maxX, rocket.position().x+pos.getX()+1.5);
                maxZ = Math.max(aabb.maxZ, rocket.position().z+pos.getZ()+1.5);
                break;
            case 3:
                minX = Math.min(aabb.minX, rocket.position().x+pos.getX()-0.5);
                minZ = Math.min(aabb.minZ, rocket.position().z+pos.getZ()-0.5);
                maxX = Math.max(aabb.maxX, rocket.position().x+pos.getX()+2.5);
                maxZ = Math.max(aabb.maxZ, rocket.position().z+pos.getZ()+2.5);
                break;
        }

        minY = Math.min(aabb.minY, rocket.position().y+pos.getY());
        maxY = Math.max(aabb.maxY, rocket.position().y+pos.getY()+height);

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BiFunction<Stage, BlockPos, BlockData> create()
    {
        return (stage, pos) ->
        {
            Level level = stage.getVessel().level();
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof FuelTankBlockEntity fuelTank)
            {
                if(fuelTank.isController())
                {
                    CompoundTag extraData = blockEntity.saveWithId(stage.getVessel().level().registryAccess());
                    if(!stage.palette.contains(state))
                        stage.palette.add(state);
                    for (int x = pos.getX(); x < pos.getX()+fuelTank.getWidth(); x++)
                        for (int z = pos.getZ(); z < pos.getZ() + fuelTank.getWidth() ; z++)
                            for (int y = pos.getY(); y < pos.getY() + fuelTank.getHeight(); y++)
                            {
                                level.removeBlock(new BlockPos(x, y, z), false);
                            }
                    return new FuelTankData(stage, stage.palette.indexOf(state), pos, extraData);
                }
                else return BlockData.VOID;
            }
            return null;
        };
    }

    @Override
    public void placeInLevel(Level level, BlockPos pos)
    {
        switch(width)
        {
            case 1:
                for (int j = 0; j < height; j++)
                {
                    level.setBlock(pos.offset(0, j, 0), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                }
                super.placeInLevel(level, pos);
                return;
            case 2:
                for (int j = 0; j < height; j++)
                {
                    level.setBlock(pos.offset(0, j, 0), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(1, j, 0), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(0, j, 1), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(1, j, 1), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                }
                super.placeInLevel(level, pos);
                return;
            case 3:
                for (int j = 0; j < height; j++)
                {
                    level.setBlock(pos.offset(0, j, 0), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(1, j, 0), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(2, j, 0), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(0, j, 1), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(0, j, 2), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(1, j, 1), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(1, j, 2), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(2, j, 1), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                    level.setBlock(pos.offset(2, j, 2), BlockInit.FUEL_TANK.get().defaultBlockState(), 2);
                }
                super.placeInLevel(level, pos);

        }
    }
}
