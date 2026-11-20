package com.gamexy.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;

/**
 * Persistent user settings backed by LibGDX Preferences.
 * Manages HUD/UI scale, camera orbit sensitivity, fullscreen mode, and VSync.
 */
public class GameSettings {

    public static final String PREFS_NAME = "gamexy-settings";

    public static final float MIN_UI_SCALE = 0.50f;
    public static final float MAX_UI_SCALE = 1.25f;
    public static final float UI_SCALE_STEP = 0.05f;
    public static final float DEFAULT_UI_SCALE = 1.00f;

    public static final float MIN_CAMERA_SENSITIVITY = 0.30f;
    public static final float MAX_CAMERA_SENSITIVITY = 2.50f;
    public static final float DEFAULT_CAMERA_SENSITIVITY = 1.00f;

    public static final boolean DEFAULT_FULLSCREEN = true;
    public static final boolean DEFAULT_VSYNC = true;
    public static final int WINDOWED_WIDTH = 1280;
    public static final int WINDOWED_HEIGHT = 720;

    private static final String KEY_UI_SCALE = "uiScale";
    private static final String KEY_CAMERA_SENSITIVITY = "cameraSensitivity";
    private static final String KEY_FULLSCREEN = "fullscreen";
    private static final String KEY_VSYNC = "vsync";

    private final Preferences prefs;

    private float uiScale = DEFAULT_UI_SCALE;
    private float cameraSensitivity = DEFAULT_CAMERA_SENSITIVITY;
    private boolean fullscreen = DEFAULT_FULLSCREEN;
    private boolean vsync = DEFAULT_VSYNC;

    public GameSettings() {
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);
        load();
    }

    public static float baseResolutionScale(int width, int height) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        return MathUtils.clamp(Math.min(w / 1280f, h / 720f), 0.75f, 3.0f);
    }

    public static float snapUiScale(float value) {
        float clamped = MathUtils.clamp(value, MIN_UI_SCALE, MAX_UI_SCALE);
        int steps = Math.round(clamped / UI_SCALE_STEP);
        return MathUtils.clamp(steps * UI_SCALE_STEP, MIN_UI_SCALE, MAX_UI_SCALE);
    }

    public static float clampCameraSensitivity(float value) {
        return MathUtils.clamp(value, MIN_CAMERA_SENSITIVITY, MAX_CAMERA_SENSITIVITY);
    }

    public void load() {
        this.uiScale = snapUiScale(prefs.getFloat(KEY_UI_SCALE, DEFAULT_UI_SCALE));
        this.cameraSensitivity = clampCameraSensitivity(
            prefs.getFloat(KEY_CAMERA_SENSITIVITY, DEFAULT_CAMERA_SENSITIVITY));
        this.fullscreen = prefs.getBoolean(KEY_FULLSCREEN, DEFAULT_FULLSCREEN);
        this.vsync = prefs.getBoolean(KEY_VSYNC, DEFAULT_VSYNC);
    }

    public void save() {
        prefs.putFloat(KEY_UI_SCALE, uiScale);
        prefs.putFloat(KEY_CAMERA_SENSITIVITY, cameraSensitivity);
        prefs.putBoolean(KEY_FULLSCREEN, fullscreen);
        prefs.putBoolean(KEY_VSYNC, vsync);
        prefs.flush();
    }

    public float getUiScale() {
        return uiScale;
    }

    public void setUiScale(float scale) {
        float snapped = snapUiScale(scale);
        if (Math.abs(snapped - this.uiScale) > 1e-4f) {
            this.uiScale = snapped;
            save();
        }
    }

    public float getCameraSensitivity() {
        return cameraSensitivity;
    }

    public void setCameraSensitivity(float sensitivity) {
        float clamped = clampCameraSensitivity(sensitivity);
        if (Math.abs(clamped - this.cameraSensitivity) > 1e-4f) {
            this.cameraSensitivity = clamped;
            save();
        }
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        if (this.fullscreen != fullscreen) {
            this.fullscreen = fullscreen;
            save();
        }
    }

    public boolean isVsync() {
        return vsync;
    }

    public void setVsync(boolean vsync) {
        if (this.vsync != vsync) {
            this.vsync = vsync;
            save();
        }
    }
}
