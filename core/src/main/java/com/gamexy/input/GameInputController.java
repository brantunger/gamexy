package com.gamexy.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.camera.IsometricOrbitCamera;
import com.gamexy.character.Enemy;
import com.gamexy.character.PlayerCharacter;
import com.gamexy.combat.CombatFeedbackSystem;
import com.gamexy.combat.CombatSystem;
import com.gamexy.targeting.Targetable;
import com.gamexy.targeting.TargetingSystem;
import com.gamexy.ui.GameHud;
import com.gamexy.ui.PauseMenu;
import com.gamexy.world.DungeonWorld;

/**
 * Translates raw keyboard and mouse events into gameplay actions, WASD locomotion,
 * camera orbit/follow control, and pause-menu delegation.
 */
public class GameInputController extends InputAdapter {

    public interface PauseHost {
        boolean isPaused();
        void setPaused(boolean paused);
    }

    private static final float TURN_SPEED_DEG = 165f;

    private final PauseHost pauseHost;
    private final PlayerCharacter player;
    private final DungeonWorld world;
    private final TargetingSystem targeting;
    private final CombatSystem combat;
    private final CombatFeedbackSystem feedback;
    private final IsometricOrbitCamera orbitCam;
    private final GameHud hud;
    private final PauseMenu pauseMenu;

    private final Vector3 moveDir = new Vector3();

    private boolean orbiting = false;
    private boolean rightMouseHeld = false;
    private boolean leftMouseHeld = false;
    private boolean mouseMoveHeld = false;
    private int lastDragX = 0;
    private int lastDragY = 0;

    public GameInputController(PauseHost pauseHost,
                               PlayerCharacter player,
                               DungeonWorld world,
                               TargetingSystem targeting,
                               CombatSystem combat,
                               CombatFeedbackSystem feedback,
                               IsometricOrbitCamera orbitCam,
                               GameHud hud,
                               PauseMenu pauseMenu) {
        this.pauseHost = pauseHost;
        this.player = player;
        this.world = world;
        this.targeting = targeting;
        this.combat = combat;
        this.feedback = feedback;
        this.orbitCam = orbitCam;
        this.hud = hud;
        this.pauseMenu = pauseMenu;
    }

    public void clearHeldInputs() {
        orbiting = false;
        rightMouseHeld = false;
        leftMouseHeld = false;
        mouseMoveHeld = false;
    }

    public void releaseLeftMouseHold() {
        leftMouseHeld = false;
    }

    /**
     * Polls continuous movement keys, updates player locomotion/auto-approach,
     * and drives camera auto-follow vs manual orbit.
     */
    public void updateGameplayFrame(float dt) {
        boolean moving = false;
        boolean usingWasd = false;
        boolean autoApproaching = false;
        boolean leaping = false;

        if (player.isAlive()) {
            if (combat.isLeaping()) {
                leaping = true;
                combat.updateLeap(dt);
            } else if (mouseMoveHeld) {
                combat.cancelAutoApproach();
                player.setFacingDeg(orbitCam.getForwardYawDeg());
                applyForwardMovement(1f, dt);
                moving = true;
            } else {
                float turnInput = sampleTurnInput();
                float forwardInput = sampleForwardInput();
                usingWasd = (turnInput != 0f || forwardInput != 0f);

                if (usingWasd) {
                    combat.cancelAutoApproach();
                }
                if (turnInput != 0f && !player.isAttacking()) {
                    player.turnBy(turnInput * TURN_SPEED_DEG * dt);
                }
                if (forwardInput != 0f) {
                    applyForwardMovement(forwardInput, dt);
                    moving = true;
                } else if (combat.isAutoApproaching()) {
                    autoApproaching = true;
                    moving = combat.updateAutoApproach(dt);
                }

                if (leftMouseHeld && !orbiting && !isAltHeld() && targeting.getSelectedEnemy() != null) {
                    combat.requestUseSkill(player.getSelectedSkillIndex());
                }
            }
        } else {
            combat.cancelActiveActions();
        }

        player.update(dt, moving);
        targeting.update(dt, player.position, world.getEnemies());
        updateCamera(dt, usingWasd, autoApproaching || leaping);
    }

    private float sampleTurnInput() {
        float turn = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) turn += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) turn -= 1f;
        return turn;
    }

    private float sampleForwardInput() {
        float fwd = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) fwd += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) fwd -= 1f;
        return fwd;
    }

    private void applyForwardMovement(float forwardInput, float dt) {
        float facingRad = player.getFacingDeg() * MathUtils.degreesToRadians;
        moveDir.set(MathUtils.sin(facingRad), 0f, MathUtils.cos(facingRad));
        float speed = player.getMoveSpeed() * (forwardInput < 0f ? 0.72f : 1.0f);
        player.position.mulAdd(moveDir, forwardInput * speed * dt);
        world.resolveCollision(player.position, player.radius);
    }

    private void updateCamera(float dt, boolean usingWasd, boolean autoApproaching) {
        boolean manualOrbit = orbiting || Gdx.input.isButtonPressed(Input.Buttons.RIGHT) || isAltHeld();
        boolean autoOrient = usingWasd || autoApproaching
            || (player.isAttacking() && targeting.getSelectedEnemy() != null);
        if (autoOrient && !manualOrbit) {
            orbitCam.followBehind(player.getFacingDeg(), dt);
        }
        orbitCam.setFollowTarget(player.position);
        orbitCam.update(dt);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        lastDragX = screenX;
        lastDragY = screenY;
        if (pauseHost.isPaused()) {
            return pauseMenu.handleTouchDown(screenX, screenY, button);
        }
        if (button == Input.Buttons.RIGHT) {
            rightMouseHeld = true;
            orbiting = true;
            return true;
        }
        if (button == Input.Buttons.LEFT) {
            if (rightMouseHeld || Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                rightMouseHeld = true;
                orbiting = true;
                mouseMoveHeld = true;
                leftMouseHeld = false;
                combat.cancelAutoApproach();
                return true;
            }
            if (isAltHeld()) {
                orbiting = true;
                return true;
            }
            return handleLeftClick(screenX, screenY);
        }
        if (isAltHeld()) {
            orbiting = true;
            return true;
        }
        return false;
    }

    private boolean handleLeftClick(int screenX, int screenY) {
        int clickedSlot = hud.getClickedSkillSlot(screenX, screenY);
        if (clickedSlot >= 0) {
            combat.requestUseSkill(clickedSlot);
            return true;
        }
        if (hud.containsPoint(screenX, screenY)) {
            return true;
        }

        Targetable picked = targeting.pickTarget(
            orbitCam.camera, screenX, screenY,
            world.getEnemies(), world.getNpcs(), world.getObstacles());
        if (picked != null) {
            if (picked instanceof Enemy && picked == targeting.getSelectedTarget()) {
                leftMouseHeld = true;
                combat.requestUseSkill(player.getSelectedSkillIndex());
            } else {
                combat.cancelAutoApproach();
                targeting.setSelectedTarget(picked);
                leftMouseHeld = false;
            }
        } else {
            combat.cancelAutoApproach();
            targeting.clearSelection();
            leftMouseHeld = false;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pauseHost.isPaused()) {
            return pauseMenu.handleTouchUp(screenX, screenY, button);
        }
        if (button == Input.Buttons.RIGHT) {
            rightMouseHeld = false;
            mouseMoveHeld = false;
            orbiting = false;
        } else if (button == Input.Buttons.LEFT) {
            leftMouseHeld = false;
            mouseMoveHeld = false;
            if (!rightMouseHeld && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                orbiting = false;
            }
        } else if (isAltHeld()) {
            orbiting = false;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        int dx = screenX - lastDragX;
        int dy = screenY - lastDragY;
        lastDragX = screenX;
        lastDragY = screenY;
        if (pauseHost.isPaused()) {
            return pauseMenu.handleTouchDragged(screenX, screenY);
        }
        if (orbiting || Gdx.input.isButtonPressed(Input.Buttons.RIGHT) || isAltHeld()) {
            orbitCam.orbit(dx, dy);
            return true;
        }
        updateWorldHover(screenX, screenY);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        lastDragX = screenX;
        lastDragY = screenY;
        if (pauseHost.isPaused()) {
            pauseMenu.handleMouseMoved(screenX, screenY);
            return true;
        }
        updateWorldHover(screenX, screenY);
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (pauseHost.isPaused()) {
            return true;
        }
        orbitCam.zoom(amountY);
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            return handleEscape();
        }
        if (pauseHost.isPaused()) {
            return true;
        }
        if (keycode == Input.Keys.TAB) {
            return handleTab();
        }
        int skillIndex = skillIndexForKeycode(keycode);
        if (skillIndex >= 0) {
            if (player.isSkillUnlocked(skillIndex)) {
                combat.requestUseSkill(skillIndex);
                return true;
            }
            return false;
        }
        if (keycode == Input.Keys.R) {
            return handleRespawn();
        }
        return false;
    }

    private boolean handleEscape() {
        if (!pauseHost.isPaused()) {
            if (targeting.hasSelection()) {
                combat.cancelAutoApproach();
                targeting.clearSelection();
            } else {
                pauseHost.setPaused(true);
            }
        } else if (!pauseMenu.handleEscape()) {
            pauseHost.setPaused(false);
        }
        return true;
    }

    private boolean handleTab() {
        combat.cancelAutoApproach();
        boolean reverse = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        targeting.cycleNearbyEnemy(player.position, world.getEnemies(), reverse);
        return true;
    }

    private boolean handleRespawn() {
        if (!player.isAlive()) {
            player.respawn();
            orbitCam.snapToTarget(player.position);
            feedback.spawnText("RESPAWNED", player.position, Color.CYAN, 1.2f, 1.3f);
        }
        return true;
    }

    private void updateWorldHover(int screenX, int screenY) {
        hud.updateHover(screenX, screenY);
        if (hud.containsPoint(screenX, screenY)) {
            targeting.setHoveredTarget(null);
        } else {
            Targetable hovered = targeting.pickTarget(
                orbitCam.camera, screenX, screenY,
                world.getEnemies(), world.getNpcs(), world.getObstacles());
            targeting.setHoveredTarget(hovered);
        }
    }

    private static int skillIndexForKeycode(int keycode) {
        switch (keycode) {
            case Input.Keys.NUM_1: return 0;
            case Input.Keys.NUM_2: return 1;
            case Input.Keys.NUM_3: return 2;
            case Input.Keys.NUM_4: return 3;
            case Input.Keys.NUM_5: return 4;
            case Input.Keys.NUM_6: return 5;
            case Input.Keys.NUM_7: return 6;
            case Input.Keys.NUM_8: return 7;
            case Input.Keys.NUM_9: return 8;
            case Input.Keys.NUM_0: return 9;
            default: return -1;
        }
    }

    private static boolean isAltHeld() {
        return Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);
    }
}
