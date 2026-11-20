package com.gamexy.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.gamexy.world.VisualEffect;

/**
 * Renders 3D line-based combat and level-up visual effects.
 */
public final class VisualEffectRenderer {

    private VisualEffectRenderer() {}

    public static void render(ShapeRenderer sr, VisualEffect fx) {
        float t = fx.getProgress();
        float alpha = 1f - t;
        sr.setColor(fx.color.r, fx.color.g, fx.color.b, alpha);

        switch (fx.kind) {
            case CLEAVE_ARC:
                renderCleaveArc(sr, fx, t);
                break;
            case SHOCKWAVE:
                renderShockwave(sr, fx, t);
                break;
            case WHIRLWIND:
                renderWhirlwind(sr, fx, t);
                break;
            case WAR_CRY:
                renderWarCry(sr, fx, t);
                break;
            case LEAP_IMPACT:
                renderLeapImpact(sr, fx, t);
                break;
            case LEVEL_UP:
                renderLevelUp(sr, fx, t);
                break;
            default:
                break;
        }
    }

    private static void renderCleaveArc(ShapeRenderer sr, VisualEffect fx, float t) {
        float y = 1.05f;
        int segments = 18;
        float startDeg = fx.facingDeg - fx.arcDeg * 0.5f;
        float sweep = fx.arcDeg * Math.min(1f, t * 1.8f);
        float rOuter = fx.radius;
        float rInner = fx.radius * 0.45f;

        for (int i = 0; i < segments; i++) {
            float a0 = (startDeg + sweep * (i / (float) segments)) * MathUtils.degreesToRadians;
            float a1 = (startDeg + sweep * ((i + 1) / (float) segments)) * MathUtils.degreesToRadians;
            float ox0 = fx.origin.x + MathUtils.sin(a0) * rOuter;
            float oz0 = fx.origin.z + MathUtils.cos(a0) * rOuter;
            float ox1 = fx.origin.x + MathUtils.sin(a1) * rOuter;
            float oz1 = fx.origin.z + MathUtils.cos(a1) * rOuter;
            float ix0 = fx.origin.x + MathUtils.sin(a0) * rInner;
            float iz0 = fx.origin.z + MathUtils.cos(a0) * rInner;
            float ix1 = fx.origin.x + MathUtils.sin(a1) * rInner;
            float iz1 = fx.origin.z + MathUtils.cos(a1) * rInner;

            sr.line(ox0, y, oz0, ox1, y, oz1);
            sr.line(ix0, y, iz0, ix1, y, iz1);
            if (i % 3 == 0) {
                sr.line(ix0, y, iz0, ox0, y, oz0);
            }
        }
    }

    private static void renderShockwave(ShapeRenderer sr, VisualEffect fx, float t) {
        float r = MathUtils.lerp(0.6f, fx.radius, t);
        WorldShapePrimitives.drawRing(sr, fx.origin.x, 0.12f, fx.origin.z, r, 32);
        WorldShapePrimitives.drawRing(sr, fx.origin.x, 0.28f, fx.origin.z, r * 0.72f, 24);
        for (int i = 0; i < 8; i++) {
            float a = (i * 45f + fx.facingDeg) * MathUtils.degreesToRadians;
            sr.line(
                fx.origin.x + MathUtils.sin(a) * (r * 0.3f), 0.10f, fx.origin.z + MathUtils.cos(a) * (r * 0.3f),
                fx.origin.x + MathUtils.sin(a) * r, 0.10f, fx.origin.z + MathUtils.cos(a) * r
            );
        }
    }

    private static void renderWhirlwind(ShapeRenderer sr, VisualEffect fx, float t) {
        float spin = t * 540f;
        for (int ring = 0; ring < 3; ring++) {
            float y = 0.55f + ring * 0.45f;
            float r = fx.radius * (0.78f + ring * 0.10f);
            WorldShapePrimitives.drawArc(sr, fx.origin.x, y, fx.origin.z, r, spin + ring * 120f, 210f, 18);
        }
    }

    private static void renderWarCry(ShapeRenderer sr, VisualEffect fx, float t) {
        float r = MathUtils.lerp(0.8f, fx.radius, t);
        WorldShapePrimitives.drawRing(sr, fx.origin.x, 0.25f + t * 1.4f, fx.origin.z, r, 32);
        WorldShapePrimitives.drawRing(sr, fx.origin.x, 0.15f, fx.origin.z, r * 0.85f, 28);
    }

    private static void renderLeapImpact(ShapeRenderer sr, VisualEffect fx, float t) {
        float r = MathUtils.lerp(0.55f, fx.radius, t);
        WorldShapePrimitives.drawRing(sr, fx.origin.x, 0.14f, fx.origin.z, r, 32);
        WorldShapePrimitives.drawRing(sr, fx.origin.x, 0.32f * (1f - t) + 0.08f, fx.origin.z, r * 0.65f, 24);
        for (int i = 0; i < 6; i++) {
            float a = (i * 60f + fx.facingDeg) * MathUtils.degreesToRadians;
            float sin = MathUtils.sin(a);
            float cos = MathUtils.cos(a);
            sr.line(
                fx.origin.x + sin * (r * 0.45f), 0.10f, fx.origin.z + cos * (r * 0.45f),
                fx.origin.x + sin * r, 0.65f * (1f - t) + 0.12f, fx.origin.z + cos * r
            );
        }
    }

    private static void renderLevelUp(ShapeRenderer sr, VisualEffect fx, float t) {
        for (int i = 0; i < 4; i++) {
            float y = (t * 3.5f + i * 0.7f) % 3.2f;
            float r = MathUtils.lerp(1.4f, 0.45f, y / 3.2f);
            WorldShapePrimitives.drawRing(sr, fx.origin.x, y, fx.origin.z, r, 24);
        }
    }
}
