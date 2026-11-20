package com.gamexy.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.assets.GameAssets;
import com.gamexy.render.PbrSceneContext;
import net.mgsx.gltf.scene3d.lights.PointLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructs the static gothic dungeon courtyard environment (floor tiles, pillars, braziers, crates).
 */
public final class CourtyardBuilder {

    public static final class BuiltCourtyard {
        public final List<Obstacle> obstacles;
        public final List<PointLightEx> brazierLights;
        public final List<Float> brazierBaseIntensities;

        BuiltCourtyard(List<Obstacle> obstacles, List<PointLightEx> brazierLights, List<Float> brazierBaseIntensities) {
            this.obstacles = obstacles;
            this.brazierLights = brazierLights;
            this.brazierBaseIntensities = brazierBaseIntensities;
        }
    }

    private static final float BRAZIER_LIGHT_INTENSITY = 18f;

    private final GameAssets assets;
    private final PbrSceneContext sceneContext;
    private final float arenaHalfSize;

    public CourtyardBuilder(GameAssets assets, PbrSceneContext sceneContext, float arenaHalfSize) {
        this.assets = assets;
        this.sceneContext = sceneContext;
        this.arenaHalfSize = arenaHalfSize;
    }

    public BuiltCourtyard build() {
        List<Obstacle> obstacles = new ArrayList<>();
        List<PointLightEx> brazierLights = new ArrayList<>();
        List<Float> brazierBaseIntensities = new ArrayList<>();

        buildFloorTiles();
        buildPillars(obstacles);
        buildBraziers(obstacles, brazierLights, brazierBaseIntensities);
        buildCrates(obstacles);

        return new BuiltCourtyard(obstacles, brazierLights, brazierBaseIntensities);
    }

    private void buildFloorTiles() {
        for (int gx = -5; gx <= 5; gx++) {
            for (int gz = -5; gz <= 5; gz++) {
                Scene tile = assets.createScene(assets.floorTile);
                tile.modelInstance.transform.idt().translate(gx * 4f, 0f, gz * 4f);
                sceneContext.addScene(tile);
            }
        }
    }

    private void buildPillars(List<Obstacle> obstacles) {
        for (int i = -4; i <= 4; i += 2) {
            addPillar(obstacles, i * 4.5f, -arenaHalfSize);
            addPillar(obstacles, i * 4.5f,  arenaHalfSize);
            if (i != -4 && i != 4) {
                addPillar(obstacles, -arenaHalfSize, i * 4.5f);
                addPillar(obstacles,  arenaHalfSize, i * 4.5f);
            }
        }

        addPillar(obstacles, -8.5f, -8.5f);
        addPillar(obstacles,  8.5f, -8.5f);
        addPillar(obstacles, -8.5f,  8.5f);
        addPillar(obstacles,  8.5f,  8.5f);
    }

    private void buildBraziers(List<Obstacle> obstacles,
                               List<PointLightEx> lights,
                               List<Float> baseIntensities) {
        addBrazier(obstacles, lights, baseIntensities, -5.5f, -5.5f);
        addBrazier(obstacles, lights, baseIntensities,  5.5f, -5.5f);
        addBrazier(obstacles, lights, baseIntensities, -5.5f,  5.5f);
        addBrazier(obstacles, lights, baseIntensities,  5.5f,  5.5f);
        addBrazier(obstacles, lights, baseIntensities,  0f,  -14.5f);
        addBrazier(obstacles, lights, baseIntensities,  0f,   14.5f);
    }

    private void buildCrates(List<Obstacle> obstacles) {
        addCrate(obstacles, -13.0f,  -4.5f,  18f);
        addCrate(obstacles, -14.2f,  -3.2f,  45f);
        addCrate(obstacles,  13.5f,   5.0f, -22f);
        addCrate(obstacles,  12.2f, -11.5f,  30f);
        addCrate(obstacles, -11.5f,  12.5f,  12f);
    }

    private void addPillar(List<Obstacle> obstacles, float x, float z) {
        Scene s = assets.createScene(assets.pillar);
        s.modelInstance.transform.idt().translate(x, 0f, z);
        sceneContext.addScene(s);
        obstacles.add(new Obstacle(Obstacle.Kind.PILLAR, s, x, z, 0.95f));
    }

    private void addBrazier(List<Obstacle> obstacles,
                            List<PointLightEx> lights,
                            List<Float> baseIntensities,
                            float x, float z) {
        Scene s = assets.createScene(assets.brazier);
        s.modelInstance.transform.idt().translate(x, 0f, z);
        sceneContext.addScene(s);
        obstacles.add(new Obstacle(Obstacle.Kind.BRAZIER, s, x, z, 0.72f));

        PointLightEx light = new PointLightEx();
        light.set(new Color(1.0f, 0.48f, 0.16f, 1f), x, 2.2f, z, BRAZIER_LIGHT_INTENSITY);
        sceneContext.addLight(light);
        lights.add(light);
        baseIntensities.add(BRAZIER_LIGHT_INTENSITY);
    }

    private void addCrate(List<Obstacle> obstacles, float x, float z, float rotY) {
        Scene s = assets.createScene(assets.crate);
        s.modelInstance.transform.idt().translate(x, 0f, z).rotate(Vector3.Y, rotY);
        sceneContext.addScene(s);
        obstacles.add(new Obstacle(Obstacle.Kind.CRATE, s, x, z, 0.75f));
    }
}
