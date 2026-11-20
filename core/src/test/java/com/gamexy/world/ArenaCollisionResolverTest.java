package com.gamexy.world;

import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArenaCollisionResolverTest {

    @Test
    void clampsPositionInsideArenaBounds() {
        ArenaCollisionResolver resolver = new ArenaCollisionResolver(20f, Collections.emptyList());
        Vector3 pos = new Vector3(50f, 0f, -40f);

        resolver.resolve(pos, 0.8f);

        assertEquals(18.0f, pos.x, 1e-4f);
        assertEquals(-18.0f, pos.z, 1e-4f);
    }

    @Test
    void pushesEntityOutOfOverlappingObstacle() {
        Obstacle pillar = new Obstacle(Obstacle.Kind.PILLAR, null, 0f, 0f, 1.0f);
        ArenaCollisionResolver resolver = new ArenaCollisionResolver(20f, Collections.singletonList(pillar));
        Vector3 pos = new Vector3(0.5f, 0f, 0f);

        resolver.resolve(pos, 0.7f);

        assertTrue(pos.x >= 1.699f, "Expected entity to be pushed out to combined radius (1.7)");
        assertEquals(0f, pos.z, 1e-4f);
    }
}
