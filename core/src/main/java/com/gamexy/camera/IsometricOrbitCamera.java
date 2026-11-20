package com.gamexy.camera;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.gamexy.math.AngleMath;

/**
 * Third-person top-down Diablo-style camera with Alt + Mouse drag orbit/pitch,
 * scroll-wheel zoom, smooth target follow, and ground-plane cursor raycasting.
 */
public class IsometricOrbitCamera {

    public final PerspectiveCamera camera;

    private final Vector3 target = new Vector3(0f, 1.0f, 0f);
    private final Vector3 desiredTarget = new Vector3(0f, 1.0f, 0f);

    private float yawDeg = 180f;
    private float pitchDeg = 46f;
    private float distance = 16.5f;
    private float orbitSensitivity = 1.0f;

    private static final float MIN_PITCH = 26f;
    private static final float MAX_PITCH = 76f;
    private static final float MIN_DISTANCE = 10f;
    private static final float MAX_DISTANCE = 28f;

    public IsometricOrbitCamera(int viewportWidth, int viewportHeight) {
        this.camera = new PerspectiveCamera(46f, viewportWidth, viewportHeight);
        this.camera.near = 0.5f;
        this.camera.far = 160f;
        applyTransform();
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    public void orbit(float deltaX, float deltaY) {
        yawDeg -= deltaX * 0.35f * orbitSensitivity;
        pitchDeg = MathUtils.clamp(pitchDeg + deltaY * 0.30f * orbitSensitivity, MIN_PITCH, MAX_PITCH);
    }

    public float getOrbitSensitivity() {
        return orbitSensitivity;
    }

    public void setOrbitSensitivity(float sensitivity) {
        this.orbitSensitivity = MathUtils.clamp(sensitivity, 0.3f, 2.5f);
    }

    public void resetOrientation() {
        yawDeg = 180f;
        pitchDeg = 46f;
        distance = 16.5f;
    }

    /**
     * Smoothly swings the camera yaw behind the character's facing angle.
     */
    public void followBehind(float characterFacingDeg, float delta) {
        float desiredYaw = characterFacingDeg + 180f;
        float diff = AngleMath.shortestAngleDiff(yawDeg, desiredYaw);
        yawDeg += diff * Math.min(1f, delta * 6.5f);
    }

    public void zoom(float amountY) {
        distance = MathUtils.clamp(distance + amountY * 1.4f, MIN_DISTANCE, MAX_DISTANCE);
    }

    public void setFollowTarget(Vector3 worldPos) {
        desiredTarget.set(worldPos.x, worldPos.y + 1.1f, worldPos.z);
    }

    public void snapToTarget(Vector3 worldPos) {
        desiredTarget.set(worldPos.x, worldPos.y + 1.1f, worldPos.z);
        target.set(desiredTarget);
        applyTransform();
    }

    public void update(float delta) {
        float lerp = Math.min(1f, delta * 9.5f);
        target.lerp(desiredTarget, lerp);
        applyTransform();
    }

    private void applyTransform() {
        float yawRad = yawDeg * MathUtils.degreesToRadians;
        float pitchRad = pitchDeg * MathUtils.degreesToRadians;

        float horiz = distance * MathUtils.cos(pitchRad);
        float offsetX = horiz * MathUtils.sin(yawRad);
        float offsetZ = horiz * MathUtils.cos(yawRad);
        float offsetY = distance * MathUtils.sin(pitchRad);

        camera.position.set(target.x + offsetX, target.y + offsetY, target.z + offsetZ);
        camera.up.set(0f, 1f, 0f);
        camera.lookAt(target);
        camera.update();
    }

    /**
     * Intersects the screen cursor ray with the Y = 0 ground plane.
     */
    public boolean screenToGround(int screenX, int screenY, Vector3 out) {
        Ray ray = camera.getPickRay(screenX, screenY);
        if (Math.abs(ray.direction.y) < 1e-5f) {
            return false;
        }
        float t = -ray.origin.y / ray.direction.y;
        if (t < 0f) {
            return false;
        }
        out.set(ray.origin).mulAdd(ray.direction, t);
        out.y = 0f;
        return true;
    }

    public float getYawDeg() {
        return yawDeg;
    }

    /**
     * Horizontal facing angle in degrees (0 = +Z) pointing along the camera's forward look direction.
     */
    public float getForwardYawDeg() {
        return (yawDeg + 180f) % 360f;
    }

    public float getPitchDeg() {
        return pitchDeg;
    }
}
