package com.gamexy.character;

import com.badlogic.gdx.graphics.Color;

import java.util.Arrays;
import java.util.List;

/**
 * Registry of class skills. Barbarian starts at Level 1 with Cleave and Ground Slam,
 * and unlocks Whirlwind at Level 2 and War Cry at Level 3.
 */
public final class SkillRegistry {

    private SkillRegistry() {}

    public static List<Skill> getSkillsFor(CharacterClass characterClass) {
        if (characterClass == CharacterClass.BARBARIAN) {
            return Arrays.asList(
                new Skill(
                    Skill.Type.CLEAVE,
                    "Cleave",
                    "CLEAVE",
                    "Wide melee axe arc. Generates +18 Fury.",
                    1,
                    0f,
                    18f,
                    0.42f,
                    3.2f,
                    135f,
                    1.0f,
                    2.6f,
                    0.0f,
                    new Color(0.88f, 0.34f, 0.18f, 1f)
                ),
                new Skill(
                    Skill.Type.GROUND_SLAM,
                    "Ground Slam",
                    "SLAM",
                    "Crushes nearby foes with a shockwave. Stuns & knocks back. Costs 30 Fury.",
                    1,
                    30f,
                    0f,
                    2.0f,
                    4.5f,
                    360f,
                    1.90f,
                    6.8f,
                    1.4f,
                    new Color(0.95f, 0.62f, 0.14f, 1f)
                ),
                new Skill(
                    Skill.Type.WHIRLWIND,
                    "Whirlwind",
                    "WHIRL",
                    "Spinning 360-degree axe strike hitting all nearby enemies. Costs 20 Fury.",
                    2,
                    20f,
                    0f,
                    0.75f,
                    3.8f,
                    360f,
                    1.40f,
                    3.2f,
                    0.0f,
                    new Color(0.82f, 0.20f, 0.25f, 1f)
                ),
                new Skill(
                    Skill.Type.WAR_CRY,
                    "War Cry",
                    "CRY",
                    "Heals 35% HP, grants +35 Fury, and boosts damage +35% for 8s.",
                    3,
                    0f,
                    35f,
                    10.0f,
                    5.0f,
                    360f,
                    0.5f,
                    5.5f,
                    0.6f,
                    new Color(0.92f, 0.80f, 0.22f, 1f)
                )
            );
        }
        throw new IllegalArgumentException("Unsupported class: " + characterClass);
    }
}
