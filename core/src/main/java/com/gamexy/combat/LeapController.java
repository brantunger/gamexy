package com.gamexy.combat;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.character.Skill;
import com.gamexy.math.AngleMath;
import com.gamexy.targeting.Targetable;
import com.gamexy.world.ArenaCollisionResolver;

/**
 * Drives a parabolic 3D leap from the player's launch position to the selected target,
 * dynamically tracking target movement in flight and resolving safe landing collisions.
 */
public class LeapController {

    public static final float MAX_LEAP_RANGE = 13.5f;

    private final Vector3 startPos = new Vector3();
    private final Vector3 landingPos = new Vector3();

    private Targetable leapTarget;
    private Skill leapSkill;
    private float targetStandOff = 1.2f;
    private float elapsed = 0f;
    private float duration = 0.45f;
    private float arcHeight = 2.4f;
    private boolean active = false;

    public boolean isLeaping() {
        return active;
    }

    public Skill getActiveSkill() {
        return leapSkill;
    }

    /**
     * Begins a parabolic leap from {@code playerPos} to {@code target}.
     * Returns the computed flight duration in seconds so animations stay synchronized.
     */
    public float start(Vector3 playerPos, float playerRadius, Targetable target, float targetRadius, Skill skill) {
        this.startPos.set(playerPos.x, 0f, playerPos.z);
        this.leapTarget = target;
        this.leapSkill = skill;
        this.targetStandOff = playerRadius + targetRadius + 0.15f;

        computeLandingPoint(startPos, target.getWorldPosition(), targetStandOff, landingPos);
        float dist = AngleMath.distanceXZ(startPos, landingPos);

        this.duration = MathUtils.clamp(0.32f + dist * 0.022f, 0.34f, 0.62f);
        this.arcHeight = MathUtils.clamp(1.6f + dist * 0.22f, 1.8f, 4.2f);
        this.elapsed = 0f;
        this.active = true;
        return this.duration;
    }

    /**
     * Advances the airborne trajectory and updates {@code playerPos}.
     * Returns the Y-axis facing angle toward the landing point, and invokes {@code onLanded} on touchdown.
     */
    public boolean update(float dt,
                          Vector3 playerPos,
                          float playerRadius,
                          ArenaCollisionResolver collisionResolver,
                          Runnable onLanded) {
        if (!active) {
            return false;
        }

        if (leapTarget != null && leapTarget.isTargetable()) {
            computeLandingPoint(startPos, leapTarget.getWorldPosition(), targetStandOff, landingPos);
        }

        elapsed += dt;
        float t = MathUtils.clamp(elapsed / Math.max(1e-4f, duration), 0f, 1f);

        float x = MathUtils.lerp(startPos.x, landingPos.x, t);
        float z = MathUtils.lerp(startPos.z, landingPos.z, t);
        float y = 4f * arcHeight * t * (1f - t);
        playerPos.set(x, y, z);

        if (t >= 1f) {
            playerPos.y = 0f;
            if (collisionResolver != null) {
                collisionResolver.resolve(playerPos, playerRadius);
            }
            active = false;
            leapTarget = null;
            if (onLanded != null) {
                onLanded.run();
            }
            leapSkill = null;
            return false;
        }
        return true;
    }

    public float getFacingDeg(Vector3 playerPos) {
        if (AngleMath.distanceSquaredXZ(playerPos, landingPos) > 1e-4f) {
            return AngleMath.facingDegreesTo(playerPos, landingPos);
        }
        return AngleMath.facingDegreesTo(startPos, landingPos);
    }

    public void cancel(Vector3 playerPos) {
        if (active && playerPos != null) {
            playerPos.y = 0f;
        }
        active = false;
        leapTarget = null;
        leapSkill = null;
        elapsed = 0f;
    }

    private static void computeLandingPoint(Vector3 from, Vector3 targetPos, float standOff, Vector3 out) {
        float dx = targetPos.x - from.x;
        float dz = targetPos.z - from.z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);
        if (dist <= standOff || dist < 1e-4f) {
            out.set(targetPos.x, 0f, targetPos.z);
            return;
        }
        float travel = dist - standOff;
        out.set(from.x + (dx / dist) * travel, 0f, from.z + (dz / dist) * travel);
    }
}
