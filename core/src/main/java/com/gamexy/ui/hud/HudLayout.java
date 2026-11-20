package com.gamexy.ui.hud;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.gamexy.settings.GameSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Computes resolution- and user-scaled screen coordinates for the Diablo-style HUD
 * and performs hit-testing for toolbar slots, flanking orbs, and the target unit frame.
 */
public class HudLayout {

    public static final int TOOLBAR_SLOTS = 10;

    private final List<Rectangle> slotBounds = new ArrayList<>();
    private final List<Rectangle> slotBoundsView = Collections.unmodifiableList(slotBounds);
    public final Rectangle toolbarPanelBounds = new Rectangle();
    public final Rectangle headerBounds = new Rectangle();
    public final Rectangle xpBarBounds = new Rectangle();
    public final Rectangle targetFrameBounds = new Rectangle();

    private int screenW = 1280;
    private int screenH = 720;
    private float userUiScale = GameSettings.DEFAULT_UI_SCALE;
    private float uiScale = 1.0f;
    private float hudFitRatio = 1.0f;
    private float slotSize = 63f;
    private float orbRadius = 58f;
    private float leftOrbX = 0f;
    private float rightOrbX = 0f;
    private float orbY = 0f;

    private boolean targetFrameVisible = false;
    private int hoveredSlot = -1;

    public HudLayout(float userUiScale, int width, int height) {
        this.userUiScale = GameSettings.snapUiScale(userUiScale);
        resize(width, height);
    }

    public void setUiScale(float userUiScale) {
        this.userUiScale = GameSettings.snapUiScale(userUiScale);
        updateScaleAndLayout();
    }

    public void resize(int width, int height) {
        this.screenW = Math.max(1, width);
        this.screenH = Math.max(1, height);
        updateScaleAndLayout();
    }

    private void updateScaleAndLayout() {
        float baseResScale = GameSettings.baseResolutionScale(screenW, screenH);
        this.uiScale = baseResScale * this.userUiScale;
        recomputeLayout();
    }

    private void recomputeLayout() {
        slotBounds.clear();

        float desiredSlotSize = 63f * uiScale;
        float maxSlotFromWidth = (screenW * 0.96f) / 15.55f;
        float maxSlotFromHeight = (screenH * 0.36f) / 2.20f;
        float maxAllowedSlot = Math.min(maxSlotFromWidth, maxSlotFromHeight);
        this.slotSize = Math.max(24f, Math.min(desiredSlotSize, maxAllowedSlot));
        this.hudFitRatio = MathUtils.clamp(this.slotSize / Math.max(1f, desiredSlotSize), 0.35f, 1.0f);

        float slotGap = slotSize * 0.10f;
        float padX = slotSize * 0.22f;
        this.orbRadius = slotSize * 0.92f;
        float orbGap = slotSize * 0.24f;

        float xpBarH = Math.max(10f, slotSize * 0.27f);
        float headerH = Math.max(14f, slotSize * 0.38f);
        float padY = Math.max(4f, slotSize * 0.12f);

        float totalSlotsW = TOOLBAR_SLOTS * slotSize + (TOOLBAR_SLOTS - 1) * slotGap;
        float panelW = totalSlotsW + padX * 2f;
        float panelX = (screenW - panelW) * 0.5f;
        float panelY = Math.max(6f, 12f * uiScale * hudFitRatio);

        float xpY = panelY + padY;
        float slotY = xpY + xpBarH + padY;
        float headerY = slotY + slotSize + padY;
        float panelH = (headerY + headerH + padY * 0.7f) - panelY;

        toolbarPanelBounds.set(panelX, panelY, panelW, panelH);
        xpBarBounds.set(panelX + padX * 0.7f, xpY, panelW - padX * 1.4f, xpBarH);
        headerBounds.set(panelX + padX * 0.7f, headerY, panelW - padX * 1.4f, headerH);

        float startX = panelX + padX;
        for (int i = 0; i < TOOLBAR_SLOTS; i++) {
            slotBounds.add(new Rectangle(startX + i * (slotSize + slotGap), slotY, slotSize, slotSize));
        }

        float barScale = getBarScale();
        this.leftOrbX = toolbarPanelBounds.x - orbRadius - orbGap;
        this.rightOrbX = toolbarPanelBounds.x + toolbarPanelBounds.width + orbRadius + orbGap;
        this.orbY = toolbarPanelBounds.y + orbRadius + 4f * barScale;
    }

    public int getScreenW() { return screenW; }
    public int getScreenH() { return screenH; }
    public float getUiScale() { return uiScale; }
    public float getHudFitRatio() { return hudFitRatio; }
    public float getBarScale() { return uiScale * hudFitRatio; }
    public float getOrbRadius() { return orbRadius; }
    public float getLeftOrbX() { return leftOrbX; }
    public float getRightOrbX() { return rightOrbX; }
    public float getOrbY() { return orbY; }
    public int getHoveredSlot() { return hoveredSlot; }
    public List<Rectangle> getSlotBounds() { return slotBoundsView; }

    public void setTargetFrameVisible(boolean visible) {
        this.targetFrameVisible = visible;
    }

    public boolean isTargetFrameVisible() {
        return targetFrameVisible;
    }

    public int getClickedSkillSlot(int screenX, int screenY) {
        float uiY = screenH - screenY;
        for (int i = 0; i < slotBounds.size(); i++) {
            if (slotBounds.get(i).contains(screenX, uiY)) {
                return i;
            }
        }
        return -1;
    }

    public boolean containsPoint(int screenX, int screenY) {
        float uiY = screenH - screenY;
        if (toolbarPanelBounds.contains(screenX, uiY)) {
            return true;
        }
        if (targetFrameVisible && targetFrameBounds.contains(screenX, uiY)) {
            return true;
        }
        float orbHitR = orbRadius + 4f;
        float orbHitR2 = orbHitR * orbHitR;
        float ldx = screenX - leftOrbX;
        float ldy = uiY - orbY;
        if (ldx * ldx + ldy * ldy <= orbHitR2) {
            return true;
        }
        float rdx = screenX - rightOrbX;
        float rdy = uiY - orbY;
        return rdx * rdx + rdy * rdy <= orbHitR2;
    }

    public void updateHover(int screenX, int screenY) {
        float uiY = screenH - screenY;
        hoveredSlot = -1;
        for (int i = 0; i < slotBounds.size(); i++) {
            if (slotBounds.get(i).contains(screenX, uiY)) {
                hoveredSlot = i;
                break;
            }
        }
    }
}
