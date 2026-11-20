package com.gamexy.ui.menu;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.gamexy.settings.GameSettings;

/**
 * Animated gothic hearth glow and drifting ember particle backdrop for {@link com.gamexy.screen.TitleScreen}.
 */
public class TitleBackdrop {

    private static final class Ember {
        float nx;
        float ny;
        float speedY;
        float driftAmp;
        float driftFreq;
        float phase;
        float radius;
        float r, g, b;
    }

    private static final int EMBER_COUNT = 38;

    private final Ember[] embers = new Ember[EMBER_COUNT];
    private float elapsed = 0f;

    public TitleBackdrop() {
        for (int i = 0; i < EMBER_COUNT; i++) {
            embers[i] = createEmber();
        }
    }

    private static Ember createEmber() {
        Ember e = new Ember();
        e.nx = MathUtils.random(0.04f, 0.96f);
        e.ny = MathUtils.random(0.0f, 1.0f);
        e.speedY = MathUtils.random(0.022f, 0.068f);
        e.driftAmp = MathUtils.random(0.008f, 0.026f);
        e.driftFreq = MathUtils.random(0.6f, 1.6f);
        e.phase = MathUtils.random(0f, MathUtils.PI2);
        e.radius = MathUtils.random(1.4f, 3.2f);
        float warm = MathUtils.random();
        if (warm < 0.45f) {
            e.r = 1.0f; e.g = 0.72f; e.b = 0.24f;
        } else if (warm < 0.80f) {
            e.r = 0.95f; e.g = 0.42f; e.b = 0.14f;
        } else {
            e.r = 0.86f; e.g = 0.20f; e.b = 0.14f;
        }
        return e;
    }

    public void update(float dt) {
        elapsed += dt;
        for (Ember e : embers) {
            e.ny += e.speedY * dt;
            if (e.ny > 1.06f) {
                e.ny = -0.04f;
                e.nx = MathUtils.random(0.04f, 0.96f);
            }
        }
    }

    /**
     * Renders the backdrop glow and rising embers. Caller must have begun {@code shapes} in Filled mode.
     */
    public void render(ShapeRenderer shapes, int screenW, int screenH) {
        drawHearthGlow(shapes, screenW, screenH);
        drawEmbers(shapes, screenW, screenH);
    }

    private void drawHearthGlow(ShapeRenderer shapes, int screenW, int screenH) {
        float cx = screenW * 0.5f;
        float maxDim = Math.max(screenW, screenH);
        float pulse = 0.92f + 0.08f * MathUtils.sin(elapsed * 1.4f);

        shapes.setColor(0.28f, 0.05f, 0.07f, 0.14f * pulse);
        shapes.circle(cx, screenH * 0.18f, maxDim * 0.48f, 48);
        shapes.setColor(0.40f, 0.12f, 0.06f, 0.11f * pulse);
        shapes.circle(cx, screenH * 0.12f, maxDim * 0.30f, 40);
        shapes.setColor(0.46f, 0.24f, 0.08f, 0.08f * pulse);
        shapes.circle(cx, screenH * 0.52f, maxDim * 0.26f, 40);
    }

    private void drawEmbers(ShapeRenderer shapes, int screenW, int screenH) {
        float baseScale = GameSettings.baseResolutionScale(screenW, screenH);
        for (Ember e : embers) {
            float ex = (e.nx + MathUtils.sin(elapsed * e.driftFreq + e.phase) * e.driftAmp) * screenW;
            float ey = e.ny * screenH;
            float fadeIn = MathUtils.clamp(e.ny * 5.0f, 0f, 1f);
            float fadeOut = MathUtils.clamp((1.0f - e.ny) * 2.2f, 0f, 1f);
            float flicker = 0.65f + 0.35f * MathUtils.sin(elapsed * 4.5f + e.phase);
            float alpha = 0.62f * fadeIn * fadeOut * flicker;
            float r = e.radius * baseScale;

            shapes.setColor(e.r, e.g, e.b, alpha * 0.35f);
            shapes.circle(ex, ey, r * 2.1f, 12);
            shapes.setColor(e.r, e.g, e.b, alpha);
            shapes.circle(ex, ey, r, 10);
        }
    }
}
