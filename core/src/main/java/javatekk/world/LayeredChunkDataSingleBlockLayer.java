package javatekk.world;

import java.util.Objects;

public final class LayeredChunkDataSingleBlockLayer implements ILayeredChunkDataLayer {
    private final Block block;

    public LayeredChunkDataSingleBlockLayer() {
        this(BlockRegistry.get(BlockRegistry.AIR));
    }

    public LayeredChunkDataSingleBlockLayer(Block block) {
        this.block = Objects.requireNonNull(block, "block");
    }

    @Override
    public Block getBlock(int x, int z) {
        Chunk.requireLayerXZ(x, z);
        return block;
    }

    @Override
    public int getBlockId(int x, int z) {
        Chunk.requireLayerXZ(x, z);
        return block.id;
    }
}
