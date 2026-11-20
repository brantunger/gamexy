package com.gamexy.targeting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.render.WorldShapePrimitives;

/**
 * Renders 3D ground rings and cardinal selection notches beneath hovered and selected targets.
 */
public class TargetRingRenderer {

    private float animTime = 0f;

    public void update(float delta) {
        animTime += delta;
    }

    /**
     * Renders ground rings. Caller must have begun {@code sr} in Line mode with the world projection matrix.
     */
    public void render(ShapeRenderer sr, Targetable hoveredTarget, Targetable selectedTarget) {
        if (hoveredTarget != null && hoveredTarget != selectedTarget && hoveredTarget.isTargetable()) {
            renderHoverRing(sr, hoveredTarget);
        }
        if (selectedTarget != null && selectedTarget.isTargetable()) {
            renderSelectedRing(sr, selectedTarget);
        }
    }

    private void renderHoverRing(ShapeRenderer sr, Targetable target) {
        Color c = target.getTargetAccentColor();
        Vector3 p = target.getWorldPosition();
        sr.setColor(c.r, c.g, c.b, 0.44f);
        WorldShapePrimitives.drawRing(sr, p.x, 0.06f, p.z, target.getSelectionRadius(), 32);
    }

    private void renderSelectedRing(ShapeRenderer sr, Targetable target) {
        Color c = target.getTargetAccentColor();
        Vector3 p = target.getWorldPosition();
        float pulse = MathUtils.sin(animTime * 4.5f) * 0.04f;
        float rInner = target.getSelectionRadius() + pulse;
        float rOuter = rInner + 0.09f;
        float y = 0.075f;

        sr.setColor(c.r, c.g, c.b, 0.96f);
        WorldShapePrimitives.drawRing(sr, p.x, y, p.z, rInner, 36);
        sr.setColor(c.r, c.g, c.b, 0.68f);
        WorldShapePrimitives.drawRing(sr, p.x, y, p.z, rOuter, 36);

        renderSelectionNotches(sr, target, c, p, rInner, rOuter, y);
    }

    private void renderSelectionNotches(ShapeRenderer sr, Targetable target, Color c,
                                        Vector3 p, float rInner, float rOuter, float y) {
        float rotDeg = (target.getTargetCategory() == Targetable.Category.WORLD_OBJECT)
            ? 45f
            : (animTime * 36f) % 360f;
        float notchOuter = rOuter + 0.22f;
        float notchInner = rInner - 0.14f;

        sr.setColor(Math.min(1f, c.r + 0.18f), Math.min(1f, c.g + 0.18f), Math.min(1f, c.b + 0.18f), 0.95f);
        for (int i = 0; i < 4; i++) {
            float a = (rotDeg + i * 90f) * MathUtils.degreesToRadians;
            float sin = MathUtils.sin(a);
            float cos = MathUtils.cos(a);
            sr.line(
                p.x + sin * notchInner, y + 0.01f, p.z + cos * notchInner,
                p.x + sin * notchOuter, y + 0.01f, p.z + cos * notchOuter
            );
        }
    }
}
