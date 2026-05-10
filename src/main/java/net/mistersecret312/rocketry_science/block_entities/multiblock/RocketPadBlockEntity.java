package net.mistersecret312.rocketry_science.block_entities.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.mistersecret312.rocketry_science.data.rocket_pad.RocketPadData;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import org.joml.Vector2i;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RocketPadBlockEntity extends AbstractMultiBlockEntity
{
    private UUID uuid = UUID.randomUUID();

    public RocketPadBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockEntityInit.ROCKET_PAD.get(), pos, state);
    }

    @Override
    protected boolean isValidConnection(BlockPos otherPos, BlockState otherState)
    {
        return this.worldPosition.getY() == otherPos.getY();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        if(this.isMaster() && this.getUUID() != null)
            tag.putUUID("pad_uuid", this.uuid);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        if(this.isMaster())
            this.uuid = tag.getUUID("pad_uuid");
    }

    public UUID getUUID()
    {
        if(!this.isMaster() && this.level != null)
        {
            BlockEntity master = this.level.getBlockEntity(this.getMasterPos());
            if(master instanceof RocketPadBlockEntity pad)
                return pad.getUUID();
        }
        return uuid;
    }

    public Vector2i getDimensions()
    {
        Set<BlockPos> network = getAllNetworkPositions();

        int minX = network.stream().mapToInt(BlockPos::getX).min().orElse(this.worldPosition.getX());
        int maxX = network.stream().mapToInt(BlockPos::getX).max().orElse(this.worldPosition.getX());

        int minZ = network.stream().mapToInt(BlockPos::getZ).min().orElse(this.worldPosition.getZ());
        int maxZ = network.stream().mapToInt(BlockPos::getZ).max().orElse(this.worldPosition.getZ());

        int sizeX = (maxX - minX) + 1;
        int sizeZ = (maxZ - minZ) + 1;

        return new Vector2i(sizeX, sizeZ);
    }

    @Override
    public void formNetwork()
    {
        if(getLevel() != null && !getLevel().isClientSide() && isMaster())
        {
            RocketPadData data = RocketPadData.get(getLevel());
            data.addRocketPad(getUUID(), getBlockPos(), getLevel().dimension());
        }
        super.formNetwork();
    }

    @Override
    public void breakNetwork()
    {
        if(getLevel() != null && !getLevel().isClientSide() && isMaster())
        {
            RocketPadData data = RocketPadData.get(getLevel());
            data.rocketPads.remove(getUUID());
        }
        super.breakNetwork();
    }

    public boolean isComplete()
    {
        Set<BlockPos> network = getAllNetworkPositions();
        if (network.isEmpty() || getTower() == null)
            return false;

        Vector2i dims = getDimensions();

        int expectedBlockCount = dims.x * dims.y;
        return network.size() == expectedBlockCount;
    }

    public AABB getOnPadBox()
    {
        LaunchTowerBlockEntity tower = getTower();
        if (!isComplete())
            return null;

        int towerHeight = tower.getTotalHeight();

        Set<BlockPos> padNetwork = getAllNetworkPositions();

        int minX = padNetwork.stream().mapToInt(BlockPos::getX).min().orElse(this.worldPosition.getX());
        int maxX = padNetwork.stream().mapToInt(BlockPos::getX).max().orElse(this.worldPosition.getX());

        int minZ = padNetwork.stream().mapToInt(BlockPos::getZ).min().orElse(this.worldPosition.getZ());
        int maxZ = padNetwork.stream().mapToInt(BlockPos::getZ).max().orElse(this.worldPosition.getZ());

        int padY = this.worldPosition.getY();

        return new AABB(
                minX, padY + 1, minZ,
                maxX, padY + towerHeight-1, maxZ
        );
    }

    public LaunchTowerBlockEntity getTower()
    {
        Level level = this.getLevel();
        if(level == null)
            return null;

        for (BlockPos pos : this.getAllNetworkPositions())
        {
            for(Direction direction : Direction.values())
            {
                BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));
                if(blockEntity instanceof LaunchTowerBlockEntity tower)
                    return tower;
            }
        }

        return null;
    }
}