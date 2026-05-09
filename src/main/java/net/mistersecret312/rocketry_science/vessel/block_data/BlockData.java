package net.mistersecret312.rocketry_science.vessel.block_data;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.rocketry_science.entities.RocketEntity;
import net.mistersecret312.rocketry_science.init.BlockDataInit;
import net.mistersecret312.rocketry_science.vessel.Stage;

import java.util.function.BiFunction;

public class BlockData
{
    public static final BlockData VOID = new BlockData(null, -1, null, null);

    public BlockPos pos;
    public int state;
    public CompoundTag extraData;

    public Stage stage;

    public BlockData() {}

    public BlockData(Stage stage, int state, BlockPos pos, CompoundTag tag)
    {
        this.state = state;
        this.pos = pos;
        this.extraData = tag;

        this.stage = stage;

        this.initializeData(stage);
    }

    public void tick(Level level)
    {

    }

    public void orbitalTick(MinecraftServer server)
    {

    }

    public BlockDataType<?> getType()
    {
        return BlockDataInit.BASE.get();
    }

    public double getMass()
    {
        return 1000;
    }

    public double getDryMass()
    {
        return getMass();
    }

    public void initializeData(Stage stage)
    {

    }
    
    public AABB affectBoundingBox(AABB aabb, RocketEntity rocket)
    {
        double minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        double minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        minX = (Math.min(aabb.minX, rocket.position().x+pos.getX()-0.5));
        minY = (Math.min(aabb.minY, rocket.position().y+pos.getY()));
        minZ = (Math.min(aabb.minZ, rocket.position().z+pos.getZ()-0.5));
        maxX = (Math.max(aabb.maxX, rocket.position().x+pos.getX()+0.5));
        maxY = (Math.max(aabb.maxY, rocket.position().y+pos.getY()+1));
        maxZ = (Math.max(aabb.maxZ, rocket.position().z+pos.getZ()+0.5));

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BiFunction<Stage, BlockPos, BlockData> create()
    {
        return (stage, pos) ->
        {
            Level level = stage.vessel.level();
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            CompoundTag extraData = new CompoundTag();
            if(blockEntity != null)
                extraData = blockEntity.saveWithId(level.registryAccess());

            if(!stage.palette.contains(state))
                stage.palette.add(state);

            level.removeBlockEntity(pos);
            level.removeBlock(pos, false);

            return new BlockData(stage, stage.palette.indexOf(state), pos, extraData);
        };
    }

    public boolean doesTick(Level level)
    {
        return false;
    }

    public boolean ticksInSpace(MinecraftServer server)
    {
        return doesTick(server.overworld());
    }

    public void placeInLevel(Level level, BlockPos pos)
    {
        BlockState state = this.stage.palette.get(this.state);

        level.setBlock(pos, state, Block.UPDATE_ALL);
        if(extraData == null || extraData.isEmpty())
            return;

        BlockEntity entity = BlockEntity.loadStatic(pos, state, this.extraData, level.registryAccess());
        if(entity != null)
        {
            level.setBlockEntity(entity);
            entity.setChanged();
        }
    }

    public void toNetwork(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeInt(this.state);
        buffer.writeNbt(this.extraData);
    }

    public void fromNetwork(RegistryFriendlyByteBuf buffer, BlockPos pos, Stage stage)
    {
        this.stage = stage;
        this.pos = pos;
        this.state = buffer.readInt();
        this.extraData = buffer.readNbt();
    }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", BlockDataInit.REGISTRY.getKey(getType()).toString());
        tag.put("pos", NbtUtils.writeBlockPos(this.pos));
        tag.putInt("state", this.state);
        tag.put("extra_data", this.extraData);

        return tag;
    }

    public void load(CompoundTag tag, Stage stage)
    {
        this.pos = NbtUtils.readBlockPos(tag, "pos").get();
        this.state = tag.getInt("state");
        this.extraData = tag.getCompound("extra_data");

        this.stage = stage;
    }

    public BlockState getBlockState()
    {
        return this.stage.palette.get(this.state);
    }

    public Stage getStage()
    {
        return stage;
    }
}
