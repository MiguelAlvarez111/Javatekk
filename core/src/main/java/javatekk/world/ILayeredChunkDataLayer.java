package javatekk.world;

public interface ILayeredChunkDataLayer {
    Block getBlock(int x, int z);

    int getBlockId(int x, int z);
}
