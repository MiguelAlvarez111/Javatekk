package javatekk.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import java.util.Objects;

public final class FlyCameraController {
    private static final float DEFAULT_MOUSE_SENSITIVITY = 0.12f;
    private static final float MAX_PITCH_DEGREES = 89.0f;

    private final PerspectiveCamera camera;
    private final Vector3 forward = new Vector3();
    private final Vector3 right = new Vector3();
    private final Vector3 movement = new Vector3();
    private final Vector3 scratch = new Vector3();

    private float speed;
    private float mouseSensitivity = DEFAULT_MOUSE_SENSITIVITY;
    private float yawDegrees;
    private float pitchDegrees;
    private boolean helpVisible = true;

    public FlyCameraController(PerspectiveCamera camera, float speed, Vector3 startPosition, Vector3 lookAt) {
        this.camera = Objects.requireNonNull(camera, "camera");
        this.speed = speed;
        camera.position.set(Objects.requireNonNull(startPosition, "startPosition"));
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
            camera.position.add(movement);
            camera.update();
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
}
