package com.gamexy.targeting;

import com.badlogic.gdx.math.Vector3;
import com.gamexy.math.AngleMath;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cycles through nearby living enemies within {@link #TAB_CYCLE_RANGE} in distance order,
 * remembering recently visited targets so repeated TAB presses cover all nearby foes.
 */
public class EnemyTabCycler<T extends Targetable> {

    public static final float TAB_CYCLE_RANGE = 18.0f;
    public static final float TAB_MEMORY_TIMEOUT = 2.6f;

    private final List<T> candidates = new ArrayList<>();
    private final Set<T> visited = new HashSet<>();
    private float timeSinceLastTab = 0f;

    public void update(float delta) {
        timeSinceLastTab += delta;
        if (timeSinceLastTab > TAB_MEMORY_TIMEOUT && !visited.isEmpty()) {
            visited.clear();
        }
    }

    public void recordManualSelection(T target) {
        visited.clear();
        if (target != null) {
            visited.add(target);
        }
        timeSinceLastTab = 0f;
    }

    public void reset() {
        visited.clear();
        timeSinceLastTab = 0f;
    }

    /**
     * Selects the next nearby targetable enemy (or previous when {@code reverse} is true).
     */
    public T cycleNext(Vector3 playerPos, List<T> enemies, T currentSelection, boolean reverse) {
        collectSortedCandidates(playerPos, enemies);
        if (candidates.isEmpty()) {
            return null;
        }

        if (timeSinceLastTab > TAB_MEMORY_TIMEOUT) {
            visited.clear();
        }
        timeSinceLastTab = 0f;

        if (currentSelection == null || !candidates.contains(currentSelection)) {
            T first = reverse ? candidates.get(candidates.size() - 1) : candidates.get(0);
            resetVisitedTo(first);
            return first;
        }

        if (candidates.size() == 1) {
            T sole = candidates.get(0);
            resetVisitedTo(sole);
            return sole;
        }

        visited.retainAll(candidates);
        visited.add(currentSelection);

        T next = findNextUnvisited(reverse);
        if (next == null) {
            resetVisitedTo(currentSelection);
            next = findNextUnvisited(reverse);
        }
        if (next != null) {
            visited.add(next);
        }
        return next;
    }

    private void collectSortedCandidates(Vector3 playerPos, List<T> enemies) {
        candidates.clear();
        if (enemies == null) {
            return;
        }
        float maxDist2 = TAB_CYCLE_RANGE * TAB_CYCLE_RANGE;
        for (T enemy : enemies) {
            if (enemy != null && enemy.isTargetable()
                && AngleMath.distanceSquaredXZ(playerPos, enemy.getWorldPosition()) <= maxDist2) {
                candidates.add(enemy);
            }
        }
        candidates.sort(Comparator
            .comparingDouble((T e) -> AngleMath.distanceSquaredXZ(playerPos, e.getWorldPosition()))
            .thenComparingInt(System::identityHashCode));
    }

    private void resetVisitedTo(T target) {
        visited.clear();
        visited.add(target);
    }

    private T findNextUnvisited(boolean reverse) {
        if (!reverse) {
            for (int i = 0; i < candidates.size(); i++) {
                T candidate = candidates.get(i);
                if (!visited.contains(candidate)) {
                    return candidate;
                }
            }
        } else {
            for (int i = candidates.size() - 1; i >= 0; i--) {
                T candidate = candidates.get(i);
                if (!visited.contains(candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }
}
