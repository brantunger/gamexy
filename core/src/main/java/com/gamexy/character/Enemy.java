package com.gamexy.character;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.targeting.Targetable;
import net.mgsx.gltf.scene3d.scene.Scene;

/**
 * Enemy entity holding combat stats, damage/knockback state, and {@link Targetable} metadata.
 * Delegates AI decisions to {@link EnemyAiController} and 3D transforms to {@link EnemyAnimator}.
 */
public class Enemy implements Targetable {

    private static final Color COLOR_HOSTILE = new Color(0.94f, 0.20f, 0.18f, 1f);
    private static final Color COLOR_NEUTRAL = new Color(0.96f, 0.82f, 0.24f, 1f);
    private static final Color COLOR_STUNNED = new Color(0.35f, 0.85f, 1.0f, 1f);

    public enum Type {
        SKELETON("Skeleton Warrior", 70f, 11f, 3.6f, 2.0f, 0.58f, 35, false, 0f),
        BRUTE("Demonic Brute", 175f, 24f, 2.8f, 2.6f, 0.92f, 95, true, 10.5f);

        public final String displayName;
        public final float maxHp;
        public final float damage;
        public final float moveSpeed;
        public final float attackRange;
        public final float radius;
        public final int xpReward;
        public final boolean aggressiveOnSight;
        public final float aggroRange;

        Type(String displayName, float maxHp, float damage, float moveSpeed,
             float attackRange, float radius, int xpReward,
             boolean aggressiveOnSight, float aggroRange) {
            this.displayName = displayName;
            this.maxHp = maxHp;
            this.damage = damage;
            this.moveSpeed = moveSpeed;
            this.attackRange = attackRange;
            this.radius = radius;
            this.xpReward = xpReward;
            this.aggressiveOnSight = aggressiveOnSight;
            this.aggroRange = aggroRange;
        }
    }

    public enum State {
        IDLE,
        CHASE,
        WINDUP,
        RECOVER,
        STUNNED,
        DEAD
    }

    public final Type type;
    public final Scene scene;
    public final Vector3 position = new Vector3();
    public final Vector3 knockbackVel = new Vector3();

    private final EnemyAiController aiController;
    private final EnemyAnimator animator = new EnemyAnimator();

    private final float maxHp;
    private float hp;
    private float facingDeg;
    private State state = State.IDLE;

    private boolean provoked = false;
    private float hitFlashTimer = 0f;
    private float deathTimer = 0f;

    public Enemy(Type type, Scene scene, float x, float z, int playerLevel) {
        this.type = type;
        this.scene = scene;
        this.position.set(x, 0f, z);
        this.aiController = new EnemyAiController(x, z);
        float scale = 1f + (playerLevel - 1) * 0.18f;
        this.maxHp = Math.round(type.maxHp * scale);
        this.hp = this.maxHp;
        this.facingDeg = MathUtils.random(0f, 360f);
        animator.applyTransform(this, false, 0f);
    }

    public float getHp() {
        return hp;
    }

    public float getFacingDeg() {
        return facingDeg;
    }

    public void setFacingDeg(float facingDeg) {
        this.facingDeg = facingDeg;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public float getDeathTimer() {
        return deathTimer;
    }

    public boolean isProvoked() {
        return provoked;
    }

    public void provoke() {
        this.provoked = true;
    }

    public void clearProvoked() {
        this.provoked = false;
    }

    public boolean isHostile() {
        return type.aggressiveOnSight || provoked;
    }

    public boolean isAlive() {
        return state != State.DEAD;
    }

    public boolean isReadyForRemoval() {
        return state == State.DEAD && deathTimer >= 1.1f;
    }

    public boolean isStunned() {
        return state == State.STUNNED;
    }

    public boolean isHitFlashing() {
        return hitFlashTimer > 0f;
    }

    public float getDamage() {
        return type.damage;
    }

    @Override
    public Category getTargetCategory() {
        return Category.ENEMY;
    }

    @Override
    public String getDisplayName() {
        return type.displayName;
    }

    @Override
    public String getSubtitle() {
        if (isStunned()) {
            return "STUNNED";
        }
        return isHostile() ? "Hostile Enemy" : "Neutral Enemy";
    }

    @Override
    public Vector3 getWorldPosition() {
        return position;
    }

    @Override
    public float getSelectionRadius() {
        return type.radius + 0.26f;
    }

    @Override
    public float getSelectionHeight() {
        return (type == Type.BRUTE) ? 2.75f : 2.15f;
    }

    @Override
    public boolean isTargetable() {
        return isAlive();
    }

    @Override
    public boolean hasHealthBar() {
        return true;
    }

    @Override
    public float getCurrentHp() {
        return hp;
    }

    @Override
    public float getMaxHp() {
        return maxHp;
    }

    @Override
    public Color getTargetAccentColor() {
        if (isStunned()) {
            return COLOR_STUNNED;
        }
        return isHostile() ? COLOR_HOSTILE : COLOR_NEUTRAL;
    }

    /**
     * Applies damage, knockback, and optional stun. Returns true if this hit killed the enemy.
     */
    public boolean takeDamage(float amount, Vector3 fromPos, float knockbackStrength, float stunDuration) {
        if (!isAlive()) {
            return false;
        }

        hp = Math.max(0f, hp - amount);
        hitFlashTimer = 0.16f;
        applyKnockback(fromPos, knockbackStrength);

        if (hp <= 0f) {
            state = State.DEAD;
            deathTimer = 0f;
            aiController.onKilled();
            return true;
        }

        aiController.onDamageTaken(this, stunDuration);
        return false;
    }

    private void applyKnockback(Vector3 fromPos, float knockbackStrength) {
        if (knockbackStrength <= 0f) {
            return;
        }
        float dx = position.x - fromPos.x;
        float dz = position.z - fromPos.z;
        float len = (float) Math.sqrt(dx * dx + dz * dz);
        if (len > 1e-4f) {
            float resistance = (type == Type.BRUTE) ? 0.55f : 1.0f;
            float impulse = knockbackStrength * resistance;
            knockbackVel.set((dx / len) * impulse, 0f, (dz / len) * impulse);
        }
    }

    /**
     * Updates timers, knockback, AI, and animation. Returns true on the exact frame a melee hit lands.
     */
    public boolean update(float delta, PlayerCharacter player, CollisionResolver resolver) {
        if (hitFlashTimer > 0f) {
            hitFlashTimer = Math.max(0f, hitFlashTimer - delta);
        }

        applyKnockbackDecay(delta, resolver);

        if (state == State.DEAD) {
            deathTimer += delta;
            animator.applyTransform(this, false, aiController.getStateTimer());
            return false;
        }

        boolean landedHit = aiController.update(this, delta, player, resolver, animator);
        animator.applyTransform(this, aiController.isWandering(), aiController.getStateTimer());
        return landedHit;
    }

    private void applyKnockbackDecay(float delta, CollisionResolver resolver) {
        if (knockbackVel.len2() > 0.01f) {
            position.mulAdd(knockbackVel, delta);
            knockbackVel.scl(Math.max(0f, 1f - delta * 8f));
            resolver.resolve(position, type.radius);
        }
    }

    public interface CollisionResolver {
        void resolve(Vector3 pos, float radius);
    }
}
