package com.gamexy.character;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.scene3d.scene.Scene;

import java.util.Collections;
import java.util.List;

/**
 * Runtime combat state, progression, cooldowns, and resource management for the player's hero.
 */
public class PlayerCharacter {

    public final CharacterClass characterClass;
    public final List<Skill> skills;
    public final Scene bodyScene;
    public final Scene weaponScene;
    public final Vector3 position = new Vector3(0f, 0f, 0f);
    public final float radius = 0.65f;

    private final float[] cooldowns;
    private final PlayerAnimator animator = new PlayerAnimator();

    private float facingDeg = 0f; // 0 = +Z
    private int level = 1;
    private int xp = 0;
    private int xpToNextLevel = 100;

    private float maxHp;
    private float hp;
    private float maxFury;
    private float fury;
    private float baseDamage;
    private float moveSpeed;

    private int selectedSkillIndex = 0;
    private int kills = 0;

    private float attackTimer = 0f;
    private float attackDuration = 0.35f;
    private Skill.Type activeAnimSkill = Skill.Type.CLEAVE;
    private float warCryBuffTimer = 0f;
    private float hurtFlashTimer = 0f;
    private float hpRegenDelay = 0f;

    public PlayerCharacter(CharacterClass characterClass, Scene bodyScene, Scene weaponScene) {
        this.characterClass = characterClass;
        this.skills = Collections.unmodifiableList(SkillRegistry.getSkillsFor(characterClass));
        this.cooldowns = new float[skills.size()];
        this.bodyScene = bodyScene;
        this.weaponScene = weaponScene;

        this.maxHp = characterClass.baseMaxHp;
        this.hp = maxHp;
        this.maxFury = characterClass.baseMaxResource;
        this.fury = 25f;
        this.baseDamage = characterClass.baseDamage;
        this.moveSpeed = characterClass.baseMoveSpeed;

        animator.update(this, 0f, false);
    }

    public boolean isAlive() {
        return hp > 0f;
    }

    public float getFacingDeg() {
        return facingDeg;
    }

    public void setFacingDeg(float facingDeg) {
        this.facingDeg = facingDeg;
    }

    public void turnBy(float deltaDeg) {
        this.facingDeg = (this.facingDeg + deltaDeg) % 360f;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getXpToNextLevel() {
        return xpToNextLevel;
    }

    public float getHp() {
        return hp;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public float getFury() {
        return fury;
    }

    public float getMaxFury() {
        return maxFury;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public int getSelectedSkillIndex() {
        return selectedSkillIndex;
    }

    public int getKills() {
        return kills;
    }

    public void recordKill() {
        kills++;
    }

    public float getCooldown(int index) {
        if (index < 0 || index >= cooldowns.length) {
            return 0f;
        }
        return cooldowns[index];
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
        return isSkillUnlocked(index) && cooldowns[index] <= 0f;
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

    public boolean isHurtFlashing() {
        return hurtFlashTimer > 0f;
    }

    public boolean isAttacking() {
        return attackTimer > 0f;
    }

    public float getAttackProgress() {
        if (attackDuration <= 1e-4f) {
            return 1f;
        }
        return MathUtils.clamp(1f - (attackTimer / attackDuration), 0f, 1f);
    }

    public Skill.Type getActiveAnimSkill() {
        return activeAnimSkill;
    }

    public void triggerSkillState(int index) {
        Skill skill = skills.get(index);
        triggerSkillState(index, resolveAttackDuration(skill.type));
    }

    public void triggerSkillState(int index, float durationOverride) {
        Skill skill = skills.get(index);
        cooldowns[index] = skill.cooldown;
        fury = MathUtils.clamp(fury - skill.furyCost + skill.furyGain, 0f, maxFury);

        this.activeAnimSkill = skill.type;
        this.attackDuration = Math.max(0.1f, durationOverride);
        if (skill.type == Skill.Type.WAR_CRY) {
            this.warCryBuffTimer = 8.0f;
            this.hp = Math.min(maxHp, hp + maxHp * 0.35f);
        }
        this.attackTimer = this.attackDuration;
    }

    private static float resolveAttackDuration(Skill.Type type) {
        switch (type) {
            case CLEAVE:      return 0.30f;
            case GROUND_SLAM: return 0.44f;
            case WHIRLWIND:   return 0.48f;
            case LEAP:        return 0.46f;
            default:          return 0.40f;
        }
    }

    public void takeDamage(float amount) {
        if (!isAlive()) {
            return;
        }
        hp = Math.max(0f, hp - amount);
        hurtFlashTimer = 0.18f;
        hpRegenDelay = 4.0f;
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
        clearCooldowns();
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
        attackTimer = 0f;
        warCryBuffTimer = 0f;
        hurtFlashTimer = 0f;
        hpRegenDelay = 0f;
        clearCooldowns();
        animator.reset();
        animator.update(this, 0f, false);
    }

    public void update(float delta, boolean isMoving) {
        tickCooldowns(delta);
        tickTimers(delta);
        applyPassiveRegenAndDecay(delta);
        animator.update(this, delta, isMoving);
    }

    private void clearCooldowns() {
        for (int i = 0; i < cooldowns.length; i++) {
            cooldowns[i] = 0f;
        }
    }

    private void tickCooldowns(float delta) {
        for (int i = 0; i < cooldowns.length; i++) {
            if (cooldowns[i] > 0f) {
                cooldowns[i] = Math.max(0f, cooldowns[i] - delta);
            }
        }
    }

    private void tickTimers(float delta) {
        if (attackTimer > 0f) {
            attackTimer = Math.max(0f, attackTimer - delta);
        }
        if (warCryBuffTimer > 0f) {
            warCryBuffTimer = Math.max(0f, warCryBuffTimer - delta);
        }
        if (hurtFlashTimer > 0f) {
            hurtFlashTimer = Math.max(0f, hurtFlashTimer - delta);
        }
    }

    private void applyPassiveRegenAndDecay(float delta) {
        if (hpRegenDelay > 0f) {
            hpRegenDelay = Math.max(0f, hpRegenDelay - delta);
        } else if (isAlive() && hp < maxHp) {
            hp = Math.min(maxHp, hp + maxHp * 0.025f * delta);
        }
        if (fury > 0f && attackTimer <= 0f) {
            fury = Math.max(0f, fury - 1.8f * delta);
        }
    }
}
