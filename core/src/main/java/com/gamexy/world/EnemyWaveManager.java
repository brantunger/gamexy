package com.gamexy.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.assets.GameAssets;
import com.gamexy.character.Enemy;
import com.gamexy.render.PbrSceneContext;
import net.mgsx.gltf.scene3d.scene.Scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Manages courtyard enemy spawning, wave reinforcements, expired-corpse cleanup, and resets.
 */
public class EnemyWaveManager {

    private static final int REINFORCEMENT_THRESHOLD = 4;
    private static final float REINFORCEMENT_DELAY = 3.2f;

    private final GameAssets assets;
    private final PbrSceneContext sceneContext;
    private final ArenaCollisionResolver collisionResolver;
    private final float arenaHalfSize;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Enemy> enemiesView = Collections.unmodifiableList(enemies);

    private float respawnTimer = 0f;
    private int waveNumber = 1;

    public EnemyWaveManager(GameAssets assets,
                            PbrSceneContext sceneContext,
                            ArenaCollisionResolver collisionResolver,
                            float arenaHalfSize) {
        this.assets = assets;
        this.sceneContext = sceneContext;
        this.collisionResolver = collisionResolver;
        this.arenaHalfSize = arenaHalfSize;
    }

    public List<Enemy> getEnemies() {
        return enemiesView;
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public void spawnInitialPack() {
        spawnEnemy(Enemy.Type.SKELETON,  -7f, -12f, 1);
        spawnEnemy(Enemy.Type.SKELETON,  -5f, -13f, 1);
        spawnEnemy(Enemy.Type.SKELETON,   8f, -11f, 1);
        spawnEnemy(Enemy.Type.SKELETON,  12f,   2f, 1);
        spawnEnemy(Enemy.Type.SKELETON, -12f,   6f, 1);
        spawnEnemy(Enemy.Type.SKELETON,   4f,  13f, 1);
        spawnEnemy(Enemy.Type.BRUTE,      0f, -16f, 1);
    }

    public Enemy spawnEnemy(Enemy.Type type, float x, float z, int playerLevel) {
        Scene scene = assets.createScene(type == Enemy.Type.BRUTE ? assets.brute : assets.skeleton);
        Enemy enemy = new Enemy(type, scene, x, z, playerLevel);
        collisionResolver.resolve(enemy.position, enemy.type.radius);
        sceneContext.addScene(scene);
        enemies.add(enemy);
        return enemy;
    }

    public void update(float delta, Vector3 playerPos, int playerLevel) {
        int aliveCount = pruneExpiredAndCountAlive();
        updateReinforcementTimer(delta, aliveCount, playerPos, playerLevel);
    }

    public void reset() {
        for (Enemy e : enemies) {
            sceneContext.removeScene(e.scene);
        }
        enemies.clear();
        waveNumber = 1;
        respawnTimer = 0f;
        spawnInitialPack();
    }

    private int pruneExpiredAndCountAlive() {
        int aliveCount = 0;
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (e.isReadyForRemoval()) {
                sceneContext.removeScene(e.scene);
                it.remove();
            } else if (e.isAlive()) {
                aliveCount++;
            }
        }
        return aliveCount;
    }

    private void updateReinforcementTimer(float delta, int aliveCount, Vector3 playerPos, int playerLevel) {
        if (aliveCount < REINFORCEMENT_THRESHOLD) {
            respawnTimer += delta;
            if (respawnTimer >= REINFORCEMENT_DELAY) {
                respawnTimer = 0f;
                waveNumber++;
                spawnReinforcementPack(playerPos, playerLevel);
            }
        } else {
            respawnTimer = 0f;
        }
    }

    private void spawnReinforcementPack(Vector3 playerPos, int playerLevel) {
        int skeletonCount = 3 + Math.min(3, waveNumber / 2);
        for (int i = 0; i < skeletonCount; i++) {
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
        float minDist2 = minDist * minDist;
        for (int attempt = 0; attempt < 20; attempt++) {
            float x = MathUtils.random(-arenaHalfSize + 3f, arenaHalfSize - 3f);
            float z = MathUtils.random(-arenaHalfSize + 3f, arenaHalfSize - 3f);
            float dx = x - avoid.x;
            float dz = z - avoid.z;
            if (dx * dx + dz * dz >= minDist2) {
                pt.set(x, 0f, z);
                return pt;
            }
        }
        pt.set(0f, 0f, -16f);
        return pt;
    }
}
