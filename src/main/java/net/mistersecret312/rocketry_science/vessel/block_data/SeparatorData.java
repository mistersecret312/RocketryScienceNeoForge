package net.mistersecret312.rocketry_science.vessel.block_data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.rocketry_science.block_entities.SeparatorBlockEntity;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.init.BlockDataInit;
import net.mistersecret312.rocketry_science.init.BlockInit;
import net.mistersecret312.rocketry_science.vessel.Stage;

import java.util.Iterator;
import java.util.function.BiFunction;

public class SeparatorData extends BlockData
{
    public boolean extended;
    public int width;

    public SeparatorData(Stage stage, int state, BlockPos pos, CompoundTag tag)
    {
        super(stage, state, pos, tag);
    }

    public SeparatorData()
    {

    }

    @Override
    public void tick(Level level)
    {
        Iterator<Stage> stage = this.getStage().getVessel().getStages().iterator();
        boolean foundSelf = false;
        Stage stage1 = null;
        while(stage.hasNext())
        {
            Stage stage0 = stage.next();
            if(foundSelf)
            {
                stage1 = stage0;
                break;
            }

            if(stage0.blocks.equals(this.getStage().blocks))
                foundSelf = true;
        }

        if(stage1 != null)
        {
            BlockData data = stage1.blocks.get(new BlockPos(this.pos.getX(), this.pos.getY()+2, this.pos.getZ()));
            if(data != null)
                extended = data.getBlockState().is(BlockInit.STEEL_ROCKET_ENGINE_STUB.get()) || data instanceof RocketEngineData;
            else extended = false;
        }
        else extended = false;

    }

    @Override
    public double getMass()
    {
        return super.getMass()*2;
    }

    @Override
    public BlockDataType<SeparatorData> getType()
    {
        return BlockDataInit.SEPARATOR.get();
    }

    @Override
    public void initializeData(Stage stage)
    {
        extended = false;
        width = this.extraData.getInt("Size");
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buffer)
    {
        super.toNetwork(buffer);
        buffer.writeBoolean(this.extended);
    }

    @Override
    public void fromNetwork(RegistryFriendlyByteBuf buffer, BlockPos pos, Stage stage)
    {
        super.fromNetwork(buffer, pos, stage);
        this.extended = buffer.readBoolean();
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
        maxY = Math.max(aabb.maxY, rocket.position().y+pos.getY()+1);

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BiFunction<Stage, BlockPos, BlockData> create()
    {
        return (stage, pos) ->
        {
            Level level = stage.getVessel().level();
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof SeparatorBlockEntity separator)
            {
                if(separator.isController())
                {
                    CompoundTag extraData = blockEntity.saveWithId(level.registryAccess());
                    if(!stage.palette.contains(state))
                        stage.palette.add(state);
                    for (int x = pos.getX(); x < pos.getX()+separator.getWidth(); x++)
                        for (int z = pos.getZ(); z < pos.getZ() + separator.getWidth() ; z++)
                        {
                            BlockPos blockPos = new BlockPos(x, pos.getY(), z);
                            level.removeBlock(blockPos, false);
                        }
                    return new SeparatorData(stage, stage.palette.indexOf(state), pos, extraData);
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
                super.placeInLevel(level, pos.offset(0, 0, 0));
                return;
            case 2:
                level.setBlock(pos.offset(1,0,0), BlockInit.SEPARATOR.get().defaultBlockState(), 2);
                level.setBlock(pos.offset(0,0,1), BlockInit.SEPARATOR.get().defaultBlockState(), 2);
                level.setBlock(pos.offset(1,0,1), BlockInit.SEPARATOR.get().defaultBlockState(), 2);
                super.placeInLevel(level, pos.offset(0, 0, 0));
                return;
            case 3:

                level.setBlock(pos.offset(1,0,0), BlockInit.SEPARATOR.get().defaultBlockState(), 2);
                level.setBlock(pos.offset(0,0,1), BlockInit.SEPARATOR.get().defaultBlockState(), 2);
                level.setBlock(pos.offset(1,0,1), BlockInit.SEPARATOR.get().defaultBlockState(), 2);

                level.setBlock(pos.offset(2,0,0), BlockInit.SEPARATOR.get().defaultBlockState(), 2);
                level.setBlock(pos.offset(0,0,2), BlockInit.SEPARATOR.get().defaultBlockState(), 2);
                level.setBlock(pos.offset(2,0,2), BlockInit.SEPARATOR.get().defaultBlockState(), 2);

                level.setBlock(pos.offset(1,0,2), BlockInit.SEPARATOR.get().defaultBlockState(), 2);
                level.setBlock(pos.offset(2,0,1), BlockInit.SEPARATOR.get().defaultBlockState(), 2);
                super.placeInLevel(level, pos.offset(0, 0, 0));
        }
    }

    @Override
    public boolean doesTick(Level level)
    {
        return true;
    }
}
