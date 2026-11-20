package com.gamexy.character;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.math.AngleMath;

/**
 * Melee AI state machine (Idle/Wander, Chase, Windup, Recover, Stunned) for an {@link Enemy}.
 */
public class EnemyAiController {

    private final Vector3 homePosition = new Vector3();
    private final Vector3 wanderTarget = new Vector3();

    private float wanderTimer;
    private boolean wandering = false;
    private float stateTimer = 0f;
    private boolean pendingAttackHit = false;

    public EnemyAiController(float homeX, float homeZ) {
        this.homePosition.set(homeX, 0f, homeZ);
        this.wanderTarget.set(homeX, 0f, homeZ);
        this.wanderTimer = MathUtils.random(1.0f, 3.5f);
    }

    public boolean isWandering() {
        return wandering;
    }

    public float getStateTimer() {
        return stateTimer;
    }

    public void onKilled() {
        pendingAttackHit = false;
    }

    public void onDamageTaken(Enemy enemy, float stunDuration) {
        if (stunDuration > 0f) {
            enemy.setState(Enemy.State.STUNNED);
            stateTimer = Math.max(stateTimer, stunDuration);
            pendingAttackHit = false;
        } else if (enemy.getState() == Enemy.State.IDLE) {
            enemy.setState(Enemy.State.CHASE);
        }
        enemy.provoke();
    }

    /**
     * Advances the AI state machine and returns true on the exact frame a melee attack connects.
     */
    public boolean update(Enemy enemy,
                          float delta,
                          PlayerCharacter player,
                          Enemy.CollisionResolver resolver,
                          EnemyAnimator animator) {
        if (!player.isAlive()) {
            resetToIdle(enemy);
            return false;
        }

        float dx = player.position.x - enemy.position.x;
        float dz = player.position.z - enemy.position.z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        switch (enemy.getState()) {
            case STUNNED:
                updateStunned(enemy, delta);
                return false;
            case IDLE:
                updateIdle(enemy, delta, dist, resolver, animator);
                return false;
            case CHASE:
                updateChase(enemy, delta, dx, dz, dist, resolver, animator);
                return false;
            case WINDUP:
                return updateWindup(enemy, delta, dx, dz, dist);
            case RECOVER:
                updateRecover(enemy, delta);
                return false;
            default:
                return false;
        }
    }

    private void resetToIdle(Enemy enemy) {
        enemy.setState(Enemy.State.IDLE);
        wandering = false;
        if (!enemy.type.aggressiveOnSight) {
            enemy.clearProvoked();
        }
    }

    private void updateStunned(Enemy enemy, float delta) {
        stateTimer -= delta;
        if (stateTimer <= 0f) {
            enemy.setState(Enemy.State.CHASE);
        }
    }

    private void updateIdle(Enemy enemy, float delta, float distToPlayer,
                            Enemy.CollisionResolver resolver, EnemyAnimator animator) {
        if (enemy.isProvoked() || (enemy.type.aggressiveOnSight && distToPlayer < enemy.type.aggroRange)) {
            enemy.provoke();
            wandering = false;
            enemy.setState(Enemy.State.CHASE);
        } else {
            updateWander(enemy, delta, resolver, animator);
        }
    }

    private void updateWander(Enemy enemy, float delta,
                              Enemy.CollisionResolver resolver, EnemyAnimator animator) {
        wanderTimer -= delta;
        if (!wandering) {
            if (wanderTimer <= 0f) {
                pickNewWanderTarget();
            }
            return;
        }

        float wdist = AngleMath.distanceXZ(enemy.position, wanderTarget);
        if (wdist < 0.35f || wanderTimer <= 0f) {
            wandering = false;
            wanderTimer = MathUtils.random(1.8f, 4.2f);
            return;
        }

        float wdx = wanderTarget.x - enemy.position.x;
        float wdz = wanderTarget.z - enemy.position.z;
        enemy.setFacingDeg(MathUtils.atan2(wdx, wdz) * MathUtils.radiansToDegrees);
        float step = enemy.type.moveSpeed * 0.38f * delta;
        enemy.position.x += (wdx / wdist) * step;
        enemy.position.z += (wdz / wdist) * step;
        resolver.resolve(enemy.position, enemy.type.radius);
        animator.advanceWalk(delta, 5.2f);
    }

    private void pickNewWanderTarget() {
        float angle = MathUtils.random(0f, MathUtils.PI2);
        float radius = MathUtils.random(1.5f, 3.8f);
        wanderTarget.set(
            homePosition.x + MathUtils.cos(angle) * radius,
            0f,
            homePosition.z + MathUtils.sin(angle) * radius
        );
        wandering = true;
        wanderTimer = MathUtils.random(2.0f, 4.0f);
    }

    private void updateChase(Enemy enemy, float delta, float dx, float dz, float dist,
                             Enemy.CollisionResolver resolver, EnemyAnimator animator) {
        enemy.setFacingDeg(MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees);
        if (dist <= enemy.type.attackRange) {
            enemy.setState(Enemy.State.WINDUP);
            stateTimer = (enemy.type == Enemy.Type.BRUTE) ? 0.58f : 0.42f;
            pendingAttackHit = true;
            return;
        }
        float step = enemy.type.moveSpeed * delta;
        enemy.position.x += (dx / dist) * step;
        enemy.position.z += (dz / dist) * step;
        resolver.resolve(enemy.position, enemy.type.radius);
        animator.advanceWalk(delta, 9.5f);
    }

    private boolean updateWindup(Enemy enemy, float delta, float dx, float dz, float dist) {
        enemy.setFacingDeg(MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees);
        stateTimer -= delta;
        if (stateTimer > 0f) {
            return false;
        }
        boolean landedHit = pendingAttackHit && dist <= enemy.type.attackRange + 0.75f;
        pendingAttackHit = false;
        enemy.setState(Enemy.State.RECOVER);
        stateTimer = (enemy.type == Enemy.Type.BRUTE) ? 0.75f : 0.55f;
        return landedHit;
    }

    private void updateRecover(Enemy enemy, float delta) {
        stateTimer -= delta;
        if (stateTimer <= 0f) {
            enemy.setState(Enemy.State.CHASE);
        }
    }
}
