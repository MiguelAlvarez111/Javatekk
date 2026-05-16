package javatekk.world;

import java.util.Objects;

public final class SingleBlockChunkData implements IChunkData {
    private Block block;

    public SingleBlockChunkData() {
        this(BlockRegistry.get(BlockRegistry.AIR));
    }

    public SingleBlockChunkData(Block block) {
        this.block = Objects.requireNonNull(block, "block");
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        Chunk.requireInBounds(x, y, z);
        return block;
    }

    @Override
    public int getBlockId(int x, int y, int z) {
        Chunk.requireInBounds(x, y, z);
        return block.id;
    }

    @Override
    public void setBlockId(int id, int x, int y, int z) {
        Chunk.requireInBounds(x, y, z);
        block = BlockRegistry.get(id);
    }
}
