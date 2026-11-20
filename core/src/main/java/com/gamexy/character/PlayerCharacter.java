package com.gamexy.character;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.scene3d.scene.Scene;

import java.util.List;

/**
 * Runtime state and procedural animation controller for the player's character.
 */
public class PlayerCharacter {

    public final CharacterClass characterClass;
    public final List<Skill> skills;
    public final float[] cooldowns;

    public final Scene bodyScene;
    public final Scene weaponScene;

    public final Vector3 position = new Vector3(0f, 0f, 0f);
    public float facingDeg = 0f; // 0 = +Z
    public float radius = 0.65f;

    public int level = 1;
    public int xp = 0;
    public int xpToNextLevel = 100;

    public float maxHp;
    public float hp;
    public float maxFury;
    public float fury;
    public float baseDamage;
    public float moveSpeed;

    public int selectedSkillIndex = 0;
    public int kills = 0;

    // Animation & buff timers
    private float walkTime = 0f;
    private float attackTimer = 0f;
    private float attackDuration = 0.35f;
    private Skill.Type activeAnimSkill = Skill.Type.CLEAVE;
    private float warCryBuffTimer = 0f;
    private float hurtFlashTimer = 0f;
    private float hpRegenDelay = 0f;

    private final Matrix4 tmpMat = new Matrix4();

    public PlayerCharacter(CharacterClass characterClass, Scene bodyScene, Scene weaponScene) {
        this.characterClass = characterClass;
        this.skills = SkillRegistry.getSkillsFor(characterClass);
        this.cooldowns = new float[skills.size()];
        this.bodyScene = bodyScene;
        this.weaponScene = weaponScene;

        this.maxHp = characterClass.baseMaxHp;
        this.hp = maxHp;
        this.maxFury = characterClass.baseMaxResource;
        this.fury = 25f;
        this.baseDamage = characterClass.baseDamage;
        this.moveSpeed = characterClass.baseMoveSpeed;

        updateTransforms(0f, false);
    }

    public boolean isAlive() {
        return hp > 0f;
    }

    public boolean isSkillUnlocked(int index) {
        if (index < 0 || index >= skills.size()) {
            return false;
        }
        return skills.get(index).unlockLevel <= level;
    }

    public boolean selectSkill(int index) {
        if (isSkillUnlocked(index)) {
            selectedSkillIndex = index;
            return true;
        }
        return false;
    }

    public Skill getSelectedSkill() {
        return skills.get(selectedSkillIndex);
    }

    public boolean isSkillReady(int index) {
        if (!isSkillUnlocked(index)) return false;
        return cooldowns[index] <= 0f;
    }

    public boolean hasEnoughFury(Skill skill) {
        return fury + 0.001f >= skill.furyCost;
    }

    public float getEffectiveDamage(Skill skill) {
        float buffMult = warCryBuffTimer > 0f ? 1.35f : 1.0f;
        float variance = MathUtils.random(0.90f, 1.12f);
        return baseDamage * skill.damageMultiplier * buffMult * variance;
    }

    public boolean isWarCryBuffActive() {
        return warCryBuffTimer > 0f;
    }

    public float getWarCryBuffRemaining() {
        return Math.max(0f, warCryBuffTimer);
    }

    public void triggerSkillState(int index) {
        Skill skill = skills.get(index);
        cooldowns[index] = skill.cooldown;
        fury = MathUtils.clamp(fury - skill.furyCost + skill.furyGain, 0f, maxFury);

        this.activeAnimSkill = skill.type;
        if (skill.type == Skill.Type.CLEAVE) {
            this.attackDuration = 0.30f;
        } else if (skill.type == Skill.Type.GROUND_SLAM) {
            this.attackDuration = 0.44f;
        } else if (skill.type == Skill.Type.WHIRLWIND) {
            this.attackDuration = 0.48f;
        } else {
            this.attackDuration = 0.40f;
            this.warCryBuffTimer = 8.0f;
            this.hp = Math.min(maxHp, hp + maxHp * 0.35f);
        }
        this.attackTimer = this.attackDuration;
    }

    public void takeDamage(float amount) {
        if (!isAlive()) return;
        hp = Math.max(0f, hp - amount);
        hurtFlashTimer = 0.18f;
        hpRegenDelay = 4.0f;
        // Barbarian gains a small amount of Fury when taking hits
        fury = Math.min(maxFury, fury + 5f);
    }

    /**
     * Adds XP and returns true if the player leveled up.
     */
    public boolean addXp(int amount) {
        xp += amount;
        boolean leveled = false;
        while (xp >= xpToNextLevel) {
            xp -= xpToNextLevel;
            level++;
            xpToNextLevel = Math.round(xpToNextLevel * 1.45f);
            maxHp += 32f;
            hp = maxHp;
            baseDamage += 6f;
            fury = Math.min(maxFury, fury + 35f);
            leveled = true;
        }
        return leveled;
    }

    public void respawn() {
        hp = maxHp;
        fury = 25f;
        position.set(0f, 0f, 0f);
        attackTimer = 0f;
        warCryBuffTimer = 0f;
        for (int i = 0; i < cooldowns.length; i++) {
            cooldowns[i] = 0f;
        }
    }

    public void resetToLevelOne() {
        level = 1;
        xp = 0;
        xpToNextLevel = 100;
        maxHp = characterClass.baseMaxHp;
        hp = maxHp;
        maxFury = characterClass.baseMaxResource;
        fury = 25f;
        baseDamage = characterClass.baseDamage;
        moveSpeed = characterClass.baseMoveSpeed;
        selectedSkillIndex = 0;
        kills = 0;
        position.set(0f, 0f, 0f);
        facingDeg = 0f;
        walkTime = 0f;
        attackTimer = 0f;
        warCryBuffTimer = 0f;
        hurtFlashTimer = 0f;
        hpRegenDelay = 0f;
        for (int i = 0; i < cooldowns.length; i++) {
            cooldowns[i] = 0f;
        }
        updateTransforms(0f, false);
    }

    public void update(float delta, boolean isMoving) {
        for (int i = 0; i < cooldowns.length; i++) {
            if (cooldowns[i] > 0f) {
                cooldowns[i] = Math.max(0f, cooldowns[i] - delta);
            }
        }
        if (attackTimer > 0f) {
            attackTimer = Math.max(0f, attackTimer - delta);
        }
        if (warCryBuffTimer > 0f) {
            warCryBuffTimer = Math.max(0f, warCryBuffTimer - delta);
        }
        if (hurtFlashTimer > 0f) {
            hurtFlashTimer = Math.max(0f, hurtFlashTimer - delta);
        }
        if (hpRegenDelay > 0f) {
            hpRegenDelay = Math.max(0f, hpRegenDelay - delta);
        } else if (isAlive() && hp < maxHp) {
            hp = Math.min(maxHp, hp + maxHp * 0.025f * delta);
        }
        // Passive fury decay out of combat
        if (fury > 0f && attackTimer <= 0f) {
            fury = Math.max(0f, fury - 1.8f * delta);
        }

        updateTransforms(delta, isMoving);
    }

    private void updateTransforms(float delta, boolean isMoving) {
        if (isMoving) {
            walkTime += delta * 11f;
        } else {
            walkTime = 0f;
        }

        float bobY = isMoving ? Math.abs(MathUtils.sin(walkTime)) * 0.12f : 0f;
        float swayZ = isMoving ? MathUtils.sin(walkTime) * 4.5f : 0f;
        float extraYaw = 0f;
        float bodyPitch = 0f;

        if (attackTimer > 0f) {
            float p = 1f - (attackTimer / attackDuration);
            if (activeAnimSkill == Skill.Type.WHIRLWIND) {
                extraYaw = p * 360f;
            } else if (activeAnimSkill == Skill.Type.GROUND_SLAM) {
                bobY += MathUtils.sin(p * MathUtils.PI) * 0.55f;
                bodyPitch = MathUtils.sin(p * MathUtils.PI) * 18f;
            } else if (activeAnimSkill == Skill.Type.CLEAVE) {
                extraYaw = MathUtils.lerp(-42f, 52f, p);
            } else if (activeAnimSkill == Skill.Type.WAR_CRY) {
                bobY += MathUtils.sin(p * MathUtils.PI) * 0.25f;
                bodyPitch = -12f * MathUtils.sin(p * MathUtils.PI);
            }
        }

        // Body transform
        bodyScene.modelInstance.transform.idt()
            .translate(position.x, position.y + bobY, position.z)
            .rotate(Vector3.Y, facingDeg + extraYaw)
            .rotate(Vector3.X, bodyPitch)
            .rotate(Vector3.Z, swayZ);

        // Weapon transform attached to Barbarian's right hand
        tmpMat.set(bodyScene.modelInstance.transform);
        if (attackTimer > 0f) {
            float p = 1f - (attackTimer / attackDuration);
            if (activeAnimSkill == Skill.Type.CLEAVE) {
                float sweepYaw = MathUtils.lerp(-80f, 85f, p);
                tmpMat.translate(0.55f, 1.15f, 0.42f)
                    .rotate(Vector3.Y, sweepYaw)
                    .rotate(Vector3.X, 78f);
            } else if (activeAnimSkill == Skill.Type.GROUND_SLAM) {
                float slamPitch = p < 0.4f
                    ? MathUtils.lerp(20f, -65f, p / 0.4f)
                    : MathUtils.lerp(-65f, 125f, (p - 0.4f) / 0.6f);
                tmpMat.translate(0.45f, 1.35f, 0.35f)
                    .rotate(Vector3.X, slamPitch);
            } else if (activeAnimSkill == Skill.Type.WHIRLWIND) {
                tmpMat.translate(0.65f, 1.20f, 0.25f)
                    .rotate(Vector3.Z, -85f)
                    .rotate(Vector3.X, 80f);
            } else {
                tmpMat.translate(0.60f, 1.55f, 0.25f)
                    .rotate(Vector3.Z, -20f);
            }
        } else {
            // Ready combat stance
            float idleSway = MathUtils.sin(walkTime * 0.8f) * 5f;
            tmpMat.translate(0.58f, 1.08f, 0.38f)
                .rotate(Vector3.X, 34f + idleSway)
                .rotate(Vector3.Z, -14f);
        }

        weaponScene.modelInstance.transform.set(tmpMat);
    }

    public boolean isHurtFlashing() {
        return hurtFlashTimer > 0f;
    }
}
