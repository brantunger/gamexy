package com.gamexy.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

/**
 * Encapsulates gdx-gltf PBR SceneManager setup, gothic moonlight, and IBL environment resources.
 */
public class PbrSceneContext implements Disposable {

    private final SceneManager sceneManager;
    private final Cubemap diffuseCubemap;
    private final Cubemap specularCubemap;
    private final Texture brdfLUT;

    public PbrSceneContext(Camera camera) {
        this.sceneManager = createSceneManager(camera);
        DirectionalLightEx moonLight = createMoonLight();
        this.sceneManager.environment.add(moonLight);

        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(moonLight);
        this.diffuseCubemap = iblBuilder.buildIrradianceMap(64);
        this.specularCubemap = iblBuilder.buildRadianceMap(6);
        iblBuilder.dispose();

        this.brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
        bindIblEnvironment();
    }

    private static SceneManager createSceneManager(Camera camera) {
        PBRShaderConfig pbrConfig = PBRShaderProvider.createDefaultConfig();
        pbrConfig.numDirectionalLights = 2;
        pbrConfig.numPointLights = 8;
        SceneManager manager = new SceneManager(
            PBRShaderProvider.createDefault(pbrConfig),
            PBRShaderProvider.createDefaultDepth(12));
        manager.setCamera(camera);
        manager.setAmbientLight(0.32f);
        return manager;
    }

    private static DirectionalLightEx createMoonLight() {
        DirectionalLightEx moonLight = new DirectionalLightEx();
        moonLight.direction.set(-0.45f, -1.0f, -0.35f).nor();
        moonLight.color.set(0.76f, 0.80f, 0.96f, 1f);
        moonLight.intensity = 2.6f;
        return moonLight;
    }

    private void bindIblEnvironment() {
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public void addScene(Scene scene) {
        sceneManager.addScene(scene);
    }

    public void removeScene(Scene scene) {
        sceneManager.removeScene(scene);
    }

    public void addLight(BaseLight<?> light) {
        sceneManager.environment.add(light);
    }

    public void update(float delta) {
        sceneManager.update(delta);
    }

    public void render() {
        sceneManager.render();
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
    }
}
