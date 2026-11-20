package com.gamexy.character;

import com.badlogic.gdx.graphics.Color;

/**
 * Definition of an active character ability shown on the bottom toolbar.
 */
public final class Skill {

    public enum Type {
        CLEAVE,
        GROUND_SLAM,
        WHIRLWIND,
        WAR_CRY,
        LEAP
    }

    public final Type type;
    public final String name;
    public final String shortLabel;
    public final String description;
    public final int unlockLevel;
    public final float furyCost;
    public final float furyGain;
    public final float cooldown;
    public final float range;
    public final float arcDegrees;
    public final float damageMultiplier;
    public final float knockback;
    public final float stunDuration;
    public final Color accentColor;

    public Skill(Type type, String name, String shortLabel, String description,
                 int unlockLevel, float furyCost, float furyGain, float cooldown,
                 float range, float arcDegrees, float damageMultiplier,
                 float knockback, float stunDuration, Color accentColor) {
        this.type = type;
        this.name = name;
        this.shortLabel = shortLabel;
        this.description = description;
        this.unlockLevel = unlockLevel;
        this.furyCost = furyCost;
        this.furyGain = furyGain;
        this.cooldown = cooldown;
        this.range = range;
        this.arcDegrees = arcDegrees;
        this.damageMultiplier = damageMultiplier;
        this.knockback = knockback;
        this.stunDuration = stunDuration;
        this.accentColor = accentColor;
    }
}
