package com.gamexy.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/**
 * Floating world-space combat text (damage numbers, crits, XP, level-up callouts).
 */
public class FloatingText {
    public final String text;
    public final Vector3 worldPos = new Vector3();
    public final Color color = new Color();
    public final float scale;
    public final float duration;
    public float elapsed = 0f;

    public FloatingText(String text, float x, float y, float z, Color color, float scale, float duration) {
        this.text = text;
        this.worldPos.set(x, y, z);
        this.color.set(color);
        this.scale = scale;
        this.duration = duration;
    }

    public boolean update(float delta) {
        elapsed += delta;
        worldPos.y += delta * 1.45f;
        return elapsed < duration;
    }

    public float getAlpha() {
        float t = elapsed / duration;
        return t > 0.6f ? Math.max(0f, 1f - (t - 0.6f) / 0.4f) : 1f;
    }
}
