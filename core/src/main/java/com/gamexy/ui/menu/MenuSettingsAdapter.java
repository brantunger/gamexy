package com.gamexy.ui.menu;

/**
 * Settings read/write adapter bridging {@link SettingsPanelView} to persistent settings and screen state.
 */
public interface MenuSettingsAdapter {
    float getCameraSensitivity();
    void setCameraSensitivity(float sensitivity);
    float getUiScale();
    void onUiScalePreview(float scale);
    void onUiScaleCommit(float scale);
    boolean isFullscreen();
    void setFullscreen(boolean fullscreen);
    boolean isVsyncEnabled();
    void setVsyncEnabled(boolean enabled);
}
