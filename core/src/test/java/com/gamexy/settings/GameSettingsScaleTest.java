package com.gamexy.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameSettingsScaleTest {

    @Test
    void snapsUiScaleToFivePercentStepsWithinBounds() {
        assertEquals(0.50f, GameSettings.snapUiScale(0.10f), 1e-4f);
        assertEquals(1.25f, GameSettings.snapUiScale(2.00f), 1e-4f);
        assertEquals(1.00f, GameSettings.snapUiScale(1.02f), 1e-4f);
        assertEquals(1.05f, GameSettings.snapUiScale(1.03f), 1e-4f);
        assertEquals(0.75f, GameSettings.snapUiScale(0.74f), 1e-4f);
    }

    @Test
    void clampsCameraSensitivityWithinBounds() {
        assertEquals(GameSettings.MIN_CAMERA_SENSITIVITY, GameSettings.clampCameraSensitivity(-1f), 1e-4f);
        assertEquals(GameSettings.MAX_CAMERA_SENSITIVITY, GameSettings.clampCameraSensitivity(9f), 1e-4f);
        assertEquals(1.45f, GameSettings.clampCameraSensitivity(1.45f), 1e-4f);
    }

    @Test
    void computesBaseResolutionScaleFrom1280x720Baseline() {
        assertEquals(1.0f, GameSettings.baseResolutionScale(1280, 720), 1e-4f);
        assertEquals(1.5f, GameSettings.baseResolutionScale(1920, 1080), 1e-4f);
        assertEquals(2.0f, GameSettings.baseResolutionScale(2560, 1440), 1e-4f);
        assertEquals(0.75f, GameSettings.baseResolutionScale(640, 480), 1e-4f);
    }
}
