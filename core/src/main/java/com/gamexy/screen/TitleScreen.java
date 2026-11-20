package com.gamexy.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gamexy.GamexyGame;
import com.gamexy.settings.DisplaySettingsApplier;
import com.gamexy.settings.GameSettings;
import com.gamexy.ui.UiFonts;
import com.gamexy.ui.menu.ControlsPanelView;
import com.gamexy.ui.menu.GothicMenuPrimitives;
import com.gamexy.ui.menu.GothicMenuView;
import com.gamexy.ui.menu.MainButtonListView;
import com.gamexy.ui.menu.MainButtonListView.ButtonEntry;
import com.gamexy.ui.menu.MenuSettingsAdapter;
import com.gamexy.ui.menu.SettingsPanelView;
import com.gamexy.ui.menu.TitleBackdrop;

import java.util.Arrays;

/**
 * Diablo-style Gothic title screen shown at game launch.
 * Composes {@link TitleBackdrop} with shared {@link GothicMenuView} sub-panels.
 */
public class TitleScreen implements Screen {

    private final GamexyGame game;
    private final GameSettings settings;
    private final UiFonts fonts;
    private final OrthographicCamera uiCam = new OrthographicCamera();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final GothicMenuPrimitives prims = new GothicMenuPrimitives();
    private final TitleBackdrop backdrop = new TitleBackdrop();
    private final InputAdapter inputAdapter;

    private final MainButtonListView mainView;
    private final ControlsPanelView controlsView;
    private final SettingsPanelView settingsView;
    private GothicMenuView activeView;

    private int screenW = 1280;
    private int screenH = 720;
    private float userUiScale;
    private float mouseUiX = -1f;
    private float mouseUiY = -1f;
    private boolean disposed = false;

    public TitleScreen(GamexyGame game, GameSettings settings) {
        this.game = game;
        this.settings = settings;
        this.userUiScale = settings.getUiScale();
        this.fonts = new UiFonts(this.userUiScale);

        this.controlsView = new ControlsPanelView(this::showMain);
        this.settingsView = new SettingsPanelView(createSettingsAdapter(), this::showMain, false);
        this.mainView = new MainButtonListView(
            "MAIN MENU",
            "Press ENTER to begin",
            "GAMEXY",
            "ACTION RPG",
            Arrays.asList(
                new ButtonEntry("Play Game", false, game::startGame),
                new ButtonEntry("Controls", false, this::showControls),
                new ButtonEntry("Settings", false, this::showSettings),
                new ButtonEntry("Quit to Desktop", true, () -> Gdx.app.exit())
            ));
        this.activeView = mainView;
        this.inputAdapter = createInputAdapter();
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

    private MenuSettingsAdapter createSettingsAdapter() {
        return new MenuSettingsAdapter() {
            @Override
            public float getCameraSensitivity() {
                return settings.getCameraSensitivity();
            }

            @Override
            public void setCameraSensitivity(float sensitivity) {
                settings.setCameraSensitivity(sensitivity);
            }

            @Override
            public float getUiScale() {
                return settings.getUiScale();
            }

            @Override
            public void onUiScalePreview(float scale) {
                // Title screen keeps the slider stationary during drag and commits on release.
            }

            @Override
            public void onUiScaleCommit(float scale) {
                settings.setUiScale(scale);
                userUiScale = settings.getUiScale();
                fonts.update(screenW, screenH, userUiScale);
                activeView.layout(screenW, screenH, userUiScale);
            }

            @Override
            public boolean isFullscreen() {
                return Gdx.graphics.isFullscreen();
            }

            @Override
            public void setFullscreen(boolean fullscreen) {
                settings.setFullscreen(fullscreen);
                DisplaySettingsApplier.apply(settings);
            }

            @Override
            public boolean isVsyncEnabled() {
                return settings.isVsync();
            }

            @Override
            public void setVsyncEnabled(boolean enabled) {
                settings.setVsync(enabled);
                DisplaySettingsApplier.applyVSync(settings.isVsync());
            }
        };
    }

    private InputAdapter createInputAdapter() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE && activeView != mainView) {
                    activeView.onCancel();
                    showView(mainView);
                    return true;
                }
                if (keycode == Input.Keys.ENTER && activeView == mainView) {
                    game.startGame();
                    return true;
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                mouseUiX = screenX;
                mouseUiY = screenH - screenY;
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                mouseUiX = screenX;
                mouseUiY = screenH - screenY;
                return activeView.handleTouchDown(mouseUiX, mouseUiY, button);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                mouseUiX = screenX;
                mouseUiY = screenH - screenY;
                return activeView.handleTouchDragged(mouseUiX, mouseUiY);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                mouseUiX = screenX;
                mouseUiY = screenH - screenY;
                return activeView.handleTouchUp(mouseUiX, mouseUiY, button);
            }
        };
    }

    private void showView(GothicMenuView view) {
        this.activeView = view;
        this.activeView.onEnter();
        this.activeView.layout(screenW, screenH, userUiScale);
    }

    @Override
    public void render(float delta) {
        if (disposed) {
            return;
        }
        backdrop.update(Math.min(delta, 0.05f));

        Gdx.gl.glViewport(0, 0, screenW, screenH);
        Gdx.gl.glClearColor(0.03f, 0.022f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(uiCam.combined);
        batch.setProjectionMatrix(uiCam.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        backdrop.render(shapes, screenW, screenH);
        activeView.renderFilled(shapes, prims, mouseUiX, mouseUiY);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        activeView.renderBorders(shapes, prims, mouseUiX, mouseUiY);
        shapes.end();

        renderTextPass();
    }

    private void renderTextPass() {
        float menuFontScale = Math.max(0.05f, activeView.getUiScale() / Math.max(0.01f, fonts.getBuiltScale()));
        fonts.bannerSerif.getData().setScale(menuFontScale);
        fonts.headerSerif.getData().setScale(menuFontScale);
        fonts.titleSerif.getData().setScale(menuFontScale);
        fonts.hudMediumSans.getData().setScale(menuFontScale);
        fonts.hudSmallSans.getData().setScale(menuFontScale);
        try {
            batch.begin();
            activeView.renderText(batch, fonts, prims, mouseUiX, mouseUiY);
            batch.end();
        } finally {
            fonts.bannerSerif.getData().setScale(1.0f);
            fonts.headerSerif.getData().setScale(1.0f);
            fonts.titleSerif.getData().setScale(1.0f);
            fonts.hudMediumSans.getData().setScale(1.0f);
            fonts.hudSmallSans.getData().setScale(1.0f);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (disposed) {
            return;
        }
        this.screenW = Math.max(1, width);
        this.screenH = Math.max(1, height);
        uiCam.setToOrtho(false, screenW, screenH);
        uiCam.update();
        fonts.update(screenW, screenH, userUiScale);
        activeView.layout(screenW, screenH, userUiScale);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputAdapter);
        mouseUiX = Gdx.input.getX();
        mouseUiY = screenH - Gdx.input.getY();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == inputAdapter) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        shapes.dispose();
        batch.dispose();
        fonts.dispose();
    }
}
