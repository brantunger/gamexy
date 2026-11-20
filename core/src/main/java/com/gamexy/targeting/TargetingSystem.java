package com.gamexy.targeting;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.character.Enemy;
import com.gamexy.math.AngleMath;
import com.gamexy.world.Obstacle;

import java.util.List;

/**
 * Coordinates active target selection and hover state, delegating ray picking to
 * {@link RayCapsulePicker}, TAB cycling to {@link EnemyTabCycler}, and ring drawing to {@link TargetRingRenderer}.
 */
public class TargetingSystem {

    private final RayCapsulePicker picker = new RayCapsulePicker();
    private final EnemyTabCycler<Enemy> tabCycler = new EnemyTabCycler<>();
    private final TargetRingRenderer ringRenderer = new TargetRingRenderer();

    private Targetable selectedTarget;
    private Targetable hoveredTarget;

    public Targetable getSelectedTarget() {
        return selectedTarget;
    }

    public Targetable getHoveredTarget() {
        return hoveredTarget;
    }

    public boolean hasSelection() {
        return selectedTarget != null && selectedTarget.isTargetable();
    }

    public Enemy getSelectedEnemy() {
        if (selectedTarget instanceof Enemy) {
            Enemy enemy = (Enemy) selectedTarget;
            if (enemy.isTargetable()) {
                return enemy;
            }
        }
        return null;
    }

    public void setSelectedTarget(Targetable target) {
        if (target != null && target.isTargetable()) {
            this.selectedTarget = target;
            if (target instanceof Enemy) {
                tabCycler.recordManualSelection((Enemy) target);
            }
        } else {
            clearSelection();
        }
    }

    public void clearSelection() {
        this.selectedTarget = null;
        tabCycler.reset();
    }

    public void setHoveredTarget(Targetable target) {
        this.hoveredTarget = (target != null && target.isTargetable()) ? target : null;
    }

    public void update(float delta, Vector3 playerPos, List<Enemy> enemies) {
        ringRenderer.update(delta);
        tabCycler.update(delta);

        if (selectedTarget != null && !selectedTarget.isTargetable()) {
            if (selectedTarget instanceof Enemy) {
                selectNextAvailableEnemy(playerPos, enemies);
            } else {
                clearSelection();
            }
        }
        if (hoveredTarget != null && !hoveredTarget.isTargetable()) {
            hoveredTarget = null;
        }
    }

    /**
     * Switches selection to the closest living enemy, or clears selection if none remain.
     */
    public Enemy selectNextAvailableEnemy(Vector3 playerPos, List<Enemy> enemies) {
        Enemy best = null;
        float bestDist2 = Float.MAX_VALUE;
        if (enemies != null) {
            for (Enemy e : enemies) {
                if (!e.isTargetable()) {
                    continue;
                }
                float dist2 = AngleMath.distanceSquaredXZ(playerPos, e.position);
                if (dist2 < bestDist2) {
                    bestDist2 = dist2;
                    best = e;
                }
            }
        }
        if (best != null) {
            setSelectedTarget(best);
        } else {
            clearSelection();
        }
        return best;
    }

    public Enemy cycleNearbyEnemy(Vector3 playerPos, List<Enemy> enemies, boolean reverse) {
        Enemy next = tabCycler.cycleNext(playerPos, enemies, getSelectedEnemy(), reverse);
        if (next != null) {
            selectedTarget = next;
        }
        return next;
    }

    public Targetable pickTarget(PerspectiveCamera camera, int screenX, int screenY,
                                 List<Enemy> enemies, List<Targetable> npcs, List<Obstacle> obstacles) {
        return picker.pickTarget(camera, screenX, screenY, enemies, npcs, obstacles);
    }

    public void renderGroundRings(ShapeRenderer sr) {
        ringRenderer.render(sr, hoveredTarget, selectedTarget);
    }
}
