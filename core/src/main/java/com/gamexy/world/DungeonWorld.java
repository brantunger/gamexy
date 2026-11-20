package com.gamexy.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.assets.GameAssets;
import com.gamexy.character.Enemy;
import net.mgsx.gltf.scene3d.lights.PointLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Builds and manages the gothic dungeon courtyard environment, static .glb props,
 * flickering brazier lights, collision resolution, and enemy waves.
 */
public class DungeonWorld {

    public static final float ARENA_HALF_SIZE = 20f;

    private final GameAssets assets;
    private final SceneManager sceneManager;

    public final List<Obstacle> obstacles = new ArrayList<>();
    public final List<Enemy> enemies = new ArrayList<>();
    private final List<PointLightEx> brazierLights = new ArrayList<>();
    private final List<Float> brazierBaseIntensity = new ArrayList<>();

    private float flickerTime = 0f;
    private float respawnTimer = 0f;
    private int waveNumber = 1;

    public DungeonWorld(GameAssets assets, SceneManager sceneManager) {
        this.assets = assets;
        this.sceneManager = sceneManager;
        buildEnvironment();
        spawnInitialEnemies();
    }

    private void buildEnvironment() {
        // 11 x 11 grid of 4x4 stone floor tiles => 44x44 tiled courtyard
        for (int gx = -5; gx <= 5; gx++) {
            for (int gz = -5; gz <= 5; gz++) {
                Scene tile = assets.createScene(assets.floorTile);
                tile.modelInstance.transform.idt().translate(gx * 4f, 0f, gz * 4f);
                sceneManager.addScene(tile);
            }
        }

        // Perimeter gothic stone pillars framing the courtyard
        for (int i = -4; i <= 4; i += 2) {
            addPillar(i * 4.5f, -ARENA_HALF_SIZE);
            addPillar(i * 4.5f,  ARENA_HALF_SIZE);
            if (i != -4 && i != 4) {
                addPillar(-ARENA_HALF_SIZE, i * 4.5f);
                addPillar( ARENA_HALF_SIZE, i * 4.5f);
            }
        }

        // Inner tactical pillars
        addPillar(-8.5f, -8.5f);
        addPillar( 8.5f, -8.5f);
        addPillar(-8.5f,  8.5f);
        addPillar( 8.5f,  8.5f);

        // Glowing fire braziers with warm point lights
        addBrazier(-5.5f, -5.5f);
        addBrazier( 5.5f, -5.5f);
        addBrazier(-5.5f,  5.5f);
        addBrazier( 5.5f,  5.5f);
        addBrazier(0f, -14.5f);
        addBrazier(0f,  14.5f);

        // Scattered dungeon crates for cover/atmosphere
        addCrate(-13.0f, -4.5f, 18f);
        addCrate(-14.2f, -3.2f, 45f);
        addCrate( 13.5f,  5.0f, -22f);
        addCrate( 12.2f, -11.5f, 30f);
        addCrate(-11.5f, 12.5f, 12f);
    }

    private void addPillar(float x, float z) {
        Scene s = assets.createScene(assets.pillar);
        s.modelInstance.transform.idt().translate(x, 0f, z);
        sceneManager.addScene(s);
        obstacles.add(new Obstacle(s, x, z, 0.95f));
    }

    private void addBrazier(float x, float z) {
        Scene s = assets.createScene(assets.brazier);
        s.modelInstance.transform.idt().translate(x, 0f, z);
        sceneManager.addScene(s);
        obstacles.add(new Obstacle(s, x, z, 0.72f));

        PointLightEx light = new PointLightEx();
        light.set(new Color(1.0f, 0.48f, 0.16f, 1f), x, 2.2f, z, 18f);
        sceneManager.environment.add(light);
        brazierLights.add(light);
        brazierBaseIntensity.add(18f);
    }

    private void addCrate(float x, float z, float rotY) {
        Scene s = assets.createScene(assets.crate);
        s.modelInstance.transform.idt().translate(x, 0f, z).rotate(Vector3.Y, rotY);
        sceneManager.addScene(s);
        obstacles.add(new Obstacle(s, x, z, 0.75f));
    }

    private void spawnInitialEnemies() {
        spawnEnemy(Enemy.Type.SKELETON, -7f, -12f, 1);
        spawnEnemy(Enemy.Type.SKELETON, -5f, -13f, 1);
        spawnEnemy(Enemy.Type.SKELETON,  8f, -11f, 1);
        spawnEnemy(Enemy.Type.SKELETON, 12f,  2f, 1);
        spawnEnemy(Enemy.Type.SKELETON, -12f, 6f, 1);
        spawnEnemy(Enemy.Type.SKELETON,  4f, 13f, 1);
        spawnEnemy(Enemy.Type.BRUTE, 0f, -16f, 1);
    }

    public Enemy spawnEnemy(Enemy.Type type, float x, float z, int playerLevel) {
        Scene s = assets.createScene(type == Enemy.Type.BRUTE ? assets.brute : assets.skeleton);
        Enemy e = new Enemy(type, s, x, z, playerLevel);
        resolveCollision(e.position, e.type.radius);
        sceneManager.addScene(s);
        enemies.add(e);
        return e;
    }

    public void update(float delta, Vector3 playerPos, int playerLevel) {
        // Subtle brazier fire flicker
        flickerTime += delta;
        for (int i = 0; i < brazierLights.size(); i++) {
            float base = brazierBaseIntensity.get(i);
            float flicker = MathUtils.sin(flickerTime * 7.5f + i * 2.1f) * 2.2f
                          + MathUtils.cos(flickerTime * 13.3f + i * 1.4f) * 1.1f;
            brazierLights.get(i).intensity = base + flicker;
        }

        // Remove expired dead enemies
        int aliveCount = 0;
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (e.isReadyForRemoval()) {
                sceneManager.removeScene(e.scene);
                it.remove();
            } else if (e.isAlive()) {
                aliveCount++;
            }
        }

        // Spawn reinforcement waves when courtyard gets low on enemies
        if (aliveCount < 4) {
            respawnTimer += delta;
            if (respawnTimer >= 3.2f) {
                respawnTimer = 0f;
                waveNumber++;
                spawnReinforcementPack(playerPos, playerLevel);
            }
        } else {
            respawnTimer = 0f;
        }
    }

    private void spawnReinforcementPack(Vector3 playerPos, int playerLevel) {
        int skelCount = 3 + Math.min(3, waveNumber / 2);
        for (int i = 0; i < skelCount; i++) {
            Vector3 pt = pickSpawnPointFarFrom(playerPos, 11f);
            spawnEnemy(Enemy.Type.SKELETON, pt.x, pt.z, playerLevel);
        }
        if (waveNumber % 2 == 0) {
            Vector3 pt = pickSpawnPointFarFrom(playerPos, 13f);
            spawnEnemy(Enemy.Type.BRUTE, pt.x, pt.z, playerLevel);
        }
    }

    private Vector3 pickSpawnPointFarFrom(Vector3 avoid, float minDist) {
        Vector3 pt = new Vector3();
        for (int attempt = 0; attempt < 20; attempt++) {
            float x = MathUtils.random(-ARENA_HALF_SIZE + 3f, ARENA_HALF_SIZE - 3f);
            float z = MathUtils.random(-ARENA_HALF_SIZE + 3f, ARENA_HALF_SIZE - 3f);
            float dx = x - avoid.x;
            float dz = z - avoid.z;
            if (dx * dx + dz * dz >= minDist * minDist) {
                pt.set(x, 0f, z);
                return pt;
            }
        }
        pt.set(0f, 0f, -16f);
        return pt;
    }

    /**
     * Keeps an entity inside the arena bounds and pushes it out of static obstacles.
     */
    public void resolveCollision(Vector3 pos, float radius) {
        float limit = ARENA_HALF_SIZE - 1.2f - radius;
        pos.x = MathUtils.clamp(pos.x, -limit, limit);
        pos.z = MathUtils.clamp(pos.z, -limit, limit);

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

    public int getWaveNumber() {
        return waveNumber;
    }

    public void reset() {
        for (Enemy e : enemies) {
            sceneManager.removeScene(e.scene);
        }
        enemies.clear();
        waveNumber = 1;
        respawnTimer = 0f;
        flickerTime = 0f;
        spawnInitialEnemies();
    }
}
