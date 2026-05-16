package javatekk.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import javatekk.world.VoxelWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class VoxelRenderer {
    private final VoxelWorld world;
    private final PerspectiveCamera camera;
    private final ShaderProgram shader;
    private final List<ChunkMesh> chunkMeshes = new ArrayList<>();
    private final Vector3 worldCenter = new Vector3();
    private int generatedFaceCount;
    private int generatedIndexCount;

    public VoxelRenderer(VoxelWorld world) {
        this.world = Objects.requireNonNull(world, "world");
        this.camera = createCamera();
        this.shader = createShader();
        buildChunkMeshes();
    }

    public void render() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        shader.bind();
        shader.setUniformMatrix("u_projViewTrans", camera.combined);
        for (ChunkMesh chunkMesh : chunkMeshes) {
            chunkMesh.render(shader);
        }
    }

    public PerspectiveCamera camera() {
        return camera;
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    public void dispose() {
        for (ChunkMesh chunkMesh : chunkMeshes) {
            chunkMesh.dispose();
        }
        chunkMeshes.clear();
        shader.dispose();
    }

    public int chunkMeshCount() {
        return chunkMeshes.size();
    }

    public int generatedFaceCount() {
        return generatedFaceCount;
    }

    public int generatedIndexCount() {
        return generatedIndexCount;
    }

    private PerspectiveCamera createCamera() {
        PerspectiveCamera createdCamera = new PerspectiveCamera(
            67.0f,
            Math.max(1, Gdx.graphics.getWidth()),
            Math.max(1, Gdx.graphics.getHeight())
        );
        worldCenter.set(VoxelWorld.WORLD_WIDTH * 0.5f, 6.0f, VoxelWorld.WORLD_LENGTH * 0.5f);
        createdCamera.position.set(VoxelWorld.WORLD_WIDTH + 24.0f, 36.0f, VoxelWorld.WORLD_LENGTH + 24.0f);
        createdCamera.lookAt(worldCenter);
        createdCamera.near = 0.1f;
        createdCamera.far = 220.0f;
        createdCamera.update();
        return createdCamera;
    }

    private ShaderProgram createShader() {
        ShaderProgram.pedantic = false;
        ShaderProgram createdShader = new ShaderProgram(vertexShader(), fragmentShader());
        if (!createdShader.isCompiled()) {
            throw new IllegalStateException("Voxel shader compilation failed: " + createdShader.getLog());
        }
        return createdShader;
    }

    private void buildChunkMeshes() {
        ChunkMeshBuilder meshBuilder = new ChunkMeshBuilder();
        int totalFaces = 0;
        int totalIndices = 0;
        for (int chunkZ = 0; chunkZ < VoxelWorld.WORLD_CHUNKS_Z; chunkZ++) {
            for (int chunkY = 0; chunkY < VoxelWorld.WORLD_CHUNKS_Y; chunkY++) {
                for (int chunkX = 0; chunkX < VoxelWorld.WORLD_CHUNKS_X; chunkX++) {
                    ChunkMesh chunkMesh = meshBuilder.build(world, chunkX, chunkY, chunkZ);
                    chunkMeshes.add(chunkMesh);
                    totalFaces += chunkMesh.faceCount();
                    totalIndices += chunkMesh.indexCount();
                }
            }
        }
        generatedFaceCount = totalFaces;
        generatedIndexCount = totalIndices;
    }

    private static String vertexShader() {
        return """
            attribute vec3 a_position;
            attribute vec4 a_color;
            uniform mat4 u_projViewTrans;
            varying vec4 v_color;

            void main() {
                v_color = a_color;
                gl_Position = u_projViewTrans * vec4(a_position, 1.0);
            }
            """;
    }

    private static String fragmentShader() {
        return """
            #ifdef GL_ES
            precision mediump float;
            #endif

            varying vec4 v_color;

            void main() {
                gl_FragColor = v_color;
            }
            """;
    }
}
