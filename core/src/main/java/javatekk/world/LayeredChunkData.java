package javatekk.world;

public final class LayeredChunkData implements IChunkData {
    private final ILayeredChunkDataLayer[] layers = new ILayeredChunkDataLayer[Chunk.CHUNK_HEIGHT];

    public ILayeredChunkDataLayer getLayer(int y) {
        Chunk.requireLayerY(y);
        return layers[y];
    }

    public void setLayer(int y, ILayeredChunkDataLayer layer) {
        Chunk.requireLayerY(y);
        layers[y] = layer;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        Chunk.requireInBounds(x, y, z);
        ILayeredChunkDataLayer layer = layers[y];
        if (layer == null) {
            return BlockRegistry.get(BlockRegistry.AIR);
        }
        return layer.getBlock(x, z);
    }

    @Override
    public int getBlockId(int x, int y, int z) {
        Chunk.requireInBounds(x, y, z);
        ILayeredChunkDataLayer layer = layers[y];
        if (layer == null) {
            return BlockRegistry.AIR;
        }
        return layer.getBlockId(x, z);
    }

    @Override
    public void setBlockId(int id, int x, int y, int z) {
        BlockRegistry.validateId(id);
        Chunk.requireInBounds(x, y, z);

        ILayeredChunkDataLayer layer = layers[y];
        if (layer == null && id == BlockRegistry.AIR) {
            return;
        }

        MutableLayer mutableLayer;
        if (layer instanceof MutableLayer existingMutableLayer) {
            mutableLayer = existingMutableLayer;
        } else {
            mutableLayer = new MutableLayer(layer);
            layers[y] = mutableLayer;
        }

        mutableLayer.setBlockId(id, x, z);
    }

    private static final class MutableLayer implements ILayeredChunkDataLayer {
        private final byte[] blockIDs = new byte[Chunk.CHUNK_WIDTH * Chunk.CHUNK_LENGTH];

        private MutableLayer(ILayeredChunkDataLayer source) {
            if (source == null) {
                return;
            }

            for (int z = 0; z < Chunk.CHUNK_LENGTH; z++) {
                for (int x = 0; x < Chunk.CHUNK_WIDTH; x++) {
                    int blockId = BlockRegistry.validateId(source.getBlockId(x, z));
                    blockIDs[index(x, z)] = (byte) blockId;
                }
            }
        }

        @Override
        public Block getBlock(int x, int z) {
            return BlockRegistry.get(getBlockId(x, z));
        }

        @Override
        public int getBlockId(int x, int z) {
            Chunk.requireLayerXZ(x, z);
            return blockIDs[index(x, z)] & 0xFF;
        }

        private void setBlockId(int id, int x, int z) {
            BlockRegistry.validateId(id);
            Chunk.requireLayerXZ(x, z);
            blockIDs[index(x, z)] = (byte) id;
        }

        private static int index(int x, int z) {
            return x + z * Chunk.CHUNK_WIDTH;
        }
    }
}
