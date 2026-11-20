package com.gamexy.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Short-lived 3D ground/air combat visual effects (Cleave arc, Ground Slam shockwave,
 * Whirlwind vortex, War Cry aura, Level-Up golden pillar).
 */
public class VisualEffect {

    public enum Kind {
        CLEAVE_ARC,
        SHOCKWAVE,
        WHIRLWIND,
        WAR_CRY,
        LEVEL_UP
    }

    public final Kind kind;
    public final Vector3 origin = new Vector3();
    public final float facingDeg;
    public final float radius;
    public final float arcDeg;
    public final Color color = new Color();
    public final float duration;
    public float elapsed = 0f;

    public VisualEffect(Kind kind, Vector3 origin, float facingDeg,
                        float radius, float arcDeg, Color color, float duration) {
        this.kind = kind;
        this.origin.set(origin);
        this.facingDeg = facingDeg;
        this.radius = radius;
        this.arcDeg = arcDeg;
        this.color.set(color);
        this.duration = duration;
    }

    public boolean update(float delta) {
        elapsed += delta;
        return elapsed < duration;
    }

    public void render(ShapeRenderer sr) {
        float t = MathUtils.clamp(elapsed / duration, 0f, 1f);
        float alpha = 1f - t;
        sr.setColor(color.r, color.g, color.b, alpha);

        switch (kind) {
            case CLEAVE_ARC: {
                float y = 1.05f;
                int segments = 18;
                float startDeg = facingDeg - arcDeg * 0.5f;
                float sweep = arcDeg * Math.min(1f, t * 1.8f);
                float rOuter = radius;
                float rInner = radius * 0.45f;
                for (int i = 0; i < segments; i++) {
                    float a0 = (startDeg + sweep * (i / (float) segments)) * MathUtils.degreesToRadians;
                    float a1 = (startDeg + sweep * ((i + 1) / (float) segments)) * MathUtils.degreesToRadians;
                    float ox0 = origin.x + MathUtils.sin(a0) * rOuter;
                    float oz0 = origin.z + MathUtils.cos(a0) * rOuter;
                    float ox1 = origin.x + MathUtils.sin(a1) * rOuter;
                    float oz1 = origin.z + MathUtils.cos(a1) * rOuter;
                    float ix0 = origin.x + MathUtils.sin(a0) * rInner;
                    float iz0 = origin.z + MathUtils.cos(a0) * rInner;
                    float ix1 = origin.x + MathUtils.sin(a1) * rInner;
                    float iz1 = origin.z + MathUtils.cos(a1) * rInner;
                    sr.line(ox0, y, oz0, ox1, y, oz1);
                    sr.line(ix0, y, iz0, ix1, y, iz1);
                    if (i % 3 == 0) {
                        sr.line(ix0, y, iz0, ox0, y, oz0);
                    }
                }
                break;
            }

            case SHOCKWAVE: {
                float r = MathUtils.lerp(0.6f, radius, t);
                drawRing(sr, origin.x, 0.12f, origin.z, r, 32);
                drawRing(sr, origin.x, 0.28f, origin.z, r * 0.72f, 24);
                // Radial fracture lines
                for (int i = 0; i < 8; i++) {
                    float a = (i * 45f + facingDeg) * MathUtils.degreesToRadians;
                    sr.line(
                        origin.x + MathUtils.sin(a) * (r * 0.3f), 0.10f, origin.z + MathUtils.cos(a) * (r * 0.3f),
                        origin.x + MathUtils.sin(a) * r, 0.10f, origin.z + MathUtils.cos(a) * r
                    );
                }
                break;
            }

            case WHIRLWIND: {
                float spin = t * 540f;
                for (int ring = 0; ring < 3; ring++) {
                    float y = 0.55f + ring * 0.45f;
                    float r = radius * (0.78f + ring * 0.10f);
                    drawArc(sr, origin.x, y, origin.z, r, spin + ring * 120f, 210f, 18);
                }
                break;
            }

            case WAR_CRY: {
                float r = MathUtils.lerp(0.8f, radius, t);
                drawRing(sr, origin.x, 0.25f + t * 1.4f, origin.z, r, 32);
                drawRing(sr, origin.x, 0.15f, origin.z, r * 0.85f, 28);
                break;
            }

            case LEVEL_UP: {
                for (int i = 0; i < 4; i++) {
                    float y = ((t * 3.5f + i * 0.7f) % 3.2f);
                    float r = MathUtils.lerp(1.4f, 0.45f, y / 3.2f);
                    drawRing(sr, origin.x, y, origin.z, r, 24);
                }
                break;
            }
        }
    }

    public static void drawRing(ShapeRenderer sr, float cx, float y, float cz, float r, int segments) {
        for (int i = 0; i < segments; i++) {
            float a0 = (MathUtils.PI2 * i) / segments;
            float a1 = (MathUtils.PI2 * (i + 1)) / segments;
            sr.line(
                cx + MathUtils.sin(a0) * r, y, cz + MathUtils.cos(a0) * r,
                cx + MathUtils.sin(a1) * r, y, cz + MathUtils.cos(a1) * r
            );
        }
    }

    private static void drawArc(ShapeRenderer sr, float cx, float y, float cz,
                                float r, float startDeg, float sweepDeg, int segments) {
        for (int i = 0; i < segments; i++) {
            float a0 = (startDeg + sweepDeg * (i / (float) segments)) * MathUtils.degreesToRadians;
            float a1 = (startDeg + sweepDeg * ((i + 1) / (float) segments)) * MathUtils.degreesToRadians;
            sr.line(
                cx + MathUtils.sin(a0) * r, y, cz + MathUtils.cos(a0) * r,
                cx + MathUtils.sin(a1) * r, y, cz + MathUtils.cos(a1) * r
            );
        }
    }
}
