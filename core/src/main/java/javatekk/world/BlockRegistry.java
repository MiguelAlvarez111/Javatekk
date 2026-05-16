package javatekk.world;

import java.util.Objects;

public final class BlockRegistry {
    public static final int MAX_BLOCKS = 256;

    public static final int AIR = 0;
    public static final int GRASS = 1;
    public static final int DIRT = 2;
    public static final int STONE = 3;
    public static final int WOOD = 4;
    public static final int LEAVES = 5;
    public static final int WATER = 6;

    private static final Block[] BLOCKS = new Block[MAX_BLOCKS];

    static {
        for (int id = 0; id < BLOCKS.length; id++) {
            BLOCKS[id] = new Block(id, "Reserved " + id, true, 1.0f, 0.0f, 1.0f);
        }

        register(new Block(AIR, "Air", false, 0.0f, 0.0f, 0.0f));
        register(new Block(GRASS, "Grass", true, 0.25f, 0.68f, 0.23f));
        register(new Block(DIRT, "Dirt", true, 0.46f, 0.30f, 0.16f));
        register(new Block(STONE, "Stone", true, 0.48f, 0.50f, 0.52f));
        register(new Block(WOOD, "Wood", true, 0.48f, 0.30f, 0.13f));
        register(new Block(LEAVES, "Leaves", true, 0.16f, 0.50f, 0.18f));
        register(new Block(WATER, "Water", false, 0.12f, 0.35f, 0.78f));
    }

    private BlockRegistry() {
    }

    public static Block get(int id) {
        return BLOCKS[validateId(id)];
    }

    public static boolean isOpaque(int id) {
        return get(id).opaque;
    }

    public static int validateId(int id) {
        if (id < 0 || id >= MAX_BLOCKS) {
            throw new IllegalArgumentException("Block ID must be in range 0..255: " + id);
        }
        return id;
    }

    private static void register(Block block) {
        Objects.requireNonNull(block, "block");
        BLOCKS[validateId(block.id)] = block;
    }
}
