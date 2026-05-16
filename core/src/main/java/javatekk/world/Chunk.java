package javatekk.world;

import java.util.Objects;

public final class Chunk {
    public static final int CHUNK_WIDTH = 16;
    public static final int CHUNK_HEIGHT = 16;
    public static final int CHUNK_LENGTH = 16;
    public static final int NUM_BLOCKS_IN_CHUNK = CHUNK_WIDTH * CHUNK_HEIGHT * CHUNK_LENGTH;

    private final IChunkData data;

    public Chunk() {
        this(new ByteChunkData());
    }

    public Chunk(IChunkData data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    public IChunkData data() {
        return data;
    }

    public Block getBlock(int x, int y, int z) {
        return data.getBlock(x, y, z);
    }

    public int getBlockId(int x, int y, int z) {
        return data.getBlockId(x, y, z);
    }

    public void setBlockId(int id, int x, int y, int z) {
        data.setBlockId(id, x, y, z);
    }

    public static int index(int x, int y, int z) {
        requireInBounds(x, y, z);
        return x + y * CHUNK_WIDTH + z * CHUNK_WIDTH * CHUNK_HEIGHT;
    }

    public static boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < CHUNK_WIDTH
            && y >= 0 && y < CHUNK_HEIGHT
            && z >= 0 && z < CHUNK_LENGTH;
    }

    static void requireInBounds(int x, int y, int z) {
        if (!inBounds(x, y, z)) {
            throw new IndexOutOfBoundsException("Block coordinates out of chunk bounds: " + x + ", " + y + ", " + z);
        }
    }

    static void requireLayerY(int y) {
        if (y < 0 || y >= CHUNK_HEIGHT) {
            throw new IndexOutOfBoundsException("Layer out of chunk bounds: " + y);
        }
    }

    static void requireLayerXZ(int x, int z) {
        if (x < 0 || x >= CHUNK_WIDTH || z < 0 || z >= CHUNK_LENGTH) {
            throw new IndexOutOfBoundsException("Layer coordinates out of chunk bounds: " + x + ", " + z);
        }
    }
}
