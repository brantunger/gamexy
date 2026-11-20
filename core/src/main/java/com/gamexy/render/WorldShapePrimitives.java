package com.gamexy.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Reusable 3D world-space ShapeRenderer line primitives (rings and arcs on horizontal planes).
 */
public final class WorldShapePrimitives {

    private WorldShapePrimitives() {}

    public static void drawRing(ShapeRenderer sr, float cx, float y, float cz, float radius, int segments) {
        for (int i = 0; i < segments; i++) {
            float a0 = (MathUtils.PI2 * i) / segments;
            float a1 = (MathUtils.PI2 * (i + 1)) / segments;
            sr.line(
                cx + MathUtils.sin(a0) * radius, y, cz + MathUtils.cos(a0) * radius,
                cx + MathUtils.sin(a1) * radius, y, cz + MathUtils.cos(a1) * radius
            );
        }
    }

    public static void drawArc(ShapeRenderer sr, float cx, float y, float cz,
                               float radius, float startDeg, float sweepDeg, int segments) {
        for (int i = 0; i < segments; i++) {
            float a0 = (startDeg + sweepDeg * (i / (float) segments)) * MathUtils.degreesToRadians;
            float a1 = (startDeg + sweepDeg * ((i + 1) / (float) segments)) * MathUtils.degreesToRadians;
            sr.line(
                cx + MathUtils.sin(a0) * radius, y, cz + MathUtils.cos(a0) * radius,
                cx + MathUtils.sin(a1) * radius, y, cz + MathUtils.cos(a1) * radius
            );
        }
    }
}
