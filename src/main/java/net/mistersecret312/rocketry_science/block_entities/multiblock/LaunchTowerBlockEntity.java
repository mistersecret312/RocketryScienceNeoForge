package net.mistersecret312.rocketry_science.block_entities.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.rocketry_science.init.BlockEntityInit;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class LaunchTowerBlockEntity extends AbstractMultiBlockEntity
{
    public LaunchTowerBlockEntity(BlockPos pos, BlockState state)
    {
        super(BlockEntityInit.LAUNCH_TOWER.get(), pos, state);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    protected boolean isValidConnection(BlockPos otherPos, BlockState otherState)
    {
        return this.worldPosition.getX() == otherPos.getX() && this.worldPosition.getZ() == otherPos.getZ();
    }

    public int getTotalHeight()
    {
        Set<BlockPos> network = getAllNetworkPositions();

        int minY = network.stream().mapToInt(BlockPos::getY).min().orElse(this.worldPosition.getY());
        int maxY = network.stream().mapToInt(BlockPos::getY).max().orElse(this.worldPosition.getY());

        return (maxY - minY) + 1;
    }
}