package com.gamexy.ui.menu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.gamexy.settings.GameSettings;
import com.gamexy.ui.UiFonts;

/**
 * Shared Settings Gothic panel view managing HUD/UI Scale (with optional live ghosted HUD preview),
 * Camera Orbit Sensitivity, Fullscreen/Windowed Display Mode, and VSync toggles.
 */
public class SettingsPanelView implements GothicMenuView {

    private enum ActiveSlider {
        NONE,
        UI_SCALE,
        SENSITIVITY
    }

    private static final float BASE_PANEL_W = 560f;
    private static final float BASE_PANEL_H = 500f;

    private final MenuSettingsAdapter settings;
    private final Runnable onBack;
    private final boolean ghostPreviewEnabled;

    private final Rectangle panelBounds = new Rectangle();
    private final Rectangle backButton = new Rectangle();
    private final Rectangle uiScaleRowHighlight = new Rectangle();
    private final Rectangle uiScaleMinusBtn = new Rectangle();
    private final Rectangle uiScaleSliderTrack = new Rectangle();
    private final Rectangle uiScalePlusBtn = new Rectangle();
    private final Rectangle uiScaleResetBtn = new Rectangle();
    private final Rectangle sensitivityMinusBtn = new Rectangle();
    private final Rectangle sensitivitySliderTrack = new Rectangle();
    private final Rectangle sensitivityPlusBtn = new Rectangle();
    private final Rectangle fullscreenToggleBtn = new Rectangle();
    private final Rectangle vsyncToggleBtn = new Rectangle();

    private ActiveSlider activeSlider = ActiveSlider.NONE;
    private float previewUiScale;
    private float uiScale = 1.0f;

    public SettingsPanelView(MenuSettingsAdapter settings, Runnable onBack, boolean ghostPreviewEnabled) {
        this.settings = settings;
        this.onBack = onBack;
        this.ghostPreviewEnabled = ghostPreviewEnabled;
        this.previewUiScale = settings.getUiScale();
    }

    public boolean isDraggingUiScale() {
        return activeSlider == ActiveSlider.UI_SCALE;
    }

    @Override
    public void onEnter() {
        activeSlider = ActiveSlider.NONE;
        previewUiScale = settings.getUiScale();
    }

    @Override
    public void onCancel() {
        if (activeSlider == ActiveSlider.UI_SCALE) {
            activeSlider = ActiveSlider.NONE;
            previewUiScale = settings.getUiScale();
            settings.onUiScalePreview(previewUiScale);
        } else {
            activeSlider = ActiveSlider.NONE;
        }
    }

    @Override
    public float getBackdropAlpha() {
        return (ghostPreviewEnabled && activeSlider == ActiveSlider.UI_SCALE) ? 0.16f : 0.72f;
    }

    @Override
    public float getUiScale() {
        return uiScale;
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

        float marginX = 44f * uiScale;
        float contentW = panelW - marginX * 2f;
        float stepBtnSize = 34f * uiScale;
        float sliderH = 14f * uiScale;

        layoutUiScaleRow(px, py, panelW, panelH, marginX, contentW, stepBtnSize, sliderH);
        layoutSensitivityRow(px, py, panelH, marginX, contentW, stepBtnSize, sliderH);

        float toggleH = 44f * uiScale;
        fullscreenToggleBtn.set(px + marginX, py + panelH - 314f * uiScale, contentW, toggleH);
        vsyncToggleBtn.set(px + marginX, py + panelH - 372f * uiScale, contentW, toggleH);
    }

    private void layoutUiScaleRow(float px, float py, float panelW, float panelH,
                                  float marginX, float contentW, float stepBtnSize, float sliderH) {
        float rowGapX = 10f * uiScale;
        float scaleTrackY = py + panelH - 140f * uiScale;
        float resetBtnW = 60f * uiScale;
        float scaleTrackW = contentW - stepBtnSize * 2f - resetBtnW - rowGapX * 3f;
        float scaleMinusX = px + marginX;
        float scaleTrackX = scaleMinusX + stepBtnSize + rowGapX;
        float scalePlusX = scaleTrackX + scaleTrackW + rowGapX;
        float scaleResetX = scalePlusX + stepBtnSize + rowGapX;

        uiScaleMinusBtn.set(scaleMinusX, scaleTrackY - 10f * uiScale, stepBtnSize, stepBtnSize);
        uiScaleSliderTrack.set(scaleTrackX, scaleTrackY, scaleTrackW, sliderH);
        uiScalePlusBtn.set(scalePlusX, scaleTrackY - 10f * uiScale, stepBtnSize, stepBtnSize);
        uiScaleResetBtn.set(scaleResetX, scaleTrackY - 10f * uiScale, resetBtnW, stepBtnSize);
        uiScaleRowHighlight.set(
            px + 22f * uiScale,
            scaleTrackY - 22f * uiScale,
            panelW - 44f * uiScale,
            82f * uiScale);
    }

    private void layoutSensitivityRow(float px, float py, float panelH,
                                      float marginX, float contentW, float stepBtnSize, float sliderH) {
        float sensTrackY = py + panelH - 234f * uiScale;
        float sensGapX = 12f * uiScale;
        float sensTrackW = contentW - stepBtnSize * 2f - sensGapX * 2f;
        float sensMinusX = px + marginX;
        float sensTrackX = sensMinusX + stepBtnSize + sensGapX;
        float sensPlusX = sensTrackX + sensTrackW + sensGapX;

        sensitivityMinusBtn.set(sensMinusX, sensTrackY - 10f * uiScale, stepBtnSize, stepBtnSize);
        sensitivitySliderTrack.set(sensTrackX, sensTrackY, sensTrackW, sliderH);
        sensitivityPlusBtn.set(sensPlusX, sensTrackY - 10f * uiScale, stepBtnSize, stepBtnSize);
    }

    @Override
    public boolean handleTouchDown(float mouseUiX, float mouseUiY, int button) {
        if (button != Input.Buttons.LEFT) {
            return true;
        }
        if (backButton.contains(mouseUiX, mouseUiY)) {
            onCancel();
            onBack.run();
            return true;
        }
        if (handleUiScaleClick(mouseUiX, mouseUiY)) {
            return true;
        }
        if (handleSensitivityClick(mouseUiX, mouseUiY)) {
            return true;
        }
        if (fullscreenToggleBtn.contains(mouseUiX, mouseUiY)) {
            settings.setFullscreen(!settings.isFullscreen());
            return true;
        }
        if (vsyncToggleBtn.contains(mouseUiX, mouseUiY)) {
            settings.setVsyncEnabled(!settings.isVsyncEnabled());
            return true;
        }
        return true;
    }

    private boolean handleUiScaleClick(float mouseUiX, float mouseUiY) {
        if (uiScaleMinusBtn.contains(mouseUiX, mouseUiY)) {
            commitScale(settings.getUiScale() - GameSettings.UI_SCALE_STEP);
            return true;
        }
        if (uiScalePlusBtn.contains(mouseUiX, mouseUiY)) {
            commitScale(settings.getUiScale() + GameSettings.UI_SCALE_STEP);
            return true;
        }
        if (uiScaleResetBtn.contains(mouseUiX, mouseUiY)) {
            commitScale(GameSettings.DEFAULT_UI_SCALE);
            return true;
        }
        if (GothicMenuPrimitives.hitSliderTrack(uiScaleSliderTrack, uiScale, mouseUiX, mouseUiY)) {
            activeSlider = ActiveSlider.UI_SCALE;
            updateUiScalePreviewFromX(mouseUiX);
            return true;
        }
        return false;
    }

    private boolean handleSensitivityClick(float mouseUiX, float mouseUiY) {
        if (sensitivityMinusBtn.contains(mouseUiX, mouseUiY)) {
            settings.setCameraSensitivity(settings.getCameraSensitivity() - 0.1f);
            return true;
        }
        if (sensitivityPlusBtn.contains(mouseUiX, mouseUiY)) {
            settings.setCameraSensitivity(settings.getCameraSensitivity() + 0.1f);
            return true;
        }
        if (GothicMenuPrimitives.hitSliderTrack(sensitivitySliderTrack, uiScale, mouseUiX, mouseUiY)) {
            activeSlider = ActiveSlider.SENSITIVITY;
            updateSensitivityFromX(mouseUiX);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleTouchDragged(float mouseUiX, float mouseUiY) {
        if (activeSlider == ActiveSlider.UI_SCALE) {
            updateUiScalePreviewFromX(mouseUiX);
        } else if (activeSlider == ActiveSlider.SENSITIVITY) {
            updateSensitivityFromX(mouseUiX);
        }
        return true;
    }

    @Override
    public boolean handleTouchUp(float mouseUiX, float mouseUiY, int button) {
        if (button == Input.Buttons.LEFT) {
            if (activeSlider == ActiveSlider.UI_SCALE) {
                activeSlider = ActiveSlider.NONE;
                settings.onUiScaleCommit(previewUiScale);
            } else {
                activeSlider = ActiveSlider.NONE;
            }
        }
        return true;
    }

    private void commitScale(float rawScale) {
        previewUiScale = GameSettings.snapUiScale(rawScale);
        settings.onUiScaleCommit(previewUiScale);
    }

    private void updateUiScalePreviewFromX(float x) {
        float ratio = MathUtils.clamp(
            (x - uiScaleSliderTrack.x) / Math.max(1f, uiScaleSliderTrack.width), 0f, 1f);
        float snapped = GameSettings.snapUiScale(
            MathUtils.lerp(GameSettings.MIN_UI_SCALE, GameSettings.MAX_UI_SCALE, ratio));
        if (Math.abs(snapped - previewUiScale) > 1e-4f) {
            previewUiScale = snapped;
            settings.onUiScalePreview(previewUiScale);
        }
    }

    private void updateSensitivityFromX(float x) {
        float ratio = MathUtils.clamp(
            (x - sensitivitySliderTrack.x) / Math.max(1f, sensitivitySliderTrack.width), 0f, 1f);
        float sens = MathUtils.lerp(
            GameSettings.MIN_CAMERA_SENSITIVITY, GameSettings.MAX_CAMERA_SENSITIVITY, ratio);
        settings.setCameraSensitivity(sens);
    }

    private boolean isGhostPreviewActive() {
        return ghostPreviewEnabled && activeSlider == ActiveSlider.UI_SCALE;
    }

    private float getDisplayedUiScale() {
        return (activeSlider == ActiveSlider.UI_SCALE) ? previewUiScale : settings.getUiScale();
    }

    @Override
    public void renderFilled(ShapeRenderer shapes, GothicMenuPrimitives prims, float mouseUiX, float mouseUiY) {
        boolean ghost = isGhostPreviewActive();
        float chromeAlpha = ghost ? 0.22f : 1.0f;

        prims.drawPanelFill(shapes, panelBounds, uiScale, chromeAlpha);

        if (ghost) {
            shapes.setColor(0.06f, 0.05f, 0.08f, 0.86f);
            shapes.rect(uiScaleRowHighlight.x, uiScaleRowHighlight.y,
                uiScaleRowHighlight.width, uiScaleRowHighlight.height);
        }

        // Row 1: HUD / UI Scale (always opaque)
        prims.drawButtonFill(shapes, uiScaleMinusBtn, false, mouseUiX, mouseUiY, 1.0f);
        prims.drawButtonFill(shapes, uiScalePlusBtn, false, mouseUiX, mouseUiY, 1.0f);
        prims.drawButtonFill(shapes, uiScaleResetBtn, false, mouseUiX, mouseUiY, 1.0f);
        float scaleRatio = (getDisplayedUiScale() - GameSettings.MIN_UI_SCALE)
            / (GameSettings.MAX_UI_SCALE - GameSettings.MIN_UI_SCALE);
        prims.drawSliderFill(shapes, uiScaleSliderTrack, scaleRatio, uiScale, 1.0f);

        // Row 2: Camera Orbit Sensitivity
        prims.drawButtonFill(shapes, sensitivityMinusBtn, false, mouseUiX, mouseUiY, chromeAlpha);
        prims.drawButtonFill(shapes, sensitivityPlusBtn, false, mouseUiX, mouseUiY, chromeAlpha);
        float sensRatio = (settings.getCameraSensitivity() - GameSettings.MIN_CAMERA_SENSITIVITY)
            / (GameSettings.MAX_CAMERA_SENSITIVITY - GameSettings.MIN_CAMERA_SENSITIVITY);
        prims.drawSliderFill(shapes, sensitivitySliderTrack, sensRatio, uiScale, chromeAlpha);

        // Rows 3, 4 & Back
        prims.drawButtonFill(shapes, fullscreenToggleBtn, false, mouseUiX, mouseUiY, chromeAlpha);
        prims.drawButtonFill(shapes, vsyncToggleBtn, false, mouseUiX, mouseUiY, chromeAlpha);
        prims.drawButtonFill(shapes, backButton, false, mouseUiX, mouseUiY, chromeAlpha);
    }

    @Override
    public void renderBorders(ShapeRenderer shapes, GothicMenuPrimitives prims, float mouseUiX, float mouseUiY) {
        boolean ghost = isGhostPreviewActive();
        float chromeAlpha = ghost ? 0.22f : 1.0f;

        prims.drawPanelBorders(shapes, panelBounds, uiScale, chromeAlpha);

        if (ghost) {
            shapes.setColor(0.82f, 0.66f, 0.30f, 0.95f);
            shapes.rect(uiScaleRowHighlight.x, uiScaleRowHighlight.y,
                uiScaleRowHighlight.width, uiScaleRowHighlight.height);
        }

        prims.drawButtonBorder(shapes, uiScaleMinusBtn, false, mouseUiX, mouseUiY, 1.0f);
        prims.drawButtonBorder(shapes, uiScalePlusBtn, false, mouseUiX, mouseUiY, 1.0f);
        prims.drawButtonBorder(shapes, uiScaleResetBtn, false, mouseUiX, mouseUiY, 1.0f);
        prims.drawSliderBorder(shapes, uiScaleSliderTrack, 1.0f);

        prims.drawButtonBorder(shapes, sensitivityMinusBtn, false, mouseUiX, mouseUiY, chromeAlpha);
        prims.drawButtonBorder(shapes, sensitivityPlusBtn, false, mouseUiX, mouseUiY, chromeAlpha);
        prims.drawSliderBorder(shapes, sensitivitySliderTrack, chromeAlpha);

        prims.drawButtonBorder(shapes, fullscreenToggleBtn, false, mouseUiX, mouseUiY, chromeAlpha);
        prims.drawButtonBorder(shapes, vsyncToggleBtn, false, mouseUiX, mouseUiY, chromeAlpha);
        prims.drawButtonBorder(shapes, backButton, false, mouseUiX, mouseUiY, chromeAlpha);
    }

    @Override
    public void renderText(SpriteBatch batch, UiFonts fonts, GothicMenuPrimitives prims,
                           float mouseUiX, float mouseUiY) {
        float chromeAlpha = isGhostPreviewActive() ? 0.22f : 1.0f;
        float centerX = panelBounds.x + panelBounds.width * 0.5f;
        float titleCenterY = panelBounds.y + panelBounds.height - 34f * uiScale;
        float displayedUiScale = getDisplayedUiScale();

        prims.drawCentered(batch, fonts.titleSerif, "SETTINGS",
            centerX, titleCenterY, 1.0f, 0.86f, 0.42f, chromeAlpha);

        int scalePct = Math.round(displayedUiScale * 100f);
        String scaleTitle = (activeSlider == ActiveSlider.UI_SCALE)
            ? "HUD / UI Scale: " + scalePct + "%  (Release to Apply)"
            : "HUD / UI Scale: " + scalePct + "%";
        prims.drawCentered(batch, fonts.hudMediumSans, scaleTitle,
            centerX, uiScaleSliderTrack.y + 38f * uiScale, 1.0f, 0.94f, 0.82f, 1.0f);

        prims.drawButtonLabel(batch, fonts.hudMediumSans, uiScaleMinusBtn, "-", mouseUiX, mouseUiY, 1.0f);
        prims.drawButtonLabel(batch, fonts.hudMediumSans, uiScalePlusBtn, "+", mouseUiX, mouseUiY, 1.0f);
        float resetAlpha = Math.abs(displayedUiScale - GameSettings.DEFAULT_UI_SCALE) < 1e-3f ? 0.55f : 1.0f;
        prims.drawSmallButtonLabel(batch, fonts.hudSmallSans, uiScaleResetBtn, "100%", mouseUiX, mouseUiY, resetAlpha);

        int sensPct = Math.round(settings.getCameraSensitivity() * 100f);
        prims.drawCentered(batch, fonts.hudMediumSans, "Camera Orbit Sensitivity: " + sensPct + "%",
            centerX, sensitivitySliderTrack.y + 38f * uiScale, 0.95f, 0.92f, 0.86f, chromeAlpha);
        prims.drawButtonLabel(batch, fonts.hudMediumSans, sensitivityMinusBtn, "-", mouseUiX, mouseUiY, chromeAlpha);
        prims.drawButtonLabel(batch, fonts.hudMediumSans, sensitivityPlusBtn, "+", mouseUiX, mouseUiY, chromeAlpha);

        String fsLabel = "Display Mode: " + (settings.isFullscreen() ? "Fullscreen" : "Windowed (1280x720)");
        prims.drawButtonLabel(batch, fonts.hudMediumSans, fullscreenToggleBtn, fsLabel, mouseUiX, mouseUiY, chromeAlpha);

        String vsyncLabel = "VSync: " + (settings.isVsyncEnabled() ? "ON" : "OFF");
        prims.drawButtonLabel(batch, fonts.hudMediumSans, vsyncToggleBtn, vsyncLabel, mouseUiX, mouseUiY, chromeAlpha);

        prims.drawButtonLabel(batch, fonts.hudMediumSans, backButton, "Back (Esc)", mouseUiX, mouseUiY, chromeAlpha);
    }
}
