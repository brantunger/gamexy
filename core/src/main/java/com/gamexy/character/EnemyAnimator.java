package com.gamexy.character;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Procedural 3D model animation controller for {@link Enemy}.
 */
public class EnemyAnimator {

    private float walkTime = 0f;

    public void advanceWalk(float delta, float speedMultiplier) {
        walkTime += delta * speedMultiplier;
    }

    public void applyTransform(Enemy enemy, boolean wandering, float stateTimer) {
        float bobY = 0f;
        float pitch = 0f;
        float scale = 1f;
        Enemy.State state = enemy.getState();

        if (state == Enemy.State.CHASE || (state == Enemy.State.IDLE && wandering)) {
            bobY = Math.abs(MathUtils.sin(walkTime)) * (state == Enemy.State.CHASE ? 0.09f : 0.05f);
        } else if (state == Enemy.State.WINDUP) {
            pitch = -20f;
        } else if (state == Enemy.State.RECOVER) {
            pitch = 16f;
        } else if (state == Enemy.State.STUNNED) {
            pitch = MathUtils.sin(stateTimer * 28f) * 8f;
        } else if (state == Enemy.State.DEAD) {
            float deathTimer = enemy.getDeathTimer();
            float p = Math.min(1f, deathTimer / 0.4f);
            pitch = -90f * p;
            bobY = 0.25f * p;
            if (deathTimer > 0.7f) {
                scale = Math.max(0.01f, 1f - (deathTimer - 0.7f) / 0.4f);
            }
        }

        enemy.scene.modelInstance.transform.idt()
            .translate(enemy.position.x, enemy.position.y + bobY, enemy.position.z)
            .rotate(Vector3.Y, enemy.getFacingDeg())
            .rotate(Vector3.X, pitch)
            .scale(scale, scale, scale);
    }
}
