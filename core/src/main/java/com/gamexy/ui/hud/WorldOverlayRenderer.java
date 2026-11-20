package com.gamexy.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.character.Enemy;
import com.gamexy.targeting.Targetable;
import com.gamexy.ui.FloatingText;
import com.gamexy.ui.UiFonts;

import java.util.List;

/**
 * Projects 3D world entities onto screen space to render enemy health bars and floating combat numbers.
 */
public class WorldOverlayRenderer {

    private final Vector3 projected = new Vector3();

    /**
     * Renders world-projected enemy HP bars. Caller must have begun {@code shapes} in Filled mode.
     */
    public void renderEnemyHealthBars(ShapeRenderer shapes, HudLayout layout,
                                      List<Enemy> enemies, PerspectiveCamera worldCam,
                                      Targetable selectedTarget) {
        float uiScale = layout.getUiScale();
        float border = Math.max(1f, 1.5f * uiScale);

        for (Enemy e : enemies) {
            if (!e.isAlive()) {
                continue;
            }
            float barHeightY = (e.type == Enemy.Type.BRUTE) ? 2.95f : 2.25f;
            projected.set(e.position.x, barHeightY, e.position.z);
            worldCam.project(projected);
            if (projected.z < 0f || projected.z > 1f) {
                continue;
            }

            boolean isSelected = (e == selectedTarget);
            float w = ((e.type == Enemy.Type.BRUTE) ? 74f : 54f) * uiScale;
            float h = ((e.type == Enemy.Type.BRUTE) ? 10f : 8.5f) * uiScale;
            float x = projected.x - w * 0.5f;
            float y = projected.y;
            float ratio = MathUtils.clamp(e.getHp() / Math.max(1f, e.getMaxHp()), 0f, 1f);

            if (isSelected) {
                float selPad = border * 2.1f;
                shapes.setColor(1.0f, 0.88f, 0.34f, 0.96f);
                shapes.rect(x - selPad, y - selPad, w + selPad * 2f, h + selPad * 2f);
            }
            shapes.setColor(0.06f, 0.05f, 0.07f, 0.90f);
            shapes.rect(x - border, y - border, w + border * 2f, h + border * 2f);

            setBarFillColor(shapes, e);
            shapes.rect(x, y, w * ratio, h);
        }
    }

    private static void setBarFillColor(ShapeRenderer shapes, Enemy e) {
        if (e.isStunned()) {
            shapes.setColor(0.35f, 0.85f, 1.0f, 1f);
        } else if (e.isHostile()) {
            shapes.setColor(0.86f, 0.16f, 0.14f, 1f);
        } else {
            shapes.setColor(0.94f, 0.80f, 0.20f, 1f);
        }
    }

    /**
     * Renders floating combat numbers. Caller must have begun {@code batch}.
     */
    public void renderFloatingTexts(SpriteBatch batch, UiFonts fonts, GlyphLayout glyphLayout,
                                    List<FloatingText> floatingTexts, PerspectiveCamera worldCam,
                                    float previewRatio) {
        for (FloatingText ft : floatingTexts) {
            projected.set(ft.worldPos);
            worldCam.project(projected);
            if (projected.z >= 0f && projected.z <= 1f) {
                fonts.combatNumbersSans.getData().setScale(Math.max(0.05f, ft.scale * previewRatio));
                Color col = ft.color;
                fonts.combatNumbersSans.setColor(col.r, col.g, col.b, ft.getAlpha());
                glyphLayout.setText(fonts.combatNumbersSans, ft.text);
                fonts.combatNumbersSans.draw(batch, ft.text,
                    projected.x - glyphLayout.width * 0.5f,
                    projected.y + glyphLayout.height * 0.5f);
            }
        }
    }
}
