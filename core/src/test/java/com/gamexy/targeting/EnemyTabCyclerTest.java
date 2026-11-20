package com.gamexy.targeting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class EnemyTabCyclerTest {

    private static final class StubEnemy implements Targetable {
        final Vector3 pos;
        boolean alive = true;

        StubEnemy(float x, float z) {
            this.pos = new Vector3(x, 0f, z);
        }

        @Override public Category getTargetCategory() { return Category.ENEMY; }
        @Override public String getDisplayName() { return "Stub"; }
        @Override public String getSubtitle() { return "Hostile"; }
        @Override public Vector3 getWorldPosition() { return pos; }
        @Override public float getSelectionRadius() { return 0.8f; }
        @Override public float getSelectionHeight() { return 2.0f; }
        @Override public boolean isTargetable() { return alive; }
        @Override public Color getTargetAccentColor() { return Color.RED; }
    }

    @Test
    void cyclesNearbyEnemiesInDistanceOrderAndIgnoresOutOfRangeOrDead() {
        EnemyTabCycler<StubEnemy> cycler = new EnemyTabCycler<>();
        Vector3 player = new Vector3(0f, 0f, 0f);

        StubEnemy near = new StubEnemy(3f, 0f);
        StubEnemy mid = new StubEnemy(8f, 0f);
        StubEnemy far = new StubEnemy(14f, 0f);
        StubEnemy outOfRange = new StubEnemy(25f, 0f);
        StubEnemy dead = new StubEnemy(2f, 0f);
        dead.alive = false;

        List<StubEnemy> enemies = Arrays.asList(far, outOfRange, dead, near, mid);

        StubEnemy first = cycler.cycleNext(player, enemies, null, false);
        assertSame(near, first);

        StubEnemy second = cycler.cycleNext(player, enemies, first, false);
        assertSame(mid, second);

        StubEnemy third = cycler.cycleNext(player, enemies, second, false);
        assertSame(far, third);

        // Full cycle wraps back to nearest
        StubEnemy fourth = cycler.cycleNext(player, enemies, third, false);
        assertSame(near, fourth);
    }

    @Test
    void reverseTabStartsFromFarthestInRange() {
        EnemyTabCycler<StubEnemy> cycler = new EnemyTabCycler<>();
        Vector3 player = new Vector3(0f, 0f, 0f);
        StubEnemy near = new StubEnemy(4f, 0f);
        StubEnemy far = new StubEnemy(12f, 0f);

        StubEnemy selected = cycler.cycleNext(player, Arrays.asList(near, far), null, true);
        assertSame(far, selected);

        StubEnemy next = cycler.cycleNext(player, Arrays.asList(near, far), selected, true);
        assertSame(near, next);
    }

    @Test
    void returnsNullWhenNoEnemiesInRange() {
        EnemyTabCycler<StubEnemy> cycler = new EnemyTabCycler<>();
        StubEnemy outOfRange = new StubEnemy(30f, 0f);
        assertNull(cycler.cycleNext(new Vector3(), Arrays.asList(outOfRange), null, false));
    }
}
