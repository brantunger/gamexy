package com.gamexy.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gamexy.GamexyGame;
import com.gamexy.assets.GameAssets;
import com.gamexy.camera.IsometricOrbitCamera;
import com.gamexy.character.CharacterClass;
import com.gamexy.character.PlayerCharacter;
import com.gamexy.combat.CombatFeedbackSystem;
import com.gamexy.combat.CombatSystem;
import com.gamexy.input.GameInputController;
import com.gamexy.render.PbrSceneContext;
import com.gamexy.settings.DisplaySettingsApplier;
import com.gamexy.settings.GameSettings;
import com.gamexy.targeting.TargetingSystem;
import com.gamexy.ui.GameHud;
import com.gamexy.ui.PauseMenu;
import com.gamexy.ui.UiFonts;
import com.gamexy.world.DungeonWorld;
import net.mgsx.gltf.scene3d.scene.Scene;

/**
 * Primary gameplay screen coordinator.
 * Owns screen lifecycle, wires gameplay/rendering/UI subsystems together, and manages pause & courtyard reset.
 */
public class GameScreen implements Screen, GameInputController.PauseHost {

    private final GamexyGame game;
    private final GameSettings settings;
    private final GameAssets assets;
    private final PbrSceneContext sceneContext;
    private final IsometricOrbitCamera orbitCam;
    private final DungeonWorld world;
    private final PlayerCharacter player;
    private final TargetingSystem targeting = new TargetingSystem();
    private final CombatFeedbackSystem feedback = new CombatFeedbackSystem();
    private final CombatSystem combat;
    private final UiFonts uiFonts;
    private final GameHud hud;
    private final PauseMenu pauseMenu;
    private final GameInputController inputController;
    private final ShapeRenderer vfxRenderer = new ShapeRenderer();

    private boolean paused = false;
    private boolean disposed = false;

    public GameScreen(GamexyGame game, GameSettings settings) {
        this.game = game;
        this.settings = settings;
        this.assets = new GameAssets();
        this.orbitCam = new IsometricOrbitCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.orbitCam.setOrbitSensitivity(settings.getCameraSensitivity());
        this.sceneContext = new PbrSceneContext(orbitCam.camera);

        Scene bodyScene = assets.createScene(assets.barbarian);
        Scene weaponScene = assets.createScene(assets.battleAxe);
        this.player = new PlayerCharacter(CharacterClass.BARBARIAN, bodyScene, weaponScene);
        this.sceneContext.addScene(bodyScene);
        this.sceneContext.addScene(weaponScene);

        this.world = new DungeonWorld(assets, sceneContext);
        this.orbitCam.snapToTarget(player.position);

        this.uiFonts = new UiFonts(settings.getUiScale());
        this.hud = new GameHud(uiFonts, settings.getUiScale());
        this.pauseMenu = new PauseMenu(createPauseCallbacks(), uiFonts, settings.getUiScale());
        this.combat = new CombatSystem(player, world, targeting, orbitCam, feedback);
        this.inputController = new GameInputController(
            this, player, world, targeting, combat, feedback, orbitCam, hud, pauseMenu);
        this.combat.setOnSelectedEnemyKilled(inputController::releaseLeftMouseHold);
        Gdx.input.setInputProcessor(inputController);
    }

    private PauseMenu.Callbacks createPauseCallbacks() {
        return new PauseMenu.Callbacks() {
            @Override
            public void onResume() {
                setPaused(false);
            }

            @Override
            public void onRestart() {
                restartCourtyard();
            }

            @Override
            public void onReturnToTitle() {
                game.showTitleScreen();
            }

            @Override
            public void onQuit() {
                Gdx.app.exit();
            }

            @Override
            public float getCameraSensitivity() {
                return orbitCam.getOrbitSensitivity();
            }

            @Override
            public void setCameraSensitivity(float sensitivity) {
                settings.setCameraSensitivity(sensitivity);
                orbitCam.setOrbitSensitivity(settings.getCameraSensitivity());
            }

            @Override
            public float getUiScale() {
                return settings.getUiScale();
            }

            @Override
            public void onUiScalePreview(float scale) {
                hud.setUiScale(scale);
            }

            @Override
            public void onUiScaleCommit(float scale) {
                settings.setUiScale(scale);
                float committed = settings.getUiScale();
                uiFonts.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), committed);
                hud.setUiScale(committed);
                pauseMenu.setUiScale(committed);
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

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) {
            inputController.clearHeldInputs();
            combat.cancelAutoApproach();
            targeting.setHoveredTarget(null);
            pauseMenu.resetView();
        }
    }

    private void restartCourtyard() {
        feedback.clear();
        combat.cancelActiveActions();
        targeting.clearSelection();
        player.resetToLevelOne();
        world.reset();
        orbitCam.resetOrientation();
        orbitCam.snapToTarget(player.position);
        setPaused(false);
        feedback.spawnText("COURTYARD RESET", player.position, Color.GOLD, 1.3f, 1.4f);
    }

    @Override
    public void render(float delta) {
        if (disposed) {
            return;
        }
        if (!paused) {
            updateSimulation(Math.min(delta, 0.05f));
        }
        renderFrame();
    }

    private void updateSimulation(float dt) {
        inputController.updateGameplayFrame(dt);
        world.update(dt, player.position, player.getLevel());
        combat.updateEnemies(dt);
        feedback.update(dt);
        sceneContext.update(dt);
    }

    private void renderFrame() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        sceneContext.render();
        renderWorldOverlays();
        hud.render(player, world.getEnemies(), feedback.getFloatingTexts(), orbitCam.camera,
            world.getWaveNumber(), targeting.getSelectedTarget());

        if (paused) {
            pauseMenu.render();
        }
    }

    private void renderWorldOverlays() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        vfxRenderer.setProjectionMatrix(orbitCam.camera.combined);
        vfxRenderer.begin(ShapeRenderer.ShapeType.Line);
        targeting.renderGroundRings(vfxRenderer);
        feedback.renderEffects(vfxRenderer);
        vfxRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        if (disposed) {
            return;
        }
        orbitCam.resize(width, height);
        uiFonts.update(width, height, settings.getUiScale());
        hud.resize(width, height);
        pauseMenu.resize(width, height);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputController);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == inputController) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        sceneContext.dispose();
        assets.dispose();
        hud.dispose();
        pauseMenu.dispose();
        uiFonts.dispose();
        vfxRenderer.dispose();
    }
}
