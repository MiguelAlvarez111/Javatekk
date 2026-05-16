package javatekk.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class VoxelWorld {
    public static final int WORLD_CHUNKS_X = 4;
    public static final int WORLD_CHUNKS_Y = 1;
    public static final int WORLD_CHUNKS_Z = 4;

    public static final int WORLD_WIDTH = WORLD_CHUNKS_X * Chunk.CHUNK_WIDTH;
    public static final int WORLD_HEIGHT = WORLD_CHUNKS_Y * Chunk.CHUNK_HEIGHT;
    public static final int WORLD_LENGTH = WORLD_CHUNKS_Z * Chunk.CHUNK_LENGTH;

    private static final int MIN_TERRAIN_HEIGHT = 3;
    private static final int MAX_TERRAIN_HEIGHT = 12;
    private static final int TREE_TRUNK_HEIGHT = 3;
    private static final int PLATFORM_Y = 10;
    private static final int[][] DEMO_TREES = {
        {18, 28},
        {20, 36},
        {22, 24},
        {28, 24},
        {36, 24},
        {46, 38},
        {43, 44}
    };

    private final Chunk[] chunks = new Chunk[WORLD_CHUNKS_X * WORLD_CHUNKS_Y * WORLD_CHUNKS_Z];
    private final List<Chunk> chunkList;

    public VoxelWorld() {
        ArrayList<Chunk> createdChunks = new ArrayList<>(chunks.length);
        for (int z = 0; z < WORLD_CHUNKS_Z; z++) {
            for (int y = 0; y < WORLD_CHUNKS_Y; y++) {
                for (int x = 0; x < WORLD_CHUNKS_X; x++) {
                    Chunk chunk = new Chunk(new ByteChunkData());
                    chunks[chunkIndex(x, y, z)] = chunk;
                    createdChunks.add(chunk);
                }
            }
        }

        chunkList = Collections.unmodifiableList(createdChunks);
        generateTerrain();
        buildPresentationScene();
        generateTrees();
    }

    public static VoxelWorld createDemoWorld() {
        return new VoxelWorld();
    }

    public List<Chunk> chunks() {
        return chunkList;
    }

    public int chunkCount() {
        return chunks.length;
    }

    public Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
        if (!chunkInBounds(chunkX, chunkY, chunkZ)) {
            throw new IndexOutOfBoundsException("Chunk coordinates out of world bounds: "
                + chunkX + ", " + chunkY + ", " + chunkZ);
        }
        return chunks[chunkIndex(chunkX, chunkY, chunkZ)];
    }

    public int getBlockIdGlobal(int x, int y, int z) {
        if (y < 0) {
            return BlockRegistry.STONE;
        }
        if (!inBoundsGlobal(x, y, z)) {
            return BlockRegistry.AIR;
        }

        Chunk chunk = chunkAtGlobal(x, y, z);
        return chunk.getBlockId(localX(x), localY(y), localZ(z));
    }

    public Block getBlockGlobal(int x, int y, int z) {
        return BlockRegistry.get(getBlockIdGlobal(x, y, z));
    }

    public boolean isOpaqueGlobal(int x, int y, int z) {
        return BlockRegistry.isOpaque(getBlockIdGlobal(x, y, z));
    }

    public int countNonAirBlocks() {
        int nonAir = 0;
        for (int z = 0; z < WORLD_LENGTH; z++) {
            for (int y = 0; y < WORLD_HEIGHT; y++) {
                for (int x = 0; x < WORLD_WIDTH; x++) {
                    if (getBlockIdGlobal(x, y, z) != BlockRegistry.AIR) {
                        nonAir++;
                    }
                }
            }
        }
        return nonAir;
    }

    public static SelfCheckResult selfCheck() {
        VoxelWorld world = new VoxelWorld();
        int nonAir = 0;
        int grass = 0;
        int dirt = 0;
        int stone = 0;
        int wood = 0;
        int leaves = 0;

        for (int z = 0; z < WORLD_LENGTH; z++) {
            for (int y = 0; y < WORLD_HEIGHT; y++) {
                for (int x = 0; x < WORLD_WIDTH; x++) {
                    int id = world.getBlockIdGlobal(x, y, z);
                    if (id != BlockRegistry.AIR) {
                        nonAir++;
                    }
                    if (id == BlockRegistry.GRASS) {
                        grass++;
                    } else if (id == BlockRegistry.DIRT) {
                        dirt++;
                    } else if (id == BlockRegistry.STONE) {
                        stone++;
                    } else if (id == BlockRegistry.WOOD) {
                        wood++;
                    } else if (id == BlockRegistry.LEAVES) {
                        leaves++;
                    }
                }
            }
        }

        boolean outsideAir = world.getBlockIdGlobal(-1, 0, 0) == BlockRegistry.AIR
            && world.getBlockIdGlobal(WORLD_WIDTH, 0, 0) == BlockRegistry.AIR
            && world.getBlockIdGlobal(0, WORLD_HEIGHT, 0) == BlockRegistry.AIR
            && world.getBlockIdGlobal(0, 0, WORLD_LENGTH) == BlockRegistry.AIR;
        boolean belowWorldSolid = world.getBlockIdGlobal(0, -1, 0) == BlockRegistry.STONE
            && world.isOpaqueGlobal(0, -1, 0);

        SelfCheckResult result = new SelfCheckResult(
            nonAir,
            grass,
            dirt,
            stone,
            wood,
            leaves,
            outsideAir,
            belowWorldSolid
        );

        if (!result.passed()) {
            throw new IllegalStateException("VoxelWorld self check failed: " + result);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(selfCheck());
    }

    private void generateTerrain() {
        for (int z = 0; z < WORLD_LENGTH; z++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                int height = terrainHeight(x, z);
                for (int y = 0; y <= height; y++) {
                    int blockId = blockForTerrainLayer(y, height);
                    setBlockIdGlobal(blockId, x, y, z);
                }
            }
        }
    }

    private void generateTrees() {
        for (int[] tree : DEMO_TREES) {
            placeTree(tree[0], tree[1]);
        }
    }

    private void buildPresentationScene() {
        buildPlatform(24, 27, 41, 40);
        buildDemoHouse(30, 30);
        buildMarkerColumn(42, 34);
    }

    private void buildPlatform(int minX, int minZ, int maxX, int maxZ) {
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                int terrainY = terrainHeight(x, z);
                for (int y = terrainY + 1; y < PLATFORM_Y; y++) {
                    setBlockIdGlobal(BlockRegistry.DIRT, x, y, z);
                }
                for (int y = PLATFORM_Y + 1; y < WORLD_HEIGHT; y++) {
                    setBlockIdGlobal(BlockRegistry.AIR, x, y, z);
                }

                boolean border = x == minX || x == maxX || z == minZ || z == maxZ;
                setBlockIdGlobal(border ? BlockRegistry.STONE : BlockRegistry.DIRT, x, PLATFORM_Y, z);
            }
        }
    }

    private void buildDemoHouse(int originX, int originZ) {
        int minX = originX;
        int maxX = originX + 6;
        int minZ = originZ;
        int maxZ = originZ + 6;
        int floorY = PLATFORM_Y + 1;
        int wallTopY = floorY + 2;
        int roofY = wallTopY + 1;

        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                setBlockIdGlobal(BlockRegistry.STONE, x, floorY, z);
                for (int y = floorY + 1; y <= wallTopY; y++) {
                    boolean wall = x == minX || x == maxX || z == minZ || z == maxZ;
                    boolean doorway = z == maxZ && x == originX + 3 && y <= floorY + 2;
                    setBlockIdGlobal(wall && !doorway ? BlockRegistry.WOOD : BlockRegistry.AIR, x, y, z);
                }
            }
        }

        for (int z = minZ - 1; z <= maxZ + 1; z++) {
            for (int x = minX - 1; x <= maxX + 1; x++) {
                setBlockIdGlobal(BlockRegistry.STONE, x, roofY, z);
            }
        }
    }

    private void buildMarkerColumn(int x, int z) {
        int baseY = PLATFORM_Y + 1;
        for (int y = baseY; y <= baseY + 4; y++) {
            setBlockIdGlobal(y == baseY + 4 ? BlockRegistry.LEAVES : BlockRegistry.STONE, x, y, z);
        }
    }

    private boolean placeTree(int x, int z) {
        int groundY = terrainHeight(x, z);
        int trunkBaseY = groundY + 1;
        int crownCenterY = trunkBaseY + TREE_TRUNK_HEIGHT;
        if (crownCenterY + 1 >= WORLD_HEIGHT) {
            return false;
        }

        for (int y = trunkBaseY; y < trunkBaseY + TREE_TRUNK_HEIGHT; y++) {
            setBlockIdGlobal(BlockRegistry.WOOD, x, y, z);
        }

        for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dx = -2; dx <= 2; dx++) {
                    int distance = Math.abs(dx) + Math.abs(dz) + Math.abs(dy);
                    if (distance > 3) {
                        continue;
                    }

                    int leafX = x + dx;
                    int leafY = crownCenterY + dy;
                    int leafZ = z + dz;
                    if (inBoundsGlobal(leafX, leafY, leafZ)
                        && getBlockIdGlobal(leafX, leafY, leafZ) == BlockRegistry.AIR) {
                        setBlockIdGlobal(BlockRegistry.LEAVES, leafX, leafY, leafZ);
                    }
                }
            }
        }

        return true;
    }

    private void setBlockIdGlobal(int id, int x, int y, int z) {
        if (!inBoundsGlobal(x, y, z)) {
            return;
        }

        Chunk chunk = chunkAtGlobal(x, y, z);
        chunk.setBlockId(id, localX(x), localY(y), localZ(z));
    }

    private Chunk chunkAtGlobal(int x, int y, int z) {
        int chunkX = x / Chunk.CHUNK_WIDTH;
        int chunkY = y / Chunk.CHUNK_HEIGHT;
        int chunkZ = z / Chunk.CHUNK_LENGTH;
        return chunks[chunkIndex(chunkX, chunkY, chunkZ)];
    }

    private static int chunkIndex(int chunkX, int chunkY, int chunkZ) {
        return chunkX + chunkY * WORLD_CHUNKS_X + chunkZ * WORLD_CHUNKS_X * WORLD_CHUNKS_Y;
    }

    private static boolean chunkInBounds(int chunkX, int chunkY, int chunkZ) {
        return chunkX >= 0 && chunkX < WORLD_CHUNKS_X
            && chunkY >= 0 && chunkY < WORLD_CHUNKS_Y
            && chunkZ >= 0 && chunkZ < WORLD_CHUNKS_Z;
    }

    private static int localX(int x) {
        return x % Chunk.CHUNK_WIDTH;
    }

    private static int localY(int y) {
        return y % Chunk.CHUNK_HEIGHT;
    }

    private static int localZ(int z) {
        return z % Chunk.CHUNK_LENGTH;
    }

    private static boolean inBoundsGlobal(int x, int y, int z) {
        return x >= 0 && x < WORLD_WIDTH
            && y >= 0 && y < WORLD_HEIGHT
            && z >= 0 && z < WORLD_LENGTH;
    }

    private static int terrainHeight(int x, int z) {
        double rolling = Math.sin(x * 0.18) * 2.4;
        double ridges = Math.cos(z * 0.21) * 1.8;
        double diagonal = Math.sin((x + z) * 0.11) * 1.3;
        int height = 7 + (int) Math.round(rolling + ridges + diagonal);
        return clamp(height, MIN_TERRAIN_HEIGHT, MAX_TERRAIN_HEIGHT);
    }

    private static int blockForTerrainLayer(int y, int terrainHeight) {
        if (y == terrainHeight) {
            return BlockRegistry.GRASS;
        }
        if (y >= terrainHeight - 3) {
            return BlockRegistry.DIRT;
        }
        return BlockRegistry.STONE;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public record SelfCheckResult(
        int nonAirBlocks,
        int grassBlocks,
        int dirtBlocks,
        int stoneBlocks,
        int woodBlocks,
        int leavesBlocks,
        boolean outsideWorldIsAir,
        boolean belowWorldIsSolid
    ) {
        public boolean passed() {
            return nonAirBlocks > 0
                && grassBlocks > 0
                && dirtBlocks > 0
                && stoneBlocks > 0
                && woodBlocks > 0
                && leavesBlocks > 0
                && outsideWorldIsAir
                && belowWorldIsSolid;
        }
    }
}
