package com.gamexy.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.character.Skill;
import com.gamexy.targeting.Targetable;
import com.gamexy.world.ArenaCollisionResolver;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeapControllerTest {

    private static final class StubTarget implements Targetable {
        final Vector3 pos;
        boolean targetable = true;

        StubTarget(float x, float z) {
            this.pos = new Vector3(x, 0f, z);
        }

        @Override public Category getTargetCategory() { return Category.ENEMY; }
        @Override public String getDisplayName() { return "Enemy"; }
        @Override public String getSubtitle() { return "Hostile"; }
        @Override public Vector3 getWorldPosition() { return pos; }
        @Override public float getSelectionRadius() { return 0.8f; }
        @Override public float getSelectionHeight() { return 2.0f; }
        @Override public boolean isTargetable() { return targetable; }
        @Override public Color getTargetAccentColor() { return Color.RED; }
    }

    private static Skill createLeapSkill() {
        return new Skill(
            Skill.Type.LEAP, "Leap", "LEAP", "Leap test",
            1, 0f, 20f, 5.0f, 3.2f, 360f, 1.25f, 4.2f, 0.5f, Color.CYAN);
    }

    @Test
    void arcsParabolicallyAndLandsAdjacentToTarget() {
        LeapController controller = new LeapController();
        Vector3 playerPos = new Vector3(0f, 0f, 0f);
        StubTarget enemy = new StubTarget(0f, 10f);
        ArenaCollisionResolver resolver = new ArenaCollisionResolver(20f, Collections.emptyList());
        AtomicInteger landCount = new AtomicInteger();

        float duration = controller.start(playerPos, 0.65f, enemy, 0.58f, createLeapSkill());
        assertTrue(controller.isLeaping());

        // Mid-flight should be elevated above ground
        controller.update(duration * 0.5f, playerPos, 0.65f, resolver, landCount::incrementAndGet);
        assertTrue(controller.isLeaping());
        assertTrue(playerPos.y > 1.5f, "Expected parabolic lift at flight apex");
        assertEquals(0, landCount.get());

        // Touchdown should land cleanly on Y=0 just in front of the enemy
        controller.update(duration * 0.6f, playerPos, 0.65f, resolver, landCount::incrementAndGet);
        assertFalse(controller.isLeaping());
        assertEquals(0f, playerPos.y, 1e-4f);
        assertEquals(1, landCount.get());
        float expectedZ = 10f - (0.65f + 0.58f + 0.15f);
        assertEquals(expectedZ, playerPos.z, 1e-3f);
    }

    @Test
    void tracksMovingTargetDuringFlightAndCancelResetsHeight() {
        LeapController controller = new LeapController();
        Vector3 playerPos = new Vector3(0f, 0f, 0f);
        StubTarget enemy = new StubTarget(0f, 8f);
        ArenaCollisionResolver resolver = new ArenaCollisionResolver(20f, Collections.emptyList());

        float duration = controller.start(playerPos, 0.65f, enemy, 0.58f, createLeapSkill());
        controller.update(duration * 0.4f, playerPos, 0.65f, resolver, null);
        assertTrue(playerPos.y > 0.5f);

        // Enemy strafes mid-flight; landing should adjust toward new X position
        enemy.pos.set(4f, 0f, 8f);
        controller.update(duration * 0.7f, playerPos, 0.65f, resolver, null);
        assertFalse(controller.isLeaping());
        assertTrue(playerPos.x > 2.5f, "Expected leap landing to track target lateral movement");

        // Cancel mid-flight restores ground height cleanly
        controller.start(playerPos, 0.65f, enemy, 0.58f, createLeapSkill());
        controller.update(0.15f, playerPos, 0.65f, resolver, null);
        assertTrue(playerPos.y > 0f);
        controller.cancel(playerPos);
        assertFalse(controller.isLeaping());
        assertEquals(0f, playerPos.y, 1e-4f);
    }
}
