package javatekk.engine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import javatekk.render.VoxelRenderer;
import javatekk.world.VoxelWorld;

public final class JavatekkGame extends ApplicationAdapter {
    private static final String LOG_TAG = "Javatekk";
    private static final float SKY_RED = 0.52f;
    private static final float SKY_GREEN = 0.74f;
    private static final float SKY_BLUE = 0.92f;
    private static final float CAMERA_SPEED = 18.0f;

    private VoxelRenderer voxelRenderer;
    private FlyCameraController flyCameraController;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera hudCamera;

    @Override
    public void create() {
        VoxelWorld world = VoxelWorld.createDemoWorld();
        voxelRenderer = new VoxelRenderer(world);
        flyCameraController = new FlyCameraController(
            voxelRenderer.camera(),
            CAMERA_SPEED,
            new Vector3(VoxelWorld.WORLD_WIDTH * 0.5f, 18.0f, VoxelWorld.WORLD_LENGTH * 0.5f + 24.0f),
            new Vector3(VoxelWorld.WORLD_WIDTH * 0.5f, 10.0f, VoxelWorld.WORLD_LENGTH * 0.5f)
        );

        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.1f);
        font.setColor(Color.WHITE);
        shapeRenderer = new ShapeRenderer();
        hudCamera = new OrthographicCamera();
        updateHudViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        logStartup(world);
    }

    @Override
    public void render() {
        float deltaTime = Math.min(Gdx.graphics.getDeltaTime(), 1.0f / 15.0f);
        flyCameraController.update(deltaTime);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(SKY_RED, SKY_GREEN, SKY_BLUE, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        voxelRenderer.render();

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        drawCrosshair();
        drawPresentationOverlay();
        if (flyCameraController.isHelpVisible()) {
            drawDetailedHud();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (voxelRenderer != null) {
            voxelRenderer.resize(width, height);
        }
        if (hudCamera != null) {
            updateHudViewport(width, height);
        }
    }

    @Override
    public void dispose() {
        if (voxelRenderer != null) {
            voxelRenderer.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
    }

    private void updateHudViewport(int width, int height) {
        hudCamera.setToOrtho(false, Math.max(1, width), Math.max(1, height));
        spriteBatch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
    }

    private void drawCrosshair() {
        float centerX = Gdx.graphics.getWidth() * 0.5f;
        float centerY = Gdx.graphics.getHeight() * 0.5f;
        float gap = 4.0f;
        float length = 10.0f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1.0f, 1.0f, 1.0f, 0.9f);
        shapeRenderer.line(centerX - length, centerY, centerX - gap, centerY);
        shapeRenderer.line(centerX + gap, centerY, centerX + length, centerY);
        shapeRenderer.line(centerX, centerY - length, centerX, centerY - gap);
        shapeRenderer.line(centerX, centerY + gap, centerX, centerY + length);
        shapeRenderer.end();
    }

    private void drawPresentationOverlay() {
        float top = Gdx.graphics.getHeight() - 14.0f;

        spriteBatch.begin();
        drawText("Javatekk MVP", 12.0f, top, 0.98f, 0.96f, 0.72f, 1.0f);
        drawText("WASD mover | Mouse mirar | Space/Shift altura | ESC mouse | F1 ayuda",
            12.0f, top - 24.0f, 1.0f, 1.0f, 1.0f, 1.0f);
        drawText("Chunks: 4x1x4 | Block storage: byte[] | Hidden-face culling",
            12.0f, top - 44.0f, 0.78f, 0.92f, 1.0f, 1.0f);
        spriteBatch.end();
    }

    private void drawDetailedHud() {
        Vector3 position = flyCameraController.position();
        float top = Gdx.graphics.getHeight() - 78.0f;

        spriteBatch.begin();
        drawText("FPS: " + Gdx.graphics.getFramesPerSecond(), 12.0f, top, 1.0f, 1.0f, 1.0f, 1.0f);
        drawText("Pos: x=" + oneDecimal(position.x)
            + " y=" + oneDecimal(position.y)
            + " z=" + oneDecimal(position.z), 12.0f, top - 20.0f, 1.0f, 1.0f, 1.0f, 1.0f);
        drawText("Click izquierdo recaptura el mouse si esta libre", 12.0f, top - 40.0f,
            0.84f, 0.92f, 1.0f, 1.0f);
        spriteBatch.end();
    }

    private void drawText(String text, float x, float y, float r, float g, float b, float a) {
        font.setColor(0.0f, 0.0f, 0.0f, 0.75f);
        font.draw(spriteBatch, text, x + 1.0f, y - 1.0f);
        font.setColor(r, g, b, a);
        font.draw(spriteBatch, text, x, y);
    }

    private static String oneDecimal(float value) {
        int scaled = Math.round(value * 10.0f);
        int absolute = Math.abs(scaled);
        String sign = scaled < 0 ? "-" : "";
        return sign + absolute / 10 + "." + absolute % 10;
    }

    private void logStartup(VoxelWorld world) {
        Gdx.app.log(LOG_TAG, "Chunks: " + world.chunkCount()
            + " (" + VoxelWorld.WORLD_CHUNKS_X + "x" + VoxelWorld.WORLD_CHUNKS_Y + "x" + VoxelWorld.WORLD_CHUNKS_Z + ")");
        Gdx.app.log(LOG_TAG, "Non-air blocks: " + world.countNonAirBlocks());
        Gdx.app.log(LOG_TAG, "Generated chunk meshes: " + voxelRenderer.chunkMeshCount());
        Gdx.app.log(LOG_TAG, "Approx visible faces: " + voxelRenderer.generatedFaceCount()
            + " (" + voxelRenderer.generatedIndexCount() + " indices)");
        Gdx.app.log(LOG_TAG, "Initial FPS: " + Gdx.graphics.getFramesPerSecond());
    }
}
