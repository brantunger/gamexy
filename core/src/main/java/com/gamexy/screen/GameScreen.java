package com.gamexy.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.GamexyGame;
import com.gamexy.assets.GameAssets;
import com.gamexy.camera.IsometricOrbitCamera;
import com.gamexy.character.CharacterClass;
import com.gamexy.character.Enemy;
import com.gamexy.character.PlayerCharacter;
import com.gamexy.character.Skill;
import com.gamexy.ui.FloatingText;
import com.gamexy.ui.GameHud;
import com.gamexy.ui.PauseMenu;
import com.gamexy.world.DungeonWorld;
import com.gamexy.world.VisualEffect;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Primary gameplay screen for gamexy:
 * - Third-person top-down camera with Alt+Mouse orbit & scroll zoom
 * - Camera-relative WASD Barbarian movement, mouse cursor facing
 * - Left-Click executes selected bottom-toolbar ability (1-4 or click bar to switch)
 * - Enemy AI, combat hit detection, knockback/stun, XP & leveling up
 */
public class GameScreen implements Screen {

    private final GamexyGame game;
    private final GameAssets assets;
    private final SceneManager sceneManager;
    private final IsometricOrbitCamera orbitCam;
    private final DungeonWorld world;
    private final PlayerCharacter player;
    private final GameHud hud;
    private final PauseMenu pauseMenu;

    private final ShapeRenderer vfxRenderer = new ShapeRenderer();
    private final List<VisualEffect> effects = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    // IBL resources for gdx-gltf PBR shading
    private final Cubemap diffuseCubemap;
    private final Cubemap specularCubemap;
    private final Texture brdfLUT;

    // Input & pause state
    private boolean paused = false;
    private boolean orbiting = false;
    private int lastDragX = 0;
    private int lastDragY = 0;
    private boolean leftMouseHeld = false;
    private final Vector3 moveDir = new Vector3();

    public GameScreen(GamexyGame game) {
        this.game = game;
        this.assets = new GameAssets();
        this.orbitCam = new IsometricOrbitCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Set up gdx-gltf PBR SceneManager + dark gothic IBL lighting
        PBRShaderConfig pbrConfig = PBRShaderProvider.createDefaultConfig();
        pbrConfig.numDirectionalLights = 2;
        pbrConfig.numPointLights = 8;
        this.sceneManager = new SceneManager(PBRShaderProvider.createDefault(pbrConfig),
            PBRShaderProvider.createDefaultDepth(12));
        this.sceneManager.setCamera(orbitCam.camera);
        this.sceneManager.setAmbientLight(0.32f);

        DirectionalLightEx moonLight = new DirectionalLightEx();
        moonLight.direction.set(-0.45f, -1.0f, -0.35f).nor();
        moonLight.color.set(0.76f, 0.80f, 0.96f, 1f);
        moonLight.intensity = 2.6f;
        this.sceneManager.environment.add(moonLight);

        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(moonLight);
        this.diffuseCubemap = iblBuilder.buildIrradianceMap(64);
        this.specularCubemap = iblBuilder.buildRadianceMap(6);
        iblBuilder.dispose();

        this.brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
        this.sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        this.sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        this.sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

        // Create Barbarian hero from standalone .glb assets
        Scene bodyScene = assets.createScene(assets.barbarian);
        Scene weaponScene = assets.createScene(assets.battleAxe);
        this.player = new PlayerCharacter(CharacterClass.BARBARIAN, bodyScene, weaponScene);
        this.sceneManager.addScene(bodyScene);
        this.sceneManager.addScene(weaponScene);

        // Build dungeon courtyard & enemies
        this.world = new DungeonWorld(assets, sceneManager);
        this.orbitCam.snapToTarget(player.position);

        this.hud = new GameHud();
        this.pauseMenu = new PauseMenu(new PauseMenu.Callbacks() {
            @Override
            public void onResume() {
                setPaused(false);
            }

            @Override
            public void onRestart() {
                restartCourtyard();
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
                orbitCam.setOrbitSensitivity(sensitivity);
            }
        });

        setupInput();
    }

    private void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) {
            orbiting = false;
            leftMouseHeld = false;
            pauseMenu.resetView();
        }
    }

    private void restartCourtyard() {
        effects.clear();
        floatingTexts.clear();
        player.resetToLevelOne();
        world.reset();
        orbitCam.resetOrientation();
        orbitCam.snapToTarget(player.position);
        setPaused(false);
        spawnText("COURTYARD RESET", player.position, Color.GOLD, 1.3f, 1.4f);
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                lastDragX = screenX;
                lastDragY = screenY;
                if (paused) {
                    return pauseMenu.handleTouchDown(screenX, screenY, button);
                }
                if (button == Input.Buttons.RIGHT || isAltHeld()) {
                    orbiting = true;
                    return true;
                }
                if (button == Input.Buttons.LEFT) {
                    if (hud.handleClick(screenX, screenY, player)) {
                        return true;
                    }
                    leftMouseHeld = true;
                    tryUseSelectedSkill();
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (paused) {
                    return pauseMenu.handleTouchUp(screenX, screenY, button);
                }
                if (button == Input.Buttons.RIGHT || isAltHeld()) {
                    orbiting = false;
                }
                if (button == Input.Buttons.LEFT) {
                    leftMouseHeld = false;
                    if (!Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                        orbiting = false;
                    }
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                int dx = screenX - lastDragX;
                int dy = screenY - lastDragY;
                lastDragX = screenX;
                lastDragY = screenY;
                if (paused) {
                    return pauseMenu.handleTouchDragged(screenX, screenY);
                }
                if (orbiting || Gdx.input.isButtonPressed(Input.Buttons.RIGHT) || isAltHeld()) {
                    orbitCam.orbit(dx, dy);
                    return true;
                }
                hud.updateHover(screenX, screenY);
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                lastDragX = screenX;
                lastDragY = screenY;
                if (paused) {
                    pauseMenu.handleMouseMoved(screenX, screenY);
                    return true;
                }
                hud.updateHover(screenX, screenY);
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (paused) {
                    return true;
                }
                orbitCam.zoom(amountY);
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (!paused) {
                        setPaused(true);
                    } else if (!pauseMenu.handleEscape()) {
                        setPaused(false);
                    }
                    return true;
                }
                if (paused) {
                    return true;
                }
                if (keycode == Input.Keys.NUM_1) return player.selectSkill(0);
                if (keycode == Input.Keys.NUM_2) return player.selectSkill(1);
                if (keycode == Input.Keys.NUM_3) return player.selectSkill(2);
                if (keycode == Input.Keys.NUM_4) return player.selectSkill(3);
                if (keycode == Input.Keys.R) {
                    if (!player.isAlive()) {
                        player.respawn();
                        orbitCam.snapToTarget(player.position);
                        spawnText("RESPAWNED", player.position, Color.CYAN, 1.2f, 1.3f);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private boolean isAltHeld() {
        return Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);
    }

    @Override
    public void render(float delta) {
        if (!paused) {
            float dt = Math.min(delta, 0.05f);
            updateGameplay(dt);
        }
        renderScene();
    }

    private void updateGameplay(float dt) {
        // 1. Camera-relative WASD movement & keyboard-driven character facing
        boolean moving = false;
        if (player.isAlive()) {
            moveDir.setZero();
            Vector3 fwd = orbitCam.getForwardXZ();
            Vector3 right = orbitCam.getRightXZ();

            if (Gdx.input.isKeyPressed(Input.Keys.W)) moveDir.add(fwd);
            if (Gdx.input.isKeyPressed(Input.Keys.S)) moveDir.sub(fwd);
            if (Gdx.input.isKeyPressed(Input.Keys.D)) moveDir.add(right);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) moveDir.sub(right);

            if (moveDir.len2() > 1e-4f) {
                moveDir.nor();
                player.position.mulAdd(moveDir, player.moveSpeed * dt);
                world.resolveCollision(player.position, player.radius);
                float targetFacing = MathUtils.atan2(moveDir.x, moveDir.z) * MathUtils.radiansToDegrees;
                float diff = shortestAngleDiff(player.facingDeg, targetFacing);
                player.facingDeg += diff * Math.min(1f, dt * 16f);
                moving = true;
            }

            // Allow holding LMB to keep attacking when ready
            if (leftMouseHeld && !orbiting && !isAltHeld()) {
                tryUseSelectedSkill();
            }
        }

        player.update(dt, moving);

        // 3. Camera follow
        orbitCam.setFollowTarget(player.position);
        orbitCam.update(dt);

        // 4. Update dungeon world & enemies
        world.update(dt, player.position, player.level);
        for (Enemy enemy : world.enemies) {
            boolean hitPlayer = enemy.update(dt, player, world::resolveCollision);
            if (hitPlayer && player.isAlive()) {
                float dmg = enemy.getDamage();
                player.takeDamage(dmg);
                spawnText("-" + Math.round(dmg), player.position, new Color(1f, 0.25f, 0.22f, 1f), 1.15f, 0.9f);
            }
        }

        // 5. Update VFX & floating text
        Iterator<VisualEffect> vfxIt = effects.iterator();
        while (vfxIt.hasNext()) {
            if (!vfxIt.next().update(dt)) {
                vfxIt.remove();
            }
        }
        Iterator<FloatingText> ftIt = floatingTexts.iterator();
        while (ftIt.hasNext()) {
            if (!ftIt.next().update(dt)) {
                ftIt.remove();
            }
        }

        sceneManager.update(dt);
    }

    private void tryUseSelectedSkill() {
        if (!player.isAlive()) return;
        int idx = player.selectedSkillIndex;
        Skill skill = player.getSelectedSkill();

        if (!player.isSkillReady(idx)) {
            return;
        }
        if (!player.hasEnoughFury(skill)) {
            if (floatingTexts.size() < 8) {
                spawnText("NOT ENOUGH FURY", player.position, new Color(1f, 0.58f, 0.18f, 1f), 1.0f, 0.85f);
            }
            return;
        }

        player.triggerSkillState(idx);

        // Spawn 3D VFX matching the skill
        if (skill.type == Skill.Type.CLEAVE) {
            effects.add(new VisualEffect(VisualEffect.Kind.CLEAVE_ARC, player.position,
                player.facingDeg, skill.range, skill.arcDegrees, skill.accentColor, 0.26f));
        } else if (skill.type == Skill.Type.GROUND_SLAM) {
            effects.add(new VisualEffect(VisualEffect.Kind.SHOCKWAVE, player.position,
                player.facingDeg, skill.range, 360f, skill.accentColor, 0.45f));
        } else if (skill.type == Skill.Type.WHIRLWIND) {
            effects.add(new VisualEffect(VisualEffect.Kind.WHIRLWIND, player.position,
                player.facingDeg, skill.range, 360f, skill.accentColor, 0.44f));
        } else if (skill.type == Skill.Type.WAR_CRY) {
            effects.add(new VisualEffect(VisualEffect.Kind.WAR_CRY, player.position,
                player.facingDeg, skill.range, 360f, skill.accentColor, 0.55f));
            spawnText("WAR CRY!", player.position, Color.GOLD, 1.35f, 1.2f);
        }

        // Resolve melee hits against enemies
        float halfArc = skill.arcDegrees * 0.5f;
        for (Enemy enemy : world.enemies) {
            if (!enemy.isAlive()) continue;
            float dx = enemy.position.x - player.position.x;
            float dz = enemy.position.z - player.position.z;
            float dist = (float) Math.sqrt(dx * dx + dz * dz);
            if (dist > skill.range + enemy.type.radius) continue;

            if (skill.arcDegrees < 359f && dist > 0.2f) {
                float angleToEnemy = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
                float diff = shortestAngleDiff(player.facingDeg, angleToEnemy);
                if (Math.abs(diff) > halfArc) {
                    continue;
                }
            }

            boolean crit = MathUtils.random() < 0.18f;
            float dmg = player.getEffectiveDamage(skill) * (crit ? 1.6f : 1.0f);
            boolean killed = enemy.takeDamage(dmg, player.position, skill.knockback, skill.stunDuration);

            Color dmgColor = crit ? Color.YELLOW : Color.WHITE;
            String label = crit ? Math.round(dmg) + "!" : String.valueOf(Math.round(dmg));
            spawnText(label, enemy.position, dmgColor, crit ? 1.35f : 1.05f, 0.85f);

            if (skill.stunDuration > 0f && !killed) {
                spawnText("STUNNED", enemy.position, Color.GOLD, 0.9f, 0.95f);
            }

            if (killed) {
                player.kills++;
                int xpGain = enemy.type.xpReward;
                spawnText("+" + xpGain + " XP", enemy.position, new Color(0.76f, 0.54f, 1.0f, 1f), 1.0f, 1.15f);
                boolean leveledUp = player.addXp(xpGain);
                if (leveledUp) {
                    onLevelUp();
                }
            }
        }
    }

    private void onLevelUp() {
        effects.add(new VisualEffect(VisualEffect.Kind.LEVEL_UP, player.position,
            0f, 1.8f, 360f, Color.GOLD, 1.4f));
        String msg = "LEVEL UP! LV " + player.level;
        for (Skill s : player.skills) {
            if (s.unlockLevel == player.level) {
                msg += " — UNLOCKED " + s.name.toUpperCase() + "!";
                break;
            }
        }
        spawnText(msg, player.position, Color.GOLD, 1.45f, 2.2f);
    }

    private void spawnText(String text, Vector3 pos, Color color, float scale, float duration) {
        float jitterX = MathUtils.random(-0.35f, 0.35f);
        float jitterZ = MathUtils.random(-0.35f, 0.35f);
        floatingTexts.add(new FloatingText(text, pos.x + jitterX, 2.2f, pos.z + jitterZ, color, scale, duration));
    }

    private static float shortestAngleDiff(float aDeg, float bDeg) {
        float diff = (bDeg - aDeg + 180f) % 360f - 180f;
        return diff < -180f ? diff + 360f : diff;
    }

    private void renderScene() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // 1. PBR 3D models via gdx-gltf
        sceneManager.render();

        // 2. 3D combat VFX
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        vfxRenderer.setProjectionMatrix(orbitCam.camera.combined);
        vfxRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (VisualEffect fx : effects) {
            fx.render(vfxRenderer);
        }
        vfxRenderer.end();

        // 3. 2D Diablo-style HUD overlay
        hud.render(player, world.enemies, floatingTexts, orbitCam.camera, world.getWaveNumber());

        // 4. Pause Menu overlay
        if (paused) {
            pauseMenu.render();
        }
    }

    @Override
    public void resize(int width, int height) {
        orbitCam.resize(width, height);
        hud.resize(width, height);
        pauseMenu.resize(width, height);
    }

    @Override
    public void show() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        sceneManager.dispose();
        assets.dispose();
        hud.dispose();
        pauseMenu.dispose();
        vfxRenderer.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
    }
}
