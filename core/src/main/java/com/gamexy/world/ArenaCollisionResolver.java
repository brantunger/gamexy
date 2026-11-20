package com.gamexy.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import java.util.List;

/**
 * Resolves entity movement against the courtyard perimeter bounds and static circular obstacles.
 */
public class ArenaCollisionResolver {

    private final float arenaHalfSize;
    private final List<Obstacle> obstacles;

    public ArenaCollisionResolver(float arenaHalfSize, List<Obstacle> obstacles) {
        this.arenaHalfSize = arenaHalfSize;
        this.obstacles = obstacles;
    }

    /**
     * Clamps {@code pos} inside the arena bounds and pushes it out of any overlapping static obstacles.
     */
    public void resolve(Vector3 pos, float radius) {
        clampToArenaBounds(pos, radius);
        pushOutOfObstacles(pos, radius);
    }

    private void clampToArenaBounds(Vector3 pos, float radius) {
        float limit = arenaHalfSize - 1.2f - radius;
        pos.x = MathUtils.clamp(pos.x, -limit, limit);
        pos.z = MathUtils.clamp(pos.z, -limit, limit);
    }

    private void pushOutOfObstacles(Vector3 pos, float radius) {
        for (Obstacle obs : obstacles) {
            float dx = pos.x - obs.position.x;
            float dz = pos.z - obs.position.z;
            float minDist = radius + obs.radius;
            float dist2 = dx * dx + dz * dz;
            if (dist2 < minDist * minDist) {
                float dist = (float) Math.sqrt(dist2);
                if (dist < 1e-4f) {
                    pos.x += minDist;
                } else {
                    float push = minDist - dist;
                    pos.x += (dx / dist) * push;
                    pos.z += (dz / dist) * push;
                }
            }
        }
    }
}
