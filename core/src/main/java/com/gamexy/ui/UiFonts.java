package com.gamexy.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;
import com.gamexy.settings.GameSettings;

/**
 * Generates and manages resolution- and user-scaled native TrueType fonts via gdx-freetype:
 * - Cinzel Bold (Gothic RPG serif) for headers, titles, and banners
 * - Roboto Bold (clean high-contrast sans-serif) for numbers, skills, tooltips, and combat text
 */
public class UiFonts implements Disposable {

    public BitmapFont headerSerif;
    public BitmapFont titleSerif;
    public BitmapFont bannerSerif;

    public BitmapFont orbValueSans;
    public BitmapFont hudMediumSans;
    public BitmapFont hudSmallSans;
    public BitmapFont combatNumbersSans;

    private float userUiScale = GameSettings.DEFAULT_UI_SCALE;
    private float builtScale = -1f;

    public UiFonts() {
        this(GameSettings.DEFAULT_UI_SCALE);
    }

    public UiFonts(float userUiScale) {
        update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), userUiScale);
    }

    public void resize(int width, int height) {
        update(width, height, this.userUiScale);
    }

    public void update(int width, int height, float userUiScale) {
        this.userUiScale = GameSettings.snapUiScale(userUiScale);
        float effectiveScale = GameSettings.baseResolutionScale(width, height) * this.userUiScale;
        if (builtScale < 0f || Math.abs(effectiveScale - builtScale) >= 0.02f) {
            regenerateForScale(effectiveScale);
        }
    }

    public float getBuiltScale() {
        return builtScale > 0f ? builtScale : 1.0f;
    }

    private void regenerateForScale(float effectiveScale) {
        disposeFonts();
        this.builtScale = effectiveScale;

        // Scale font pixel sizes cleanly with resolution * user UI scale so text stays crisp inside HUD & menu containers
        float hudScale = effectiveScale * 0.80f;

        FreeTypeFontGenerator serifGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Cinzel-Bold.ttf"));
        FreeTypeFontGenerator sansGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Bold.ttf"));

        try {
            this.headerSerif = generate(serifGen, Math.round(18f * hudScale), 1.3f * effectiveScale, Math.max(1, Math.round(1.5f * effectiveScale)), true);
            this.titleSerif = generate(serifGen, Math.round(26f * hudScale), 1.7f * effectiveScale, Math.max(1, Math.round(2f * effectiveScale)), true);
            this.bannerSerif = generate(serifGen, Math.round(42f * hudScale), 2.4f * effectiveScale, Math.max(1, Math.round(3f * effectiveScale)), true);

            this.orbValueSans = generate(sansGen, Math.round(20f * hudScale), 1.6f * effectiveScale, Math.max(1, Math.round(2f * effectiveScale)), true);
            this.hudMediumSans = generate(sansGen, Math.round(15f * hudScale), 1.2f * effectiveScale, Math.max(1, Math.round(1.2f * effectiveScale)), true);
            this.hudSmallSans = generate(sansGen, Math.round(12.5f * hudScale), 1.0f * effectiveScale, Math.max(1, Math.round(effectiveScale)), true);
            this.combatNumbersSans = generate(sansGen, Math.round(22f * effectiveScale), 1.9f * effectiveScale, Math.max(1, Math.round(1.8f * effectiveScale)), false);
        } finally {
            serifGen.dispose();
            sansGen.dispose();
        }
    }

    private static BitmapFont generate(FreeTypeFontGenerator generator, int sizePx,
                                       float borderWidth, int shadowOffset, boolean integerPositions) {
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = Math.max(8, sizePx);
        param.color = Color.WHITE;
        param.borderWidth = Math.max(0.75f, borderWidth);
        param.borderColor = new Color(0.03f, 0.02f, 0.04f, 0.96f);
        param.borderStraight = false;
        param.shadowOffsetX = Math.max(1, shadowOffset);
        param.shadowOffsetY = Math.max(1, shadowOffset);
        param.shadowColor = new Color(0f, 0f, 0f, 0.72f);
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        param.hinting = FreeTypeFontGenerator.Hinting.Full;

        BitmapFont font = generator.generateFont(param);
        font.setUseIntegerPositions(integerPositions);
        return font;
    }

    private void disposeFonts() {
        if (headerSerif != null) headerSerif.dispose();
        if (titleSerif != null) titleSerif.dispose();
        if (bannerSerif != null) bannerSerif.dispose();
        if (orbValueSans != null) orbValueSans.dispose();
        if (hudMediumSans != null) hudMediumSans.dispose();
        if (hudSmallSans != null) hudSmallSans.dispose();
        if (combatNumbersSans != null) combatNumbersSans.dispose();
    }

    @Override
    public void dispose() {
        disposeFonts();
    }
}
