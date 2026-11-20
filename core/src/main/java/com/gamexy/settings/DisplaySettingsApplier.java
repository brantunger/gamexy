package com.gamexy.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;

/**
 * Applies display and VSync preferences to the active LibGDX graphics window.
 */
public final class DisplaySettingsApplier {

    private DisplaySettingsApplier() {}

    public static void apply(GameSettings settings) {
        applyVSync(settings.isVsync());
        applyDisplayMode(settings.isFullscreen());
    }

    public static void applyVSync(boolean enabled) {
        Gdx.graphics.setVSync(enabled);
    }

    public static void applyDisplayMode(boolean fullscreen) {
        if (fullscreen) {
            if (!Gdx.graphics.isFullscreen()) {
                Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
                Gdx.graphics.setFullscreenMode(mode);
            }
        } else if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(GameSettings.WINDOWED_WIDTH, GameSettings.WINDOWED_HEIGHT);
        }
    }
}
