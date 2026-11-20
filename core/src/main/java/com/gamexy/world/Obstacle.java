package com.gamexy.world;

import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.scene3d.scene.Scene;

/**
 * Static dungeon prop backed by a .glb Scene with circular collision footprint.
 */
public class Obstacle {
    public final Scene scene;
    public final Vector3 position;
    public final float radius;

    public Obstacle(Scene scene, float x, float z, float radius) {
        this.scene = scene;
        this.position = new Vector3(x, 0f, z);
        this.radius = radius;
    }
}
