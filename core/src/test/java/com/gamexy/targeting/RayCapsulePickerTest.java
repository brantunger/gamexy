package com.gamexy.targeting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RayCapsulePickerTest {

    private static final class StubTarget implements Targetable {
        final Category category;
        final Vector3 pos;
        final float radius;
        final float height;

        StubTarget(Category category, float x, float z, float radius, float height) {
            this.category = category;
            this.pos = new Vector3(x, 0f, z);
            this.radius = radius;
            this.height = height;
        }

        @Override public Category getTargetCategory() { return category; }
        @Override public String getDisplayName() { return category.name(); }
        @Override public String getSubtitle() { return ""; }
        @Override public Vector3 getWorldPosition() { return pos; }
        @Override public float getSelectionRadius() { return radius; }
        @Override public float getSelectionHeight() { return height; }
        @Override public boolean isTargetable() { return true; }
        @Override public Color getTargetAccentColor() { return Color.WHITE; }
    }

    @Test
    void hitsCapsuleAlongRayAndMissesOutsideRadius() {
        Ray directRay = new Ray(new Vector3(0f, 1.0f, -10f), new Vector3(0f, 0f, 1f));
        float hitT = RayCapsulePicker.intersectRayCapsule(directRay, new Vector3(0f, 0f, 0f), 0.8f, 2.2f);
        assertTrue(hitT > 9f && hitT < 11f);

        Ray missRay = new Ray(new Vector3(5f, 1.0f, -10f), new Vector3(0f, 0f, 1f));
        assertEquals(-1f, RayCapsulePicker.intersectRayCapsule(missRay, new Vector3(0f, 0f, 0f), 0.8f, 2.2f));
    }

    @Test
    void favorsCharacterOverSlightlyCloserPropWhenSilhouettesOverlap() {
        RayCapsulePicker picker = new RayCapsulePicker();
        Ray ray = new Ray(new Vector3(0f, 1.0f, -10f), new Vector3(0f, 0f, 1f));

        StubTarget enemy = new StubTarget(Targetable.Category.ENEMY, 0f, 1.0f, 0.8f, 2.2f);
        StubTarget crateSlightlyCloser = new StubTarget(Targetable.Category.WORLD_OBJECT, 0f, 0.2f, 0.9f, 1.5f);

        Targetable picked = picker.pickTarget(
            ray,
            Collections.singletonList(enemy),
            Collections.emptyList(),
            Collections.singletonList(crateSlightlyCloser));

        assertSame(enemy, picked);
    }

    @Test
    void picksPropWhenMuchCloserThanCharacterBehindIt() {
        RayCapsulePicker picker = new RayCapsulePicker();
        Ray ray = new Ray(new Vector3(0f, 1.0f, -10f), new Vector3(0f, 0f, 1f));

        StubTarget farEnemy = new StubTarget(Targetable.Category.ENEMY, 0f, 5.0f, 0.8f, 2.2f);
        StubTarget nearPillar = new StubTarget(Targetable.Category.WORLD_OBJECT, 0f, 0.0f, 1.0f, 3.6f);

        Targetable picked = picker.pickTarget(
            ray,
            Collections.singletonList(farEnemy),
            Collections.emptyList(),
            Collections.singletonList(nearPillar));

        assertSame(nearPillar, picked);
    }

    @Test
    void returnsNullWhenNothingIsHit() {
        RayCapsulePicker picker = new RayCapsulePicker();
        Ray ray = new Ray(new Vector3(10f, 1.0f, -10f), new Vector3(0f, 0f, 1f));
        StubTarget enemy = new StubTarget(Targetable.Category.ENEMY, 0f, 0f, 0.8f, 2.2f);

        assertNull(picker.pickTarget(ray, Collections.singletonList(enemy), null, null));
    }
}
