package com.gamexy.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

/**
 * Loads and manages standalone .glb 3D model assets using gdx-gltf.
 */
public class GameAssets implements Disposable {

    public final SceneAsset barbarian;
    public final SceneAsset battleAxe;
    public final SceneAsset skeleton;
    public final SceneAsset brute;
    public final SceneAsset pillar;
    public final SceneAsset brazier;
    public final SceneAsset crate;
    public final SceneAsset floorTile;

    public GameAssets() {
        // GLBLoader is stateful in gdx-gltf (retains loaded scenes); create a fresh loader per file.
        this.barbarian = loadGlb("models/barbarian.glb");
        this.battleAxe = loadGlb("models/battle_axe.glb");
        this.skeleton = loadGlb("models/skeleton.glb");
        this.brute = loadGlb("models/brute.glb");
        this.pillar = loadGlb("models/pillar.glb");
        this.brazier = loadGlb("models/brazier.glb");
        this.crate = loadGlb("models/crate.glb");
        this.floorTile = loadGlb("models/floor_tile.glb");
    }

    private static SceneAsset loadGlb(String path) {
        return new GLBLoader().load(Gdx.files.internal(path));
    }

    public Scene createScene(SceneAsset asset) {
        return new Scene(asset.scene);
    }

    @Override
    public void dispose() {
        barbarian.dispose();
        battleAxe.dispose();
        skeleton.dispose();
        brute.dispose();
        pillar.dispose();
        brazier.dispose();
        crate.dispose();
        floorTile.dispose();
    }
}
