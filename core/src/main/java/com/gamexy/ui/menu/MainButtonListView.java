package com.gamexy.ui.menu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.gamexy.ui.UiFonts;

import java.util.List;

/**
 * Reusable Gothic main-menu panel with a crimson header, subtitle, optional top banner,
 * and a vertical stack of interactive buttons.
 */
public class MainButtonListView implements GothicMenuView {

    public static final class ButtonEntry {
        public final String label;
        public final boolean danger;
        public final Runnable action;

        public ButtonEntry(String label, boolean danger, Runnable action) {
            this.label = label;
            this.danger = danger;
            this.action = action;
        }
    }

    private static final float BASE_PANEL_W = 420f;
    private static final float BANNER_OVERHEAD = 120f;

    private final String headerTitle;
    private final String subtitle;
    private final String bannerTitle;
    private final String bannerSubtitle;
    private final List<ButtonEntry> entries;
    private final Rectangle panelBounds = new Rectangle();
    private final Rectangle[] buttonBounds;

    private float uiScale = 1.0f;

    public MainButtonListView(String headerTitle, String subtitle, List<ButtonEntry> entries) {
        this(headerTitle, subtitle, null, null, entries);
    }

    public MainButtonListView(String headerTitle, String subtitle,
                              String bannerTitle, String bannerSubtitle,
                              List<ButtonEntry> entries) {
        this.headerTitle = headerTitle;
        this.subtitle = subtitle;
        this.bannerTitle = bannerTitle;
        this.bannerSubtitle = bannerSubtitle;
        this.entries = entries;
        this.buttonBounds = new Rectangle[entries.size()];
        for (int i = 0; i < buttonBounds.length; i++) {
            buttonBounds[i] = new Rectangle();
        }
    }

    @Override
    public void layout(int screenW, int screenH, float userUiScale) {
        boolean hasBanner = bannerTitle != null;
        float basePanelH = 146f + Math.max(0, entries.size() - 1) * 58f + 40f;
        float overhead = hasBanner ? BANNER_OVERHEAD : 0f;
        float baseFitW = hasBanner ? 480f : BASE_PANEL_W;
        float baseFitH = basePanelH + overhead;

        this.uiScale = GothicMenuPrimitives.computeFitScale(screenW, screenH, userUiScale, baseFitW, baseFitH);

        float panelW = BASE_PANEL_W * uiScale;
        float panelH = basePanelH * uiScale;
        float totalH = (basePanelH + overhead) * uiScale;
        float px = (screenW - panelW) * 0.5f;
        float py = (screenH - totalH) * 0.5f;
        panelBounds.set(px, py, panelW, panelH);

        float btnW = panelW - 76f * uiScale;
        float btnH = 46f * uiScale;
        float btnGap = 12f * uiScale;
        float topBtnY = py + panelH - 146f * uiScale;
        for (int i = 0; i < buttonBounds.length; i++) {
            buttonBounds[i].set(px + 38f * uiScale, topBtnY - i * (btnH + btnGap), btnW, btnH);
        }
    }

    @Override
    public float getUiScale() {
        return uiScale;
    }

    @Override
    public boolean handleTouchDown(float mouseUiX, float mouseUiY, int button) {
        if (button != Input.Buttons.LEFT) {
            return true;
        }
        for (int i = 0; i < buttonBounds.length; i++) {
            if (buttonBounds[i].contains(mouseUiX, mouseUiY)) {
                entries.get(i).action.run();
                return true;
            }
        }
        return true;
    }

    @Override
    public void renderFilled(ShapeRenderer shapes, GothicMenuPrimitives prims, float mouseUiX, float mouseUiY) {
        prims.drawPanelFill(shapes, panelBounds, uiScale, 1.0f);

        if (bannerTitle != null) {
            drawBannerRule(shapes);
        }

        for (int i = 0; i < buttonBounds.length; i++) {
            prims.drawButtonFill(shapes, buttonBounds[i], entries.get(i).danger, mouseUiX, mouseUiY, 1.0f);
        }
    }

    private void drawBannerRule(ShapeRenderer shapes) {
        float centerX = panelBounds.x + panelBounds.width * 0.5f;
        float ruleY = panelBounds.y + panelBounds.height + 50f * uiScale;
        float ruleHalfW = 165f * uiScale;
        shapes.setColor(0.68f, 0.52f, 0.24f, 0.78f);
        shapes.rect(centerX - ruleHalfW, ruleY, ruleHalfW * 2f, Math.max(1.5f, 2f * uiScale));
        shapes.setColor(0.48f, 0.10f, 0.11f, 0.85f);
        shapes.rect(centerX - ruleHalfW * 0.55f, ruleY - 3f * uiScale, ruleHalfW * 1.1f, Math.max(1.5f, 2f * uiScale));
    }

    @Override
    public void renderBorders(ShapeRenderer shapes, GothicMenuPrimitives prims, float mouseUiX, float mouseUiY) {
        prims.drawPanelBorders(shapes, panelBounds, uiScale, 1.0f);
        for (int i = 0; i < buttonBounds.length; i++) {
            prims.drawButtonBorder(shapes, buttonBounds[i], entries.get(i).danger, mouseUiX, mouseUiY, 1.0f);
        }
    }

    @Override
    public void renderText(SpriteBatch batch, UiFonts fonts, GothicMenuPrimitives prims,
                           float mouseUiX, float mouseUiY) {
        float centerX = panelBounds.x + panelBounds.width * 0.5f;
        float titleCenterY = panelBounds.y + panelBounds.height - 34f * uiScale;

        if (bannerTitle != null) {
            float panelTop = panelBounds.y + panelBounds.height;
            prims.drawCentered(batch, fonts.bannerSerif, bannerTitle,
                centerX, panelTop + 86f * uiScale, 1.0f, 0.86f, 0.42f, 1.0f);
            if (bannerSubtitle != null) {
                prims.drawCentered(batch, fonts.headerSerif, bannerSubtitle,
                    centerX, panelTop + 26f * uiScale, 0.84f, 0.72f, 0.50f, 0.95f);
            }
        }

        prims.drawCentered(batch, fonts.titleSerif, headerTitle,
            centerX, titleCenterY, 1.0f, 0.86f, 0.42f, 1.0f);

        if (subtitle != null) {
            prims.drawCentered(batch, fonts.hudSmallSans, subtitle,
                centerX, panelBounds.y + panelBounds.height - 86f * uiScale,
                0.78f, 0.75f, 0.82f, 0.94f);
        }

        for (int i = 0; i < buttonBounds.length; i++) {
            prims.drawButtonLabel(batch, fonts.hudMediumSans, buttonBounds[i],
                entries.get(i).label, mouseUiX, mouseUiY, 1.0f);
        }
    }
}
