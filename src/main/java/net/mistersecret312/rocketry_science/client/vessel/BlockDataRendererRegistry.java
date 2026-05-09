package net.mistersecret312.rocketry_science.client.vessel;

import net.mistersecret312.rocketry_science.vessel.block_data.BlockData;
import net.mistersecret312.rocketry_science.vessel.block_data.BlockDataType;

import java.util.HashMap;
import java.util.Map;

public class BlockDataRendererRegistry
{
    private static final Map<BlockDataType<?>, IBlockDataRenderer<?>> RENDERERS = new HashMap<>();

    public static <T extends BlockData> void register(BlockDataType<T> type, IBlockDataRenderer<T> renderer) {
        RENDERERS.put(type, renderer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockData> IBlockDataRenderer<T> getRenderer(BlockDataType<T> type) {
        return (IBlockDataRenderer<T>) RENDERERS.get(type);
    }
}