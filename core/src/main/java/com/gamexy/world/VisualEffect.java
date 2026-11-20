package com.gamexy.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Data model and lifetime state for short-lived 3D combat and level-up visual effects.
 */
public class VisualEffect {

    public enum Kind {
        CLEAVE_ARC,
        SHOCKWAVE,
        WHIRLWIND,
        WAR_CRY,
        LEAP_IMPACT,
        LEVEL_UP
    }

    public final Kind kind;
    public final Vector3 origin = new Vector3();
    public final float facingDeg;
    public final float radius;
    public final float arcDeg;
    public final Color color = new Color();
    public final float duration;
    private float elapsed = 0f;

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

    public float getProgress() {
        return MathUtils.clamp(elapsed / Math.max(1e-4f, duration), 0f, 1f);
    }
}
