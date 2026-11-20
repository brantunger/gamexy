package com.gamexy.ui.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.gamexy.settings.GameSettings;

/**
 * Shared Gothic RPG panel, button, slider, and centered-text drawing primitives
 * used by both {@link com.gamexy.screen.TitleScreen} and {@link com.gamexy.ui.PauseMenu}.
 */
public class GothicMenuPrimitives {

    private final GlyphLayout layout = new GlyphLayout();
    private final Color tmpColor = new Color();

    public static float computeFitScale(int screenW, int screenH, float userUiScale,
                                        float baseFitW, float baseFitH) {
        float desiredScale = GameSettings.baseResolutionScale(screenW, screenH) * userUiScale;
        float maxFitScale = Math.min((screenW * 0.94f) / baseFitW, (screenH * 0.92f) / baseFitH);
        return Math.max(0.35f, Math.min(desiredScale, maxFitScale));
    }

    public static boolean hitSliderTrack(Rectangle track, float uiScale, float x, float y) {
        float padX = 10f * uiScale;
        float padY = 14f * uiScale;
        return x >= track.x - padX
            && x <= track.x + track.width + padX
            && y >= track.y - padY
            && y <= track.y + track.height + padY;
    }

    public void drawPanelFill(ShapeRenderer shapes, Rectangle panelBounds, float uiScale, float chromeAlpha) {
        float shadowOff = 6f * uiScale;
        shapes.setColor(0.0f, 0.0f, 0.0f, 0.56f * chromeAlpha);
        shapes.rect(panelBounds.x + shadowOff, panelBounds.y - shadowOff, panelBounds.width, panelBounds.height);

        shapes.setColor(0.08f, 0.065f, 0.09f, 0.96f * chromeAlpha);
        shapes.rect(panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height);

        float headerBarH = 64f * uiScale;
        shapes.setColor(0.42f, 0.08f, 0.09f, 0.95f * chromeAlpha);
        shapes.rect(panelBounds.x + 2f, panelBounds.y + panelBounds.height - headerBarH - 2f,
            panelBounds.width - 4f, headerBarH);

        shapes.setColor(0.68f, 0.52f, 0.24f, 0.90f * chromeAlpha);
        shapes.rect(panelBounds.x + 18f * uiScale, panelBounds.y + panelBounds.height - headerBarH - 4f,
            panelBounds.width - 36f * uiScale, Math.max(1.5f, 2f * uiScale));
    }

    public void drawPanelBorders(ShapeRenderer shapes, Rectangle panelBounds, float uiScale, float chromeAlpha) {
        float innerInset = Math.max(2f, 4f * uiScale);
        shapes.setColor(0.66f, 0.52f, 0.28f, 0.98f * chromeAlpha);
        shapes.rect(panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height);
        shapes.setColor(0.38f, 0.28f, 0.16f, 0.90f * chromeAlpha);
        shapes.rect(panelBounds.x + innerInset, panelBounds.y + innerInset,
            panelBounds.width - innerInset * 2f, panelBounds.height - innerInset * 2f);

        float c = 9f * uiScale;
        shapes.setColor(0.88f, 0.72f, 0.34f, 0.95f * chromeAlpha);
        shapes.rect(panelBounds.x - 2f, panelBounds.y - 2f, c, c);
        shapes.rect(panelBounds.x + panelBounds.width - c + 2f, panelBounds.y - 2f, c, c);
        shapes.rect(panelBounds.x - 2f, panelBounds.y + panelBounds.height - c + 2f, c, c);
        shapes.rect(panelBounds.x + panelBounds.width - c + 2f, panelBounds.y + panelBounds.height - c + 2f, c, c);
    }

    public void drawButtonFill(ShapeRenderer shapes, Rectangle r, boolean danger,
                               float mouseUiX, float mouseUiY, float alphaMul) {
        boolean hovered = alphaMul > 0.5f && r.contains(mouseUiX, mouseUiY);
        if (danger) {
            if (hovered) {
                shapes.setColor(0.46f, 0.11f, 0.11f, 0.98f * alphaMul);
            } else {
                shapes.setColor(0.26f, 0.08f, 0.09f, 0.94f * alphaMul);
            }
        } else {
            if (hovered) {
                shapes.setColor(0.28f, 0.20f, 0.14f, 0.98f * alphaMul);
            } else {
                shapes.setColor(0.14f, 0.12f, 0.16f, 0.94f * alphaMul);
            }
        }
        shapes.rect(r.x, r.y, r.width, r.height);
    }

    public void drawButtonBorder(ShapeRenderer shapes, Rectangle r, boolean danger,
                                 float mouseUiX, float mouseUiY, float alphaMul) {
        boolean hovered = alphaMul > 0.5f && r.contains(mouseUiX, mouseUiY);
        if (hovered) {
            shapes.setColor(1.0f, 0.82f, 0.32f, alphaMul);
            shapes.rect(r.x - 1f, r.y - 1f, r.width + 2f, r.height + 2f);
        } else if (danger) {
            shapes.setColor(0.65f, 0.24f, 0.22f, 0.90f * alphaMul);
        } else {
            shapes.setColor(0.48f, 0.38f, 0.24f, 0.88f * alphaMul);
        }
        shapes.rect(r.x, r.y, r.width, r.height);
    }

    public void drawSliderFill(ShapeRenderer shapes, Rectangle track, float ratio, float uiScale, float alphaMul) {
        float clamped = MathUtils.clamp(ratio, 0f, 1f);
        shapes.setColor(0.16f, 0.14f, 0.20f, alphaMul);
        shapes.rect(track.x, track.y, track.width, track.height);

        shapes.setColor(0.76f, 0.52f, 0.18f, alphaMul);
        shapes.rect(track.x, track.y, track.width * clamped, track.height);

        float knobX = track.x + track.width * clamped;
        float knobY = track.y + track.height * 0.5f;
        shapes.setColor(0.98f, 0.84f, 0.34f, alphaMul);
        shapes.circle(knobX, knobY, 10f * uiScale, 24);
    }

    public void drawSliderBorder(ShapeRenderer shapes, Rectangle track, float alphaMul) {
        shapes.setColor(0.55f, 0.44f, 0.26f, 0.92f * alphaMul);
        shapes.rect(track.x, track.y, track.width, track.height);
    }

    public void drawButtonLabel(SpriteBatch batch, BitmapFont font, Rectangle r, String text,
                                float mouseUiX, float mouseUiY, float alphaMul) {
        boolean hovered = alphaMul > 0.5f && r.contains(mouseUiX, mouseUiY);
        if (hovered) {
            tmpColor.set(1.0f, 0.90f, 0.48f, alphaMul);
        } else {
            tmpColor.set(0.94f, 0.92f, 0.88f, alphaMul);
        }
        drawCentered(batch, font, text, r.x + r.width * 0.5f, r.y + r.height * 0.5f, tmpColor);
    }

    public void drawSmallButtonLabel(SpriteBatch batch, BitmapFont font, Rectangle r, String text,
                                     float mouseUiX, float mouseUiY, float alphaMul) {
        boolean hovered = r.contains(mouseUiX, mouseUiY);
        if (hovered) {
            tmpColor.set(1.0f, 0.90f, 0.48f, 1.0f);
        } else {
            tmpColor.set(0.94f, 0.92f, 0.88f, alphaMul);
        }
        drawCentered(batch, font, text, r.x + r.width * 0.5f, r.y + r.height * 0.5f, tmpColor);
    }

    public void drawCentered(SpriteBatch batch, BitmapFont targetFont, String text,
                             float cx, float cy, float r, float g, float b, float a) {
        drawCentered(batch, targetFont, text, cx, cy, tmpColor.set(r, g, b, a));
    }

    public void drawCentered(SpriteBatch batch, BitmapFont targetFont, String text,
                             float cx, float cy, Color color) {
        targetFont.setColor(color);
        layout.setText(targetFont, text);
        targetFont.draw(batch, text, cx - layout.width * 0.5f, cy + layout.height * 0.5f);
    }
}
