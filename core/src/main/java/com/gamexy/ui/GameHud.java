package com.gamexy.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.gamexy.character.Enemy;
import com.gamexy.character.PlayerCharacter;
import com.gamexy.settings.GameSettings;
import com.gamexy.targeting.Targetable;
import com.gamexy.ui.hud.ActionToolbarRenderer;
import com.gamexy.ui.hud.HudLayout;
import com.gamexy.ui.hud.ResourceOrbRenderer;
import com.gamexy.ui.hud.TargetUnitFrameRenderer;
import com.gamexy.ui.hud.WorldOverlayRenderer;

import java.util.List;

/**
 * Resolution- and user-scaled Diablo-style Action RPG HUD coordinator.
 * Runs 3 batched render passes (Filled, Line, SpriteBatch) across modular HUD widget renderers.
 */
public class GameHud implements Disposable {

    public static final int TOOLBAR_SLOTS = HudLayout.TOOLBAR_SLOTS;

    private final UiFonts fonts;
    private final OrthographicCamera uiCam = new OrthographicCamera();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private final HudLayout layout;
    private final WorldOverlayRenderer worldOverlayRenderer = new WorldOverlayRenderer();
    private final ResourceOrbRenderer orbRenderer = new ResourceOrbRenderer();
    private final ActionToolbarRenderer toolbarRenderer = new ActionToolbarRenderer();
    private final TargetUnitFrameRenderer targetFrameRenderer = new TargetUnitFrameRenderer();

    public GameHud(UiFonts fonts) {
        this(fonts, GameSettings.DEFAULT_UI_SCALE);
    }

    public GameHud(UiFonts fonts, float userUiScale) {
        this.fonts = fonts;
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        this.layout = new HudLayout(userUiScale, w, h);
        updateCamera(w, h);
    }

    public void setUiScale(float userUiScale) {
        layout.setUiScale(userUiScale);
    }

    public void resize(int width, int height) {
        layout.resize(width, height);
        updateCamera(layout.getScreenW(), layout.getScreenH());
    }

    private void updateCamera(int width, int height) {
        uiCam.setToOrtho(false, Math.max(1, width), Math.max(1, height));
        uiCam.update();
    }

    public int getClickedSkillSlot(int screenX, int screenY) {
        return layout.getClickedSkillSlot(screenX, screenY);
    }

    public boolean containsPoint(int screenX, int screenY) {
        return layout.containsPoint(screenX, screenY);
    }

    public void updateHover(int screenX, int screenY) {
        layout.updateHover(screenX, screenY);
    }

    public void render(PlayerCharacter player, List<Enemy> enemies,
                       List<FloatingText> floatingTexts, PerspectiveCamera worldCam,
                       int waveNumber, Targetable selectedTarget) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(uiCam.combined);
        batch.setProjectionMatrix(uiCam.combined);

        targetFrameRenderer.prepareFrame(layout, selectedTarget);
        toolbarRenderer.prepareFrame(layout, player);

        renderFilledPass(player, enemies, worldCam, selectedTarget);
        renderBorderPass(player);
        renderTextPass(player, floatingTexts, worldCam, waveNumber, selectedTarget);
    }

    private void renderFilledPass(PlayerCharacter player, List<Enemy> enemies,
                                  PerspectiveCamera worldCam, Targetable selectedTarget) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        worldOverlayRenderer.renderEnemyHealthBars(shapes, layout, enemies, worldCam, selectedTarget);
        targetFrameRenderer.renderFilled(shapes, layout, selectedTarget);
        toolbarRenderer.renderFilled(shapes, layout, player);
        orbRenderer.renderFilled(shapes, layout, player);

        if (!player.isAlive()) {
            float uiScale = layout.getUiScale();
            shapes.setColor(0.18f, 0.02f, 0.02f, 0.80f);
            shapes.rect(0f, layout.getScreenH() * 0.5f - 74f * uiScale, layout.getScreenW(), 148f * uiScale);
        }
        shapes.end();
    }

    private void renderBorderPass(PlayerCharacter player) {
        shapes.begin(ShapeRenderer.ShapeType.Line);
        toolbarRenderer.renderBorders(shapes, layout, player);
        orbRenderer.renderBorders(shapes, layout);
        targetFrameRenderer.renderBorders(shapes, layout);
        shapes.end();
    }

    private void renderTextPass(PlayerCharacter player, List<FloatingText> floatingTexts,
                                PerspectiveCamera worldCam, int waveNumber, Targetable selectedTarget) {
        float previewRatio = Math.max(0.05f, layout.getUiScale() / Math.max(0.01f, fonts.getBuiltScale()));
        float hudTextScale = Math.max(0.05f, previewRatio * layout.getHudFitRatio());

        fonts.headerSerif.getData().setScale(hudTextScale);
        fonts.orbValueSans.getData().setScale(hudTextScale);
        fonts.hudMediumSans.getData().setScale(hudTextScale);
        fonts.hudSmallSans.getData().setScale(hudTextScale);
        fonts.bannerSerif.getData().setScale(previewRatio);

        try {
            batch.begin();
            toolbarRenderer.renderText(batch, fonts, glyphLayout, layout, player, waveNumber);
            orbRenderer.renderText(batch, fonts, glyphLayout, layout, player);
            worldOverlayRenderer.renderFloatingTexts(batch, fonts, glyphLayout, floatingTexts, worldCam, previewRatio);
            targetFrameRenderer.renderText(batch, fonts, glyphLayout, layout, selectedTarget, previewRatio, hudTextScale);

            if (!player.isAlive()) {
                renderDeathBannerText(previewRatio);
            }
            batch.end();
        } finally {
            fonts.headerSerif.getData().setScale(1.0f);
            fonts.orbValueSans.getData().setScale(1.0f);
            fonts.hudMediumSans.getData().setScale(1.0f);
            fonts.hudSmallSans.getData().setScale(1.0f);
            fonts.bannerSerif.getData().setScale(1.0f);
            fonts.combatNumbersSans.getData().setScale(1.0f);
        }
    }

    private void renderDeathBannerText(float previewRatio) {
        float uiScale = layout.getUiScale();
        float cx = layout.getScreenW() * 0.5f;
        float cy = layout.getScreenH() * 0.5f;
        fonts.hudMediumSans.getData().setScale(previewRatio);
        drawCentered(fonts.bannerSerif, "YOU HAVE FALLEN", cx, cy + 26f * uiScale, 0.98f, 0.24f, 0.20f, 1f);
        drawCentered(fonts.hudMediumSans, "Press R to Respawn in the Courtyard", cx, cy - 30f * uiScale, 1f, 1f, 1f, 1f);
    }

    private void drawCentered(BitmapFont font, String text, float cx, float cy, float r, float g, float b, float a) {
        font.setColor(r, g, b, a);
        glyphLayout.setText(font, text);
        font.draw(batch, text, cx - glyphLayout.width * 0.5f, cy + glyphLayout.height * 0.5f);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
    }
}
