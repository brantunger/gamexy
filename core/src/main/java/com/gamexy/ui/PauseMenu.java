package com.gamexy.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.gamexy.settings.GameSettings;
import com.gamexy.ui.menu.ControlsPanelView;
import com.gamexy.ui.menu.GothicMenuPrimitives;
import com.gamexy.ui.menu.GothicMenuView;
import com.gamexy.ui.menu.MainButtonListView;
import com.gamexy.ui.menu.MainButtonListView.ButtonEntry;
import com.gamexy.ui.menu.MenuSettingsAdapter;
import com.gamexy.ui.menu.SettingsPanelView;

import java.util.Arrays;

/**
 * Diablo-style Gothic pause menu overlay host toggled by pressing ESC.
 * Composes shared {@link GothicMenuView} sub-panels (Main, Controls, Settings).
 */
public class PauseMenu implements Disposable {

    public interface Callbacks extends MenuSettingsAdapter {
        void onResume();
        void onRestart();
        void onReturnToTitle();
        void onQuit();
    }

    private final UiFonts fonts;
    private final OrthographicCamera uiCam = new OrthographicCamera();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final GothicMenuPrimitives prims = new GothicMenuPrimitives();

    private final MainButtonListView mainView;
    private final ControlsPanelView controlsView;
    private final SettingsPanelView settingsView;
    private GothicMenuView activeView;

    private int screenW = 1280;
    private int screenH = 720;
    private float userUiScale;
    private float mouseUiX = -1f;
    private float mouseUiY = -1f;

    public PauseMenu(Callbacks callbacks, UiFonts fonts) {
        this(callbacks, fonts, callbacks.getUiScale());
    }

    public PauseMenu(Callbacks callbacks, UiFonts fonts, float userUiScale) {
        this.fonts = fonts;
        this.userUiScale = GameSettings.snapUiScale(userUiScale);
        this.controlsView = new ControlsPanelView(this::showMain);
        this.settingsView = new SettingsPanelView(callbacks, this::showMain, true);
        this.mainView = new MainButtonListView(
            "GAME PAUSED",
            "Press ESC to resume",
            Arrays.asList(
                new ButtonEntry("Resume Game", false, callbacks::onResume),
                new ButtonEntry("Restart Courtyard", false, callbacks::onRestart),
                new ButtonEntry("Controls", false, this::showControls),
                new ButtonEntry("Settings", false, this::showSettings),
                new ButtonEntry("Return to Title", false, callbacks::onReturnToTitle),
                new ButtonEntry("Quit to Desktop", true, callbacks::onQuit)
            ));
        this.activeView = mainView;
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void showMain() {
        showView(mainView);
    }

    private void showControls() {
        showView(controlsView);
    }

    private void showSettings() {
        showView(settingsView);
    }

    private void showView(GothicMenuView view) {
        this.activeView = view;
        this.activeView.onEnter();
        this.activeView.layout(screenW, screenH, userUiScale);
    }

    public void setUiScale(float userUiScale) {
        this.userUiScale = GameSettings.snapUiScale(userUiScale);
        activeView.layout(screenW, screenH, this.userUiScale);
    }

    public void resize(int width, int height) {
        this.screenW = Math.max(1, width);
        this.screenH = Math.max(1, height);
        uiCam.setToOrtho(false, screenW, screenH);
        uiCam.update();
        activeView.layout(screenW, screenH, userUiScale);
    }

    public void resetView() {
        activeView.onCancel();
        showView(mainView);
    }

    /**
     * Handles Escape while paused. Returns true if navigating back from a sub-panel; false if on main view.
     */
    public boolean handleEscape() {
        if (activeView != mainView) {
            activeView.onCancel();
            showView(mainView);
            return true;
        }
        return false;
    }

    public void handleMouseMoved(int screenX, int screenY) {
        mouseUiX = screenX;
        mouseUiY = screenH - screenY;
    }

    public boolean handleTouchDown(int screenX, int screenY, int button) {
        mouseUiX = screenX;
        mouseUiY = screenH - screenY;
        return activeView.handleTouchDown(mouseUiX, mouseUiY, button);
    }

    public boolean handleTouchDragged(int screenX, int screenY) {
        mouseUiX = screenX;
        mouseUiY = screenH - screenY;
        return activeView.handleTouchDragged(mouseUiX, mouseUiY);
    }

    public boolean handleTouchUp(int screenX, int screenY, int button) {
        mouseUiX = screenX;
        mouseUiY = screenH - screenY;
        return activeView.handleTouchUp(mouseUiX, mouseUiY, button);
    }

    public void render() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(uiCam.combined);
        batch.setProjectionMatrix(uiCam.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.02f, 0.015f, 0.03f, activeView.getBackdropAlpha());
        shapes.rect(0f, 0f, screenW, screenH);
        activeView.renderFilled(shapes, prims, mouseUiX, mouseUiY);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        activeView.renderBorders(shapes, prims, mouseUiX, mouseUiY);
        shapes.end();

        renderTextPass();
    }

    private void renderTextPass() {
        float menuFontScale = Math.max(0.05f, activeView.getUiScale() / Math.max(0.01f, fonts.getBuiltScale()));
        fonts.titleSerif.getData().setScale(menuFontScale);
        fonts.hudMediumSans.getData().setScale(menuFontScale);
        fonts.hudSmallSans.getData().setScale(menuFontScale);
        try {
            batch.begin();
            activeView.renderText(batch, fonts, prims, mouseUiX, mouseUiY);
            batch.end();
        } finally {
            fonts.titleSerif.getData().setScale(1.0f);
            fonts.hudMediumSans.getData().setScale(1.0f);
            fonts.hudSmallSans.getData().setScale(1.0f);
        }
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
    }
}
