package com.gamexy.math;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Shared angle-wrapping and XZ-plane spatial math utilities.
 */
public final class AngleMath {

    private AngleMath() {}

    /**
     * Returns the signed shortest angular difference in degrees in the range [-180, 180]
     * from {@code fromDeg} to {@code toDeg}.
     */
    public static float shortestAngleDiff(float fromDeg, float toDeg) {
        float diff = (toDeg - fromDeg + 180f) % 360f - 180f;
        return diff < -180f ? diff + 360f : diff;
    }

    /**
     * Returns squared horizontal (XZ) distance between two world positions.
     */
    public static float distanceSquaredXZ(Vector3 a, Vector3 b) {
        float dx = b.x - a.x;
        float dz = b.z - a.z;
        return dx * dx + dz * dz;
    }

    /**
     * Returns horizontal (XZ) Euclidean distance between two world positions.
     */
    public static float distanceXZ(Vector3 a, Vector3 b) {
        return (float) Math.sqrt(distanceSquaredXZ(a, b));
    }

    /**
     * Returns the Y-axis facing angle in degrees (0 = +Z) pointing from {@code from} toward {@code to}.
     */
    public static float facingDegreesTo(Vector3 from, Vector3 to) {
        return MathUtils.atan2(to.x - from.x, to.z - from.z) * MathUtils.radiansToDegrees;
    }
}
