package com.gamexy.ui.menu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.gamexy.ui.UiFonts;

/**
 * Shared Controls Reference Gothic panel view displaying all key and mouse bindings.
 */
public class ControlsPanelView implements GothicMenuView {

    public static final String[][] CONTROL_BINDINGS = {
        { "W / S",                    "Move Forward / Back" },
        { "A / D",                    "Turn Left / Right (Camera Follows)" },
        { "Hold RMB + LMB",           "Move Forward & Steer with Mouse" },
        { "Left-Click",               "Select Target (Click Selected Enemy to Attack)" },
        { "Tab / Shift+Tab",          "Cycle Nearby Enemies" },
        { "1 - 9, 0 / Click Bar",     "Select Skill (Chase & Attack Selected Enemy)" },
        { "Right-Click / Alt+Drag",   "Free-Orbit Camera (Overrides Follow)" },
        { "Mouse Scroll",             "Zoom Camera In / Out" },
        { "Esc / R",                  "Clear Target or Pause / Respawn" }
    };

    private static final float BASE_PANEL_W = 640f;
    private static final float BASE_PANEL_H = 492f;

    private final Runnable onBack;
    private final Rectangle panelBounds = new Rectangle();
    private final Rectangle backButton = new Rectangle();
    private float uiScale = 1.0f;

    public ControlsPanelView(Runnable onBack) {
        this.onBack = onBack;
    }

    @Override
    public void layout(int screenW, int screenH, float userUiScale) {
        this.uiScale = GothicMenuPrimitives.computeFitScale(
            screenW, screenH, userUiScale, BASE_PANEL_W, BASE_PANEL_H);

        float panelW = BASE_PANEL_W * uiScale;
        float panelH = BASE_PANEL_H * uiScale;
        float px = (screenW - panelW) * 0.5f;
        float py = (screenH - panelH) * 0.5f;
        panelBounds.set(px, py, panelW, panelH);

        float backW = 200f * uiScale;
        float backH = 44f * uiScale;
        backButton.set(px + (panelW - backW) * 0.5f, py + 28f * uiScale, backW, backH);
    }

    @Override
    public float getUiScale() {
        return uiScale;
    }

    @Override
    public boolean handleTouchDown(float mouseUiX, float mouseUiY, int button) {
        if (button == Input.Buttons.LEFT && backButton.contains(mouseUiX, mouseUiY)) {
            onBack.run();
        }
        return true;
    }

    @Override
    public void renderFilled(ShapeRenderer shapes, GothicMenuPrimitives prims, float mouseUiX, float mouseUiY) {
        prims.drawPanelFill(shapes, panelBounds, uiScale, 1.0f);

        float startY = panelBounds.y + panelBounds.height - 110f * uiScale;
        float rowH = 33f * uiScale;
        for (int i = 0; i < CONTROL_BINDINGS.length; i++) {
            if (i % 2 == 0) {
                shapes.setColor(0.12f, 0.10f, 0.14f, 0.75f);
                shapes.rect(panelBounds.x + 26f * uiScale, startY - i * rowH - 22f * uiScale,
                    panelBounds.width - 52f * uiScale, rowH - 2f);
            }
        }
        prims.drawButtonFill(shapes, backButton, false, mouseUiX, mouseUiY, 1.0f);
    }

    @Override
    public void renderBorders(ShapeRenderer shapes, GothicMenuPrimitives prims, float mouseUiX, float mouseUiY) {
        prims.drawPanelBorders(shapes, panelBounds, uiScale, 1.0f);
        prims.drawButtonBorder(shapes, backButton, false, mouseUiX, mouseUiY, 1.0f);
    }

    @Override
    public void renderText(SpriteBatch batch, UiFonts fonts, GothicMenuPrimitives prims,
                           float mouseUiX, float mouseUiY) {
        float centerX = panelBounds.x + panelBounds.width * 0.5f;
        float titleCenterY = panelBounds.y + panelBounds.height - 34f * uiScale;
        prims.drawCentered(batch, fonts.titleSerif, "CONTROLS",
            centerX, titleCenterY, 1.0f, 0.86f, 0.42f, 1.0f);

        float startY = panelBounds.y + panelBounds.height - 108f * uiScale;
        float rowH = 33f * uiScale;
        float keyX = panelBounds.x + 36f * uiScale;
        float descX = panelBounds.x + 250f * uiScale;

        for (int i = 0; i < CONTROL_BINDINGS.length; i++) {
            float y = startY - i * rowH;
            fonts.hudSmallSans.setColor(1.0f, 0.82f, 0.36f, 1.0f);
            fonts.hudSmallSans.draw(batch, CONTROL_BINDINGS[i][0], keyX, y);
            fonts.hudSmallSans.setColor(0.92f, 0.92f, 0.96f, 1.0f);
            fonts.hudSmallSans.draw(batch, CONTROL_BINDINGS[i][1], descX, y);
        }

        prims.drawButtonLabel(batch, fonts.hudMediumSans, backButton, "Back (Esc)", mouseUiX, mouseUiY, 1.0f);
    }
}
