package com.gamexy.tools;

/**
 * Immutable PBR Metallic-Roughness material specification for glTF 2.0 export.
 */
public final class MaterialSpec {

    public final String name;
    public final float r, g, b, a;
    public final float metallic;
    public final float roughness;
    public final float er, eg, eb;

    public MaterialSpec(String name, float r, float g, float b, float metallic, float roughness) {
        this(name, r, g, b, 1f, metallic, roughness, 0f, 0f, 0f);
    }

    public MaterialSpec(String name, float r, float g, float b, float a,
                        float metallic, float roughness,
                        float er, float eg, float eb) {
        this.name = name;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.metallic = metallic;
        this.roughness = roughness;
        this.er = er;
        this.eg = eg;
        this.eb = eb;
    }

    public boolean hasEmission() {
        return er > 0f || eg > 0f || eb > 0f;
    }

    public boolean isTranslucent() {
        return a < 0.999f;
    }
}
