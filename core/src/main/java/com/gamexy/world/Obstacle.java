package com.gamexy.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.targeting.Targetable;
import net.mgsx.gltf.scene3d.scene.Scene;

/**
 * Static dungeon prop backed by a .glb Scene with circular collision footprint and click-selection metadata.
 */
public class Obstacle implements Targetable {

    public enum Kind {
        PILLAR("Stone Pillar", "Ancient Courtyard Pillar", 3.6f),
        BRAZIER("Fire Brazier", "Flickering Iron Brazier", 2.1f),
        CRATE("Supply Crate", "Weathered Wooden Crate", 1.35f);

        public final String displayName;
        public final String description;
        public final float selectionHeight;

        Kind(String displayName, String description, float selectionHeight) {
            this.displayName = displayName;
            this.description = description;
            this.selectionHeight = selectionHeight;
        }
    }

    private static final Color COLOR_WORLD_OBJECT = new Color(0.84f, 0.78f, 0.56f, 1f);

    public final Kind kind;
    public final Scene scene;
    public final Vector3 position;
    public final float radius;

    public Obstacle(Kind kind, Scene scene, float x, float z, float radius) {
        this.kind = kind;
        this.scene = scene;
        this.position = new Vector3(x, 0f, z);
        this.radius = radius;
    }

    @Override
    public Category getTargetCategory() {
        return Category.WORLD_OBJECT;
    }

    @Override
    public String getDisplayName() {
        return kind.displayName;
    }

    @Override
    public String getSubtitle() {
        return kind.description;
    }

    @Override
    public Vector3 getWorldPosition() {
        return position;
    }

    @Override
    public float getSelectionRadius() {
        return radius + 0.24f;
    }

    @Override
    public float getSelectionHeight() {
        return kind.selectionHeight;
    }

    @Override
    public boolean isTargetable() {
        return true;
    }

    @Override
    public Color getTargetAccentColor() {
        return COLOR_WORLD_OBJECT;
    }
}
