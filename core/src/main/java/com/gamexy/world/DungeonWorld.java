package com.gamexy.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.assets.GameAssets;
import com.gamexy.character.Enemy;
import com.gamexy.render.PbrSceneContext;
import com.gamexy.targeting.Targetable;
import net.mgsx.gltf.scene3d.lights.PointLightEx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Coordinates the gothic dungeon courtyard environment, brazier flicker,
 * collision resolution, NPC registry, and enemy wave lifecycle.
 */
public class DungeonWorld {

    public static final float ARENA_HALF_SIZE = 20f;

    private final List<Obstacle> obstacles;
    private final List<Obstacle> obstaclesView;
    private final List<Targetable> npcs = new ArrayList<>();
    private final List<Targetable> npcsView = Collections.unmodifiableList(npcs);
    private final List<PointLightEx> brazierLights;
    private final List<Float> brazierBaseIntensities;
    private final ArenaCollisionResolver collisionResolver;
    private final EnemyWaveManager waveManager;

    private float flickerTime = 0f;

    public DungeonWorld(GameAssets assets, PbrSceneContext sceneContext) {
        CourtyardBuilder.BuiltCourtyard built = new CourtyardBuilder(assets, sceneContext, ARENA_HALF_SIZE).build();
        this.obstacles = built.obstacles;
        this.obstaclesView = Collections.unmodifiableList(this.obstacles);
        this.brazierLights = built.brazierLights;
        this.brazierBaseIntensities = built.brazierBaseIntensities;
        this.collisionResolver = new ArenaCollisionResolver(ARENA_HALF_SIZE, this.obstacles);
        this.waveManager = new EnemyWaveManager(assets, sceneContext, this.collisionResolver, ARENA_HALF_SIZE);
        this.waveManager.spawnInitialPack();
    }

    public List<Obstacle> getObstacles() {
        return obstaclesView;
    }

    public List<Enemy> getEnemies() {
        return waveManager.getEnemies();
    }

    public List<Targetable> getNpcs() {
        return npcsView;
    }

    public int getWaveNumber() {
        return waveManager.getWaveNumber();
    }

    public ArenaCollisionResolver getCollisionResolver() {
        return collisionResolver;
    }

    public void resolveCollision(Vector3 pos, float radius) {
        collisionResolver.resolve(pos, radius);
    }

    public void update(float delta, Vector3 playerPos, int playerLevel) {
        updateBrazierFlicker(delta);
        waveManager.update(delta, playerPos, playerLevel);
    }

    public void reset() {
        flickerTime = 0f;
        waveManager.reset();
    }

    private void updateBrazierFlicker(float delta) {
        flickerTime += delta;
        for (int i = 0; i < brazierLights.size(); i++) {
            float base = brazierBaseIntensities.get(i);
            float flicker = MathUtils.sin(flickerTime * 7.5f + i * 2.1f) * 2.2f
                          + MathUtils.cos(flickerTime * 13.3f + i * 1.4f) * 1.1f;
            brazierLights.get(i).intensity = base + flicker;
        }
    }
}
