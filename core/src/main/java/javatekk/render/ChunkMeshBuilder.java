package javatekk.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import javatekk.world.Block;
import javatekk.world.BlockRegistry;
import javatekk.world.Chunk;
import javatekk.world.VoxelWorld;

public final class ChunkMeshBuilder {
    private static final int FLOATS_PER_VERTEX = 4;
    private static final int MAX_UNSIGNED_SHORT_INDEX = 65_535;

    private static final int FACE_NEG_X = 0;
    private static final int FACE_POS_X = 1;
    private static final int FACE_NEG_Y = 2;
    private static final int FACE_POS_Y = 3;
    private static final int FACE_NEG_Z = 4;
    private static final int FACE_POS_Z = 5;

    private static final int[][] FACE_OFFSETS = {
        {-1, 0, 0},
        {1, 0, 0},
        {0, -1, 0},
        {0, 1, 0},
        {0, 0, -1},
        {0, 0, 1}
    };

    public ChunkMesh build(VoxelWorld world, int chunkX, int chunkY, int chunkZ) {
        Chunk chunk = world.getChunk(chunkX, chunkY, chunkZ);
        FloatArray vertices = new FloatArray(Chunk.CHUNK_WIDTH * Chunk.CHUNK_LENGTH * 6 * FLOATS_PER_VERTEX);
        ShortArray indices = new ShortArray(Chunk.CHUNK_WIDTH * Chunk.CHUNK_LENGTH * 6 * 6);

        int originX = chunkX * Chunk.CHUNK_WIDTH;
        int originY = chunkY * Chunk.CHUNK_HEIGHT;
        int originZ = chunkZ * Chunk.CHUNK_LENGTH;

        for (int localZ = 0; localZ < Chunk.CHUNK_LENGTH; localZ++) {
            for (int localY = 0; localY < Chunk.CHUNK_HEIGHT; localY++) {
                for (int localX = 0; localX < Chunk.CHUNK_WIDTH; localX++) {
                    int blockId = chunk.getBlockId(localX, localY, localZ);
                    if (blockId == BlockRegistry.AIR) {
                        continue;
                    }

                    int worldX = originX + localX;
                    int worldY = originY + localY;
                    int worldZ = originZ + localZ;
                    Block block = BlockRegistry.get(blockId);

                    for (int face = 0; face < FACE_OFFSETS.length; face++) {
                        int[] offset = FACE_OFFSETS[face];
                        if (!world.isOpaqueGlobal(worldX + offset[0], worldY + offset[1], worldZ + offset[2])) {
                            addFace(vertices, indices, face, worldX, worldY, worldZ, block);
                        }
                    }
                }
            }
        }

        int vertexCount = vertices.size / FLOATS_PER_VERTEX;
        int indexCount = indices.size;
        Mesh mesh = new Mesh(
            true,
            Math.max(1, vertexCount),
            Math.max(1, indexCount),
            VertexAttribute.Position(),
            VertexAttribute.ColorPacked()
        );
        if (vertexCount > 0) {
            mesh.setVertices(vertices.toArray());
            mesh.setIndices(indices.toArray());
        }
        return new ChunkMesh(chunkX, chunkY, chunkZ, mesh, indexCount);
    }

    private static void addFace(FloatArray vertices, ShortArray indices, int face, int x, int y, int z, Block block) {
        int vertexBase = vertices.size / FLOATS_PER_VERTEX;
        if (vertexBase + 3 > MAX_UNSIGNED_SHORT_INDEX) {
            throw new IllegalStateException("Chunk mesh exceeded unsigned short index capacity.");
        }

        float color = colorFor(block, face);
        float x0 = x;
        float y0 = y;
        float z0 = z;
        float x1 = x + 1.0f;
        float y1 = y + 1.0f;
        float z1 = z + 1.0f;

        switch (face) {
            case FACE_NEG_X -> {
                addVertex(vertices, x0, y0, z1, color);
                addVertex(vertices, x0, y1, z1, color);
                addVertex(vertices, x0, y1, z0, color);
                addVertex(vertices, x0, y0, z0, color);
            }
            case FACE_POS_X -> {
                addVertex(vertices, x1, y0, z0, color);
                addVertex(vertices, x1, y1, z0, color);
                addVertex(vertices, x1, y1, z1, color);
                addVertex(vertices, x1, y0, z1, color);
            }
            case FACE_NEG_Y -> {
                addVertex(vertices, x0, y0, z0, color);
                addVertex(vertices, x1, y0, z0, color);
                addVertex(vertices, x1, y0, z1, color);
                addVertex(vertices, x0, y0, z1, color);
            }
            case FACE_POS_Y -> {
                addVertex(vertices, x0, y1, z1, color);
                addVertex(vertices, x1, y1, z1, color);
                addVertex(vertices, x1, y1, z0, color);
                addVertex(vertices, x0, y1, z0, color);
            }
            case FACE_NEG_Z -> {
                addVertex(vertices, x0, y0, z0, color);
                addVertex(vertices, x0, y1, z0, color);
                addVertex(vertices, x1, y1, z0, color);
                addVertex(vertices, x1, y0, z0, color);
            }
            case FACE_POS_Z -> {
                addVertex(vertices, x1, y0, z1, color);
                addVertex(vertices, x1, y1, z1, color);
                addVertex(vertices, x0, y1, z1, color);
                addVertex(vertices, x0, y0, z1, color);
            }
            default -> throw new IllegalArgumentException("Unknown face: " + face);
        }

        addFaceIndices(indices, vertexBase);
    }

    private static void addVertex(FloatArray vertices, float x, float y, float z, float color) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(color);
    }

    private static void addFaceIndices(ShortArray indices, int vertexBase) {
        indices.add((short) vertexBase);
        indices.add((short) (vertexBase + 1));
        indices.add((short) (vertexBase + 2));
        indices.add((short) (vertexBase + 2));
        indices.add((short) (vertexBase + 3));
        indices.add((short) vertexBase);
    }

    private static float colorFor(Block block, int face) {
        float shade = switch (face) {
            case FACE_POS_Y -> 1.0f;
            case FACE_NEG_Y -> 0.48f;
            case FACE_POS_Z -> 0.78f;
            case FACE_NEG_Z -> 0.66f;
            default -> 0.72f;
        };
        return Color.toFloatBits(block.r * shade, block.g * shade, block.b * shade, 1.0f);
    }
}
