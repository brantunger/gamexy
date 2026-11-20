package com.gamexy.character;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.scene3d.scene.Scene;

/**
 * Enemy entity with simple ARPG melee AI (Idle, Chase, Windup, Attack, Stunned, Dead).
 */
public class Enemy {

    public enum Type {
        SKELETON("Skeleton Warrior", 70f, 11f, 3.6f, 2.0f, 0.58f, 35),
        BRUTE("Demonic Brute", 175f, 24f, 2.8f, 2.6f, 0.92f, 95);

        public final String displayName;
        public final float maxHp;
        public final float damage;
        public final float moveSpeed;
        public final float attackRange;
        public final float radius;
        public final int xpReward;

        Type(String displayName, float maxHp, float damage, float moveSpeed,
             float attackRange, float radius, int xpReward) {
            this.displayName = displayName;
            this.maxHp = maxHp;
            this.damage = damage;
            this.moveSpeed = moveSpeed;
            this.attackRange = attackRange;
            this.radius = radius;
            this.xpReward = xpReward;
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

    public float hp;
    public float maxHp;
    public float facingDeg = 0f;
    public State state = State.IDLE;

    private float stateTimer = 0f;
    private float hitFlashTimer = 0f;
    private float deathTimer = 0f;
    private float walkTime = 0f;
    private boolean pendingAttackHit = false;

    public Enemy(Type type, Scene scene, float x, float z, int playerLevel) {
        this.type = type;
        this.scene = scene;
        this.position.set(x, 0f, z);
        float scale = 1f + (playerLevel - 1) * 0.18f;
        this.maxHp = Math.round(type.maxHp * scale);
        this.hp = this.maxHp;
        this.facingDeg = MathUtils.random(0f, 360f);
        updateTransform();
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

    /**
     * Applies damage, knockback, and optional stun. Returns true if this hit killed the enemy.
     */
    public boolean takeDamage(float amount, Vector3 fromPos, float knockbackStrength, float stunDuration) {
        if (!isAlive()) return false;

        hp = Math.max(0f, hp - amount);
        hitFlashTimer = 0.16f;

        // Knockback away from attacker
        float dx = position.x - fromPos.x;
        float dz = position.z - fromPos.z;
        float len = (float) Math.sqrt(dx * dx + dz * dz);
        if (len > 1e-4f && knockbackStrength > 0f) {
            float resistance = (type == Type.BRUTE) ? 0.55f : 1.0f;
            knockbackVel.set((dx / len) * knockbackStrength * resistance, 0f, (dz / len) * knockbackStrength * resistance);
        }

        if (hp <= 0f) {
            state = State.DEAD;
            deathTimer = 0f;
            pendingAttackHit = false;
            return true;
        }

        if (stunDuration > 0f) {
            state = State.STUNNED;
            stateTimer = Math.max(stateTimer, stunDuration);
            pendingAttackHit = false;
        }
        return false;
    }

    /**
     * Updates AI and returns true on the exact frame the enemy lands a melee hit on the player.
     */
    public boolean update(float delta, PlayerCharacter player, CollisionResolver resolver) {
        if (hitFlashTimer > 0f) {
            hitFlashTimer = Math.max(0f, hitFlashTimer - delta);
        }

        // Apply knockback
        if (knockbackVel.len2() > 0.01f) {
            position.mulAdd(knockbackVel, delta);
            knockbackVel.scl(Math.max(0f, 1f - delta * 8f));
            resolver.resolve(position, type.radius);
        }

        if (state == State.DEAD) {
            deathTimer += delta;
            updateTransform();
            return false;
        }

        boolean landedHit = false;
        float dx = player.position.x - position.x;
        float dz = player.position.z - position.z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);

        if (!player.isAlive()) {
            state = State.IDLE;
            updateTransform();
            return false;
        }

        switch (state) {
            case STUNNED:
                stateTimer -= delta;
                if (stateTimer <= 0f) {
                    state = State.CHASE;
                }
                break;

            case IDLE:
                if (dist < 14.5f) {
                    state = State.CHASE;
                }
                break;

            case CHASE:
                facingDeg = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
                if (dist <= type.attackRange) {
                    state = State.WINDUP;
                    stateTimer = (type == Type.BRUTE) ? 0.58f : 0.42f;
                    pendingAttackHit = true;
                } else {
                    float step = type.moveSpeed * delta;
                    position.x += (dx / dist) * step;
                    position.z += (dz / dist) * step;
                    resolver.resolve(position, type.radius);
                    walkTime += delta * 9.5f;
                }
                break;

            case WINDUP:
                facingDeg = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
                stateTimer -= delta;
                if (stateTimer <= 0f) {
                    if (pendingAttackHit && dist <= type.attackRange + 0.75f) {
                        landedHit = true;
                    }
                    pendingAttackHit = false;
                    state = State.RECOVER;
                    stateTimer = (type == Type.BRUTE) ? 0.75f : 0.55f;
                }
                break;

            case RECOVER:
                stateTimer -= delta;
                if (stateTimer <= 0f) {
                    state = State.CHASE;
                }
                break;

            default:
                break;
        }

        updateTransform();
        return landedHit;
    }

    private void updateTransform() {
        float bobY = 0f;
        float pitch = 0f;
        float scale = 1f;

        if (state == State.CHASE) {
            bobY = Math.abs(MathUtils.sin(walkTime)) * 0.09f;
        } else if (state == State.WINDUP) {
            pitch = -20f;
        } else if (state == State.RECOVER) {
            pitch = 16f;
        } else if (state == State.STUNNED) {
            pitch = MathUtils.sin(stateTimer * 28f) * 8f;
        } else if (state == State.DEAD) {
            float p = Math.min(1f, deathTimer / 0.4f);
            pitch = -90f * p;
            bobY = 0.25f * p;
            if (deathTimer > 0.7f) {
                scale = Math.max(0.01f, 1f - (deathTimer - 0.7f) / 0.4f);
            }
        }

        scene.modelInstance.transform.idt()
            .translate(position.x, position.y + bobY, position.z)
            .rotate(Vector3.Y, facingDeg)
            .rotate(Vector3.X, pitch)
            .scale(scale, scale, scale);
    }

    public interface CollisionResolver {
        void resolve(Vector3 pos, float radius);
    }
}
