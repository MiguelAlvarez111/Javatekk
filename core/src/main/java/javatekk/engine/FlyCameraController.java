package javatekk.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import javatekk.world.VoxelWorld;

import java.util.Objects;

public final class FlyCameraController {
    private static final float DEFAULT_MOUSE_SENSITIVITY = 0.12f;
    private static final float MAX_PITCH_DEGREES = 89.0f;
    private static final float CAMERA_RADIUS = 0.28f;
    private static final float CAMERA_HALF_HEIGHT = 0.45f;
    private static final float COLLISION_STEP = 0.22f;
    private static final float WORLD_EDGE_MARGIN = 0.35f;
    private static final float MIN_CAMERA_Y = 1.20f;
    private static final float MAX_CAMERA_Y = 34.0f;

    private final PerspectiveCamera camera;
    private final VoxelWorld collisionWorld;
    private final Vector3 forward = new Vector3();
    private final Vector3 right = new Vector3();
    private final Vector3 movement = new Vector3();
    private final Vector3 stepMovement = new Vector3();
    private final Vector3 scratch = new Vector3();

    private float speed;
    private float mouseSensitivity = DEFAULT_MOUSE_SENSITIVITY;
    private float yawDegrees;
    private float pitchDegrees;
    private boolean helpVisible = true;

    public FlyCameraController(PerspectiveCamera camera, float speed, Vector3 startPosition, Vector3 lookAt) {
        this(camera, speed, startPosition, lookAt, null);
    }

    public FlyCameraController(
        PerspectiveCamera camera,
        float speed,
        Vector3 startPosition,
        Vector3 lookAt,
        VoxelWorld collisionWorld
    ) {
        this.camera = Objects.requireNonNull(camera, "camera");
        this.collisionWorld = collisionWorld;
        this.speed = speed;
        camera.position.set(Objects.requireNonNull(startPosition, "startPosition"));
        clampPosition(camera.position);
        setAnglesFromDirection(scratch.set(Objects.requireNonNull(lookAt, "lookAt")).sub(startPosition).nor());
        updateCameraDirection();
        Gdx.input.setCursorCatched(true);
    }

    public void update(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            helpVisible = !helpVisible;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
        }
        if (!Gdx.input.isCursorCatched() && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Gdx.input.setCursorCatched(true);
        }

        if (Gdx.input.isCursorCatched()) {
            yawDegrees += Gdx.input.getDeltaX() * mouseSensitivity;
            pitchDegrees -= Gdx.input.getDeltaY() * mouseSensitivity;
            pitchDegrees = MathUtils.clamp(pitchDegrees, -MAX_PITCH_DEGREES, MAX_PITCH_DEGREES);
            updateCameraDirection();
        }

        movement.setZero();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            movement.add(forward);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            movement.sub(forward);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            movement.add(right);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            movement.sub(right);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            movement.y += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            movement.y -= 1.0f;
        }

        if (!movement.isZero(0.0001f)) {
            movement.nor().scl(speed * deltaTime);
            moveWithCollision(movement);
        }
    }

    public boolean isHelpVisible() {
        return helpVisible;
    }

    public Vector3 position() {
        return camera.position;
    }

    public float speed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setMouseSensitivity(float mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }

    private void setAnglesFromDirection(Vector3 direction) {
        pitchDegrees = (float) Math.asin(MathUtils.clamp(direction.y, -1.0f, 1.0f)) * MathUtils.radiansToDegrees;
        yawDegrees = (float) Math.atan2(direction.x, -direction.z) * MathUtils.radiansToDegrees;
    }

    private void updateCameraDirection() {
        float yaw = yawDegrees * MathUtils.degreesToRadians;
        float pitch = pitchDegrees * MathUtils.degreesToRadians;
        float cosPitch = MathUtils.cos(pitch);

        forward.set(
            MathUtils.sin(yaw) * cosPitch,
            MathUtils.sin(pitch),
            -MathUtils.cos(yaw) * cosPitch
        ).nor();
        right.set(forward).crs(Vector3.Y).nor();

        camera.direction.set(forward);
        camera.up.set(Vector3.Y);
        camera.update();
    }

    private void moveWithCollision(Vector3 delta) {
        int steps = Math.max(1, (int) Math.ceil(delta.len() / COLLISION_STEP));
        stepMovement.set(delta).scl(1.0f / steps);
        for (int step = 0; step < steps; step++) {
            tryMove(stepMovement.x, 0.0f, 0.0f);
            tryMove(0.0f, stepMovement.y, 0.0f);
            tryMove(0.0f, 0.0f, stepMovement.z);
        }
        camera.update();
    }

    private void tryMove(float deltaX, float deltaY, float deltaZ) {
        if (deltaX == 0.0f && deltaY == 0.0f && deltaZ == 0.0f) {
            return;
        }

        scratch.set(camera.position).add(deltaX, deltaY, deltaZ);
        clampPosition(scratch);
        if (!collidesWithWorld(scratch)) {
            camera.position.set(scratch);
        }
    }

    private void clampPosition(Vector3 position) {
        if (collisionWorld == null) {
            return;
        }

        position.x = MathUtils.clamp(position.x, WORLD_EDGE_MARGIN, VoxelWorld.WORLD_WIDTH - WORLD_EDGE_MARGIN);
        position.y = MathUtils.clamp(position.y, MIN_CAMERA_Y, MAX_CAMERA_Y);
        position.z = MathUtils.clamp(position.z, WORLD_EDGE_MARGIN, VoxelWorld.WORLD_LENGTH - WORLD_EDGE_MARGIN);
    }

    private boolean collidesWithWorld(Vector3 position) {
        if (collisionWorld == null) {
            return false;
        }

        int minX = MathUtils.floor(position.x - CAMERA_RADIUS);
        int maxX = MathUtils.floor(position.x + CAMERA_RADIUS);
        int minY = MathUtils.floor(position.y - CAMERA_HALF_HEIGHT);
        int maxY = MathUtils.floor(position.y + CAMERA_HALF_HEIGHT);
        int minZ = MathUtils.floor(position.z - CAMERA_RADIUS);
        int maxZ = MathUtils.floor(position.z + CAMERA_RADIUS);

        for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    if (collisionWorld.isOpaqueGlobal(x, y, z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
