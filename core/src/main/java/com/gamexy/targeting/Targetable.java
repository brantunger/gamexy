package com.gamexy.targeting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/**
 * Common contract for any selectable entity in the 3D world:
 * enemies, NPCs (friendly or neutral), and interactive/static world objects.
 */
public interface Targetable {

    enum Category {
        ENEMY,
        NPC,
        WORLD_OBJECT
    }

    Category getTargetCategory();

    String getDisplayName();

    String getSubtitle();

    Vector3 getWorldPosition();

    /**
     * Horizontal radius used for 3D ray-capsule picking and the ground selection ring.
     */
    float getSelectionRadius();

    /**
     * Vertical height (from Y = 0) used for 3D ray-capsule picking.
     */
    float getSelectionHeight();

    /**
     * Returns false once the entity is dead, destroyed, or otherwise no longer selectable.
     */
    boolean isTargetable();

    default boolean hasHealthBar() {
        return false;
    }

    default float getCurrentHp() {
        return 0f;
    }

    default float getMaxHp() {
        return 0f;
    }

    /**
     * Accent color used for the 3D ground selection ring and target unit frame styling.
     */
    Color getTargetAccentColor();
}
