package com.gamexy.targeting;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import java.util.List;

/**
 * Performs 3D ray-capsule picking against enemies, NPCs, and static world objects.
 * Slightly favors characters (enemies/NPCs) over static obstacles when silhouettes overlap.
 */
public final class RayCapsulePicker {

    private static final float OBSTACLE_PRIORITY_MARGIN = 1.5f;

    private static final class PickResult {
        Targetable target;
        float distance = Float.MAX_VALUE;
    }

    public Targetable pickTarget(PerspectiveCamera camera,
                                 int screenX,
                                 int screenY,
                                 List<? extends Targetable> enemies,
                                 List<? extends Targetable> npcs,
                                 List<? extends Targetable> obstacles) {
        return pickTarget(camera.getPickRay(screenX, screenY), enemies, npcs, obstacles);
    }

    public Targetable pickTarget(Ray ray,
                                 List<? extends Targetable> enemies,
                                 List<? extends Targetable> npcs,
                                 List<? extends Targetable> obstacles) {
        PickResult bestCharacter = new PickResult();
        testCandidates(ray, enemies, bestCharacter);
        testCandidates(ray, npcs, bestCharacter);

        PickResult bestObject = new PickResult();
        testCandidates(ray, obstacles, bestObject);

        if (bestCharacter.target != null && bestObject.target != null) {
            return (bestObject.distance + OBSTACLE_PRIORITY_MARGIN < bestCharacter.distance)
                ? bestObject.target
                : bestCharacter.target;
        }
        return bestCharacter.target != null ? bestCharacter.target : bestObject.target;
    }

    private static void testCandidates(Ray ray, List<? extends Targetable> candidates, PickResult best) {
        if (candidates == null) {
            return;
        }
        for (Targetable candidate : candidates) {
            if (candidate == null || !candidate.isTargetable()) {
                continue;
            }
            float hitT = intersectRayCapsule(
                ray,
                candidate.getWorldPosition(),
                candidate.getSelectionRadius(),
                candidate.getSelectionHeight());
            if (hitT >= 0f && hitT < best.distance) {
                best.distance = hitT;
                best.target = candidate;
            }
        }
    }

    /**
     * Closest-distance test between a 3D ray and a vertical capsule centered at (pos.x, [0..height], pos.z).
     * Returns distance {@code t} along the ray on hit, or {@code -1f} on miss.
     */
    public static float intersectRayCapsule(Ray ray, Vector3 pos, float radius, float height) {
        float ox = ray.origin.x - pos.x;
        float oy = ray.origin.y;
        float oz = ray.origin.z - pos.z;

        float dx = ray.direction.x;
        float dy = ray.direction.y;
        float dz = ray.direction.z;

        float segMinY = Math.min(radius * 0.4f, height * 0.5f);
        float segMaxY = Math.max(segMinY, height - radius * 0.35f);

        float horizLen2 = dx * dx + dz * dz;
        float dDotW = dx * ox + dy * oy + dz * oz;

        float yClosest;
        if (horizLen2 > 1e-5f) {
            yClosest = MathUtils.clamp((oy - dy * dDotW) / horizLen2, segMinY, segMaxY);
        } else {
            yClosest = segMaxY;
        }

        float t = -(dx * ox + dy * (oy - yClosest) + dz * oz);
        if (t < 0f) {
            return -1f;
        }

        float px = ox + dx * t;
        float py = oy + dy * t - yClosest;
        float pz = oz + dz * t;
        float dist2 = px * px + py * py + pz * pz;

        return dist2 <= radius * radius ? t : -1f;
    }
}
