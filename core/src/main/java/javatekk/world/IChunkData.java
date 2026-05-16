package javatekk.world;

public interface IChunkData {
    Block getBlock(int x, int y, int z);

    int getBlockId(int x, int y, int z);

    void setBlockId(int id, int x, int y, int z);
}
