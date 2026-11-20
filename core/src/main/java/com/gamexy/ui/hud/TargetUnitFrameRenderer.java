package com.gamexy.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.gamexy.targeting.Targetable;
import com.gamexy.ui.UiFonts;

/**
 * Renders the top-center Gothic Target Unit Frame (name, subtitle, accent strip, and optional HP bar)
 * across the HUD's 3 batched render passes.
 */
public class TargetUnitFrameRenderer {

    private final Rectangle hpBarBounds = new Rectangle();
    private boolean hasHp = false;
    private Color accent = Color.WHITE;

    public void prepareFrame(HudLayout layout, Targetable selectedTarget) {
        boolean visible = (selectedTarget != null && selectedTarget.isTargetable());
        layout.setTargetFrameVisible(visible);
        if (!visible) {
            return;
        }
        float uiScale = layout.getUiScale();
        hasHp = selectedTarget.hasHealthBar();
        accent = selectedTarget.getTargetAccentColor();
        float tfW = Math.min(370f * uiScale, layout.getScreenW() * 0.88f);
        float tfH = (hasHp ? 82f : 62f) * uiScale;
        float tfX = (layout.getScreenW() - tfW) * 0.5f;
        float tfY = layout.getScreenH() - tfH - 14f * uiScale;

        layout.targetFrameBounds.set(tfX, tfY, tfW, tfH);
        hpBarBounds.set(tfX + 14f * uiScale, tfY + 11f * uiScale, tfW - 28f * uiScale, 21f * uiScale);
    }

    public void renderFilled(ShapeRenderer shapes, HudLayout layout, Targetable selectedTarget) {
        if (!layout.isTargetFrameVisible()) {
            return;
        }
        float uiScale = layout.getUiScale();
        Rectangle tf = layout.targetFrameBounds;

        float shadow = 4f * uiScale;
        shapes.setColor(0f, 0f, 0f, 0.55f);
        shapes.rect(tf.x + shadow, tf.y - shadow, tf.width, tf.height);

        shapes.setColor(0.07f, 0.06f, 0.085f, 0.94f);
        shapes.rect(tf.x, tf.y, tf.width, tf.height);

        float accentH = Math.max(2.5f, 4f * uiScale);
        shapes.setColor(accent.r, accent.g, accent.b, 0.95f);
        shapes.rect(tf.x, tf.y + tf.height - accentH, tf.width, accentH);

        if (hasHp) {
            float hpRatio = MathUtils.clamp(
                selectedTarget.getCurrentHp() / Math.max(1f, selectedTarget.getMaxHp()), 0f, 1f);
            shapes.setColor(0.14f, 0.08f, 0.09f, 1f);
            shapes.rect(hpBarBounds.x, hpBarBounds.y, hpBarBounds.width, hpBarBounds.height);
            if (hpRatio > 0.001f) {
                shapes.setColor(accent.r * 0.85f, accent.g * 0.85f, accent.b * 0.85f, 1f);
                shapes.rect(hpBarBounds.x, hpBarBounds.y, hpBarBounds.width * hpRatio, hpBarBounds.height);
                float glossH = Math.max(1.5f, 3.5f * uiScale);
                shapes.setColor(1f, 1f, 1f, 0.22f);
                shapes.rect(hpBarBounds.x, hpBarBounds.y + hpBarBounds.height - glossH,
                    hpBarBounds.width * hpRatio, glossH);
            }
        }
    }

    public void renderBorders(ShapeRenderer shapes, HudLayout layout) {
        if (!layout.isTargetFrameVisible()) {
            return;
        }
        float uiScale = layout.getUiScale();
        Rectangle tf = layout.targetFrameBounds;
        float inset = Math.max(1.5f, 3f * uiScale);

        shapes.setColor(accent.r, accent.g, accent.b, 0.92f);
        shapes.rect(tf.x, tf.y, tf.width, tf.height);
        shapes.setColor(0.42f, 0.32f, 0.18f, 0.85f);
        shapes.rect(tf.x + inset, tf.y + inset, tf.width - inset * 2f, tf.height - inset * 2f);

        float corner = 6f * uiScale;
        shapes.setColor(0.92f, 0.78f, 0.38f, 0.95f);
        shapes.rect(tf.x - 1f, tf.y - 1f, corner, corner);
        shapes.rect(tf.x + tf.width - corner + 1f, tf.y - 1f, corner, corner);
        shapes.rect(tf.x - 1f, tf.y + tf.height - corner + 1f, corner, corner);
        shapes.rect(tf.x + tf.width - corner + 1f, tf.y + tf.height - corner + 1f, corner, corner);

        if (hasHp) {
            shapes.setColor(0.60f, 0.48f, 0.26f, 0.90f);
            shapes.rect(hpBarBounds.x, hpBarBounds.y, hpBarBounds.width, hpBarBounds.height);
        }
    }

    public void renderText(SpriteBatch batch, UiFonts fonts, GlyphLayout glyphLayout,
                           HudLayout layout, Targetable selectedTarget,
                           float previewRatio, float restoreHudTextScale) {
        if (!layout.isTargetFrameVisible()) {
            return;
        }
        float uiScale = layout.getUiScale();
        Rectangle tf = layout.targetFrameBounds;
        float tfFit = Math.min(1.0f, tf.width / Math.max(1f, 370f * uiScale));
        float tfTextScale = Math.max(0.05f, previewRatio * tfFit);

        fonts.headerSerif.getData().setScale(tfTextScale);
        fonts.hudSmallSans.getData().setScale(tfTextScale);
        try {
            float centerX = tf.x + tf.width * 0.5f;
            float nameY = tf.y + tf.height - (hasHp ? 19f : 21f) * uiScale;
            drawCentered(batch, fonts.headerSerif, glyphLayout,
                selectedTarget.getDisplayName().toUpperCase(), centerX, nameY, 1.0f, 0.94f, 0.80f, 1f);

            float subY = hasHp ? (tf.y + 45f * uiScale) : (tf.y + 20f * uiScale);
            drawCentered(batch, fonts.hudSmallSans, glyphLayout, selectedTarget.getSubtitle(), centerX, subY,
                Math.min(1f, accent.r + 0.18f),
                Math.min(1f, accent.g + 0.18f),
                Math.min(1f, accent.b + 0.18f),
                0.96f);

            if (hasHp) {
                String hpText = Math.round(selectedTarget.getCurrentHp()) + " / " + Math.round(selectedTarget.getMaxHp());
                drawCentered(batch, fonts.hudSmallSans, glyphLayout, hpText,
                    centerX, hpBarBounds.y + hpBarBounds.height * 0.5f, 1f, 1f, 1f, 1f);
            }
        } finally {
            fonts.headerSerif.getData().setScale(restoreHudTextScale);
            fonts.hudSmallSans.getData().setScale(restoreHudTextScale);
        }
    }

    private static void drawCentered(SpriteBatch batch, BitmapFont font, GlyphLayout layout,
                                     String text, float cx, float cy, float r, float g, float b, float a) {
        font.setColor(r, g, b, a);
        layout.setText(font, text);
        font.draw(batch, text, cx - layout.width * 0.5f, cy + layout.height * 0.5f);
    }
}
