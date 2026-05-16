package javatekk.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public final class ChunkMesh {
    private final int chunkX;
    private final int chunkY;
    private final int chunkZ;
    private final Mesh mesh;
    private final int indexCount;

    public ChunkMesh(int chunkX, int chunkY, int chunkZ, Mesh mesh, int indexCount) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.mesh = mesh;
        this.indexCount = indexCount;
    }

    public int chunkX() {
        return chunkX;
    }

    public int chunkY() {
        return chunkY;
    }

    public int chunkZ() {
        return chunkZ;
    }

    public boolean isEmpty() {
        return indexCount == 0;
    }

    public int indexCount() {
        return indexCount;
    }

    public int faceCount() {
        return indexCount / 6;
    }

    public void render(ShaderProgram shader) {
        if (!isEmpty()) {
            mesh.render(shader, GL20.GL_TRIANGLES, 0, indexCount);
        }
    }

    public void dispose() {
        mesh.dispose();
    }
}
