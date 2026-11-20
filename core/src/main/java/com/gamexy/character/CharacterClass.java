package com.gamexy.character;

/**
 * Playable character classes for gamexy.
 * Designed to expand as additional Diablo-style classes are introduced.
 */
public enum CharacterClass {
    BARBARIAN(
        "Barbarian",
        "Fury",
        150f,
        100f,
        22f,
        6.8f
    );

    public final String displayName;
    public final String resourceName;
    public final float baseMaxHp;
    public final float baseMaxResource;
    public final float baseDamage;
    public final float baseMoveSpeed;

    CharacterClass(String displayName, String resourceName,
                   float baseMaxHp, float baseMaxResource,
                   float baseDamage, float baseMoveSpeed) {
        this.displayName = displayName;
        this.resourceName = resourceName;
        this.baseMaxHp = baseMaxHp;
        this.baseMaxResource = baseMaxResource;
        this.baseDamage = baseDamage;
        this.baseMoveSpeed = baseMoveSpeed;
    }
}
