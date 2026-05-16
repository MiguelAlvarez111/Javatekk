package javatekk.world;

public final class ByteChunkData implements IChunkData {
    private final byte[] blockIDs;

    public ByteChunkData() {
        this.blockIDs = new byte[Chunk.NUM_BLOCKS_IN_CHUNK];
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return BlockRegistry.get(getBlockId(x, y, z));
    }

    @Override
    public int getBlockId(int x, int y, int z) {
        int index = Chunk.index(x, y, z);
        return blockIDs[index] & 0xFF;
    }

    @Override
    public void setBlockId(int id, int x, int y, int z) {
        BlockRegistry.validateId(id);
        int index = Chunk.index(x, y, z);
        blockIDs[index] = (byte) id;
    }
}
