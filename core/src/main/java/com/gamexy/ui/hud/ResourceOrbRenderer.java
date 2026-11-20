package com.gamexy.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.gamexy.character.PlayerCharacter;
import com.gamexy.ui.UiFonts;

/**
 * Renders the flanking Diablo-style liquid Health (left) and Fury (right) orbs across 3 HUD passes.
 */
public class ResourceOrbRenderer {

    private static final Color HP_FILL = new Color(0.78f, 0.10f, 0.12f, 1f);
    private static final Color HP_RIM = new Color(0.95f, 0.28f, 0.24f, 0.75f);
    private static final Color HP_BG = new Color(0.18f, 0.04f, 0.05f, 0.96f);
    private static final Color HP_LABEL = new Color(1f, 0.88f, 0.88f, 1f);

    private static final Color FURY_FILL = new Color(0.90f, 0.44f, 0.07f, 1f);
    private static final Color FURY_RIM = new Color(1.0f, 0.72f, 0.22f, 0.80f);
    private static final Color FURY_BG = new Color(0.18f, 0.10f, 0.03f, 0.96f);
    private static final Color FURY_LABEL = new Color(1f, 0.92f, 0.80f, 1f);

    public void renderFilled(ShapeRenderer shapes, HudLayout layout, PlayerCharacter player) {
        drawOrbFilled(shapes, layout.getLeftOrbX(), layout.getOrbY(), layout.getOrbRadius(),
            player.getHp() / Math.max(1f, player.getMaxHp()), HP_FILL, HP_RIM, HP_BG);
        drawOrbFilled(shapes, layout.getRightOrbX(), layout.getOrbY(), layout.getOrbRadius(),
            player.getFury() / Math.max(1f, player.getMaxFury()), FURY_FILL, FURY_RIM, FURY_BG);
    }

    public void renderBorders(ShapeRenderer shapes, HudLayout layout) {
        float barScale = layout.getBarScale();
        float orbRadius = layout.getOrbRadius();
        float leftX = layout.getLeftOrbX();
        float rightX = layout.getRightOrbX();
        float orbY = layout.getOrbY();

        float ringMid = Math.max(1f, 2f * barScale);
        float ringOuter = Math.max(2f, 4f * barScale);

        shapes.setColor(0.76f, 0.60f, 0.30f, 1.0f);
        shapes.circle(leftX, orbY, orbRadius, 56);
        shapes.circle(leftX, orbY, orbRadius + ringMid, 56);
        shapes.circle(rightX, orbY, orbRadius, 56);
        shapes.circle(rightX, orbY, orbRadius + ringMid, 56);

        shapes.setColor(0.32f, 0.24f, 0.14f, 0.95f);
        shapes.circle(leftX, orbY, orbRadius + ringOuter, 56);
        shapes.circle(rightX, orbY, orbRadius + ringOuter, 56);
    }

    public void renderText(SpriteBatch batch, UiFonts fonts, GlyphLayout glyphLayout,
                           HudLayout layout, PlayerCharacter player) {
        float orbY = layout.getOrbY();
        float r = layout.getOrbRadius();

        drawCentered(batch, fonts.headerSerif, glyphLayout, "HEALTH", layout.getLeftOrbX(), orbY + r * 0.28f, HP_LABEL);
        drawCentered(batch, fonts.orbValueSans, glyphLayout,
            Math.round(player.getHp()) + " / " + Math.round(player.getMaxHp()),
            layout.getLeftOrbX(), orbY - r * 0.16f, Color.WHITE);

        drawCentered(batch, fonts.headerSerif, glyphLayout, "FURY", layout.getRightOrbX(), orbY + r * 0.28f, FURY_LABEL);
        drawCentered(batch, fonts.orbValueSans, glyphLayout,
            Math.round(player.getFury()) + " / " + Math.round(player.getMaxFury()),
            layout.getRightOrbX(), orbY - r * 0.16f, Color.WHITE);
    }

    private static void drawOrbFilled(ShapeRenderer shapes, float cx, float cy, float radius, float ratio,
                                      Color fill, Color rimHighlight, Color bg) {
        ratio = MathUtils.clamp(ratio, 0f, 1f);
        shapes.setColor(0.05f, 0.04f, 0.06f, 0.95f);
        shapes.circle(cx, cy, radius + 4f, 56);

        shapes.setColor(bg);
        shapes.circle(cx, cy, radius, 56);

        if (ratio > 0.001f) {
            drawLiquidSlices(shapes, cx, cy, radius, ratio, fill, rimHighlight);
        }

        shapes.setColor(1f, 1f, 1f, 0.18f);
        shapes.circle(cx - radius * 0.30f, cy + radius * 0.32f, radius * 0.24f, 20);
        shapes.setColor(1f, 1f, 1f, 0.10f);
        shapes.circle(cx - radius * 0.38f, cy + radius * 0.40f, radius * 0.10f, 14);
    }

    private static void drawLiquidSlices(ShapeRenderer shapes, float cx, float cy, float radius,
                                         float ratio, Color fill, Color rimHighlight) {
        shapes.setColor(fill);
        int slices = 40;
        float maxY = cy - radius + radius * 2f * ratio;
        float step = (radius * 2f) / slices;
        for (int i = 0; i < slices; i++) {
            float y0 = cy - radius + i * step;
            if (y0 >= maxY) {
                break;
            }
            float y1 = Math.min(maxY, y0 + step);
            float dy = (y0 + y1) * 0.5f - cy;
            float halfW = (float) Math.sqrt(Math.max(0f, radius * radius - dy * dy));
            shapes.rect(cx - halfW, y0, halfW * 2f, y1 - y0 + 0.5f);
        }
        if (ratio < 0.97f && ratio > 0.05f) {
            float dy = maxY - cy;
            float halfW = (float) Math.sqrt(Math.max(0f, radius * radius - dy * dy));
            shapes.setColor(rimHighlight);
            shapes.rect(cx - halfW, maxY - 3f, halfW * 2f, 3.5f);
        }
    }

    private static void drawCentered(SpriteBatch batch, BitmapFont font, GlyphLayout layout,
                                     String text, float cx, float cy, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, text, cx - layout.width * 0.5f, cy + layout.height * 0.5f);
    }
}
