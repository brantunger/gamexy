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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.gamexy.character.Enemy;
import com.gamexy.character.PlayerCharacter;
import com.gamexy.character.Skill;

import java.util.ArrayList;
import java.util.List;

/**
 * Diablo-style Action RPG HUD:
 * - Crimson Health Orb (left) and Amber Fury Orb (right)
 * - Bottom selectable skill toolbar (click or press 1-4 to select active LMB ability)
 * - XP progress bar & level badge
 * - World-projected enemy HP bars & floating combat numbers
 * - Hover tooltips and controls reference
 */
public class GameHud implements Disposable {

    private final OrthographicCamera uiCam = new OrthographicCamera();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();

    private final List<Rectangle> slotBounds = new ArrayList<>();
    private final Rectangle toolbarPanelBounds = new Rectangle();
    private final Vector3 projected = new Vector3();

    private int hoveredSlot = -1;
    private int screenW = 1280;
    private int screenH = 720;

    public GameHud() {
        font.getData().setScale(1.05f);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void resize(int width, int height) {
        this.screenW = Math.max(1, width);
        this.screenH = Math.max(1, height);
        uiCam.setToOrtho(false, screenW, screenH);
        uiCam.update();
        recomputeLayout(4);
    }

    private void recomputeLayout(int slotCount) {
        slotBounds.clear();
        float slotSize = 66f;
        float gap = 12f;
        float totalW = slotCount * slotSize + (slotCount - 1) * gap;
        float startX = (screenW - totalW) * 0.5f;
        float slotY = 34f;

        toolbarPanelBounds.set(startX - 18f, 10f, totalW + 36f, slotSize + 40f);
        for (int i = 0; i < slotCount; i++) {
            slotBounds.add(new Rectangle(startX + i * (slotSize + gap), slotY, slotSize, slotSize));
        }
    }

    /**
     * Handles a left-click in screen coordinates (Y-down).
     * Returns true if the click hit the bottom HUD (selecting a skill if clicked on a slot).
     */
    public boolean handleClick(int screenX, int screenY, PlayerCharacter player) {
        float uiY = screenH - screenY;
        for (int i = 0; i < slotBounds.size(); i++) {
            if (slotBounds.get(i).contains(screenX, uiY)) {
                player.selectSkill(i);
                return true;
            }
        }
        return toolbarPanelBounds.contains(screenX, uiY);
    }

    public void updateHover(int screenX, int screenY) {
        float uiY = screenH - screenY;
        hoveredSlot = -1;
        for (int i = 0; i < slotBounds.size(); i++) {
            if (slotBounds.get(i).contains(screenX, uiY)) {
                hoveredSlot = i;
                break;
            }
        }
    }

    public void render(PlayerCharacter player, List<Enemy> enemies,
                       List<FloatingText> floatingTexts, PerspectiveCamera worldCam, int waveNumber) {
        if (slotBounds.size() != player.skills.size()) {
            recomputeLayout(player.skills.size());
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(uiCam.combined);
        batch.setProjectionMatrix(uiCam.combined);

        // 1. World-projected enemy HP bars
        renderEnemyHealthBars(enemies, worldCam);

        // 2. Bottom Diablo-style HUD geometry
        float orbRadius = 56f;
        float leftOrbX = toolbarPanelBounds.x - orbRadius - 24f;
        float rightOrbX = toolbarPanelBounds.x + toolbarPanelBounds.width + orbRadius + 24f;
        float orbY = 68f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Dark gothic toolbar backing
        shapes.setColor(0.08f, 0.07f, 0.09f, 0.90f);
        shapes.rect(toolbarPanelBounds.x, toolbarPanelBounds.y, toolbarPanelBounds.width, toolbarPanelBounds.height);

        // XP bar along bottom of toolbar
        float xpBarX = toolbarPanelBounds.x + 12f;
        float xpBarY = 14f;
        float xpBarW = toolbarPanelBounds.width - 24f;
        float xpBarH = 10f;
        float xpRatio = MathUtils.clamp((float) player.xp / Math.max(1, player.xpToNextLevel), 0f, 1f);
        shapes.setColor(0.14f, 0.12f, 0.18f, 1f);
        shapes.rect(xpBarX, xpBarY, xpBarW, xpBarH);
        shapes.setColor(0.68f, 0.45f, 0.96f, 1f);
        shapes.rect(xpBarX, xpBarY, xpBarW * xpRatio, xpBarH);

        // Health Orb (Left)
        drawOrbFilled(leftOrbX, orbY, orbRadius,
            player.hp / Math.max(1f, player.maxHp),
            new Color(0.76f, 0.10f, 0.12f, 1f),
            new Color(0.22f, 0.05f, 0.06f, 0.94f));

        // Fury Orb (Right)
        drawOrbFilled(rightOrbX, orbY, orbRadius,
            player.fury / Math.max(1f, player.maxFury),
            new Color(0.92f, 0.46f, 0.08f, 1f),
            new Color(0.22f, 0.12f, 0.04f, 0.94f));

        // Skill slots
        for (int i = 0; i < player.skills.size(); i++) {
            Skill skill = player.skills.get(i);
            Rectangle r = slotBounds.get(i);
            boolean unlocked = player.isSkillUnlocked(i);
            boolean selected = (player.selectedSkillIndex == i);

            if (!unlocked) {
                shapes.setColor(0.11f, 0.11f, 0.13f, 0.92f);
            } else if (selected) {
                shapes.setColor(skill.accentColor.r * 0.48f, skill.accentColor.g * 0.48f, skill.accentColor.b * 0.48f, 0.96f);
            } else {
                shapes.setColor(skill.accentColor.r * 0.24f, skill.accentColor.g * 0.24f, skill.accentColor.b * 0.24f, 0.92f);
            }
            shapes.rect(r.x, r.y, r.width, r.height);

            // Inner accent stripe
            if (unlocked) {
                shapes.setColor(skill.accentColor.r, skill.accentColor.g, skill.accentColor.b, selected ? 0.95f : 0.55f);
                shapes.rect(r.x, r.y + r.height - 6f, r.width, 6f);
            }

            // Cooldown dark overlay
            if (unlocked && player.cooldowns[i] > 0f) {
                float cdRatio = MathUtils.clamp(player.cooldowns[i] / skill.cooldown, 0f, 1f);
                shapes.setColor(0f, 0f, 0f, 0.68f);
                shapes.rect(r.x, r.y, r.width, r.height * cdRatio);
            }

            // Insufficient fury tint
            if (unlocked && !player.hasEnoughFury(skill)) {
                shapes.setColor(0.05f, 0.05f, 0.12f, 0.42f);
                shapes.rect(r.x, r.y, r.width, r.height);
            }
        }

        // Top-left status/controls backdrop
        shapes.setColor(0.06f, 0.06f, 0.08f, 0.72f);
        shapes.rect(14f, screenH - 118f, 470f, 104f);

        // Tooltip backdrop when hovering a skill slot
        if (hoveredSlot >= 0 && hoveredSlot < player.skills.size()) {
            Rectangle r = slotBounds.get(hoveredSlot);
            float tipW = 310f;
            float tipH = 66f;
            float tipX = MathUtils.clamp(r.x + r.width * 0.5f - tipW * 0.5f, 12f, screenW - tipW - 12f);
            float tipY = r.y + r.height + 16f;
            shapes.setColor(0.07f, 0.06f, 0.09f, 0.94f);
            shapes.rect(tipX, tipY, tipW, tipH);
        }

        // Death overlay banner
        if (!player.isAlive()) {
            shapes.setColor(0.18f, 0.02f, 0.02f, 0.76f);
            shapes.rect(0f, screenH * 0.5f - 52f, screenW, 104f);
        }

        shapes.end();

        // Borders & outlines
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.45f, 0.36f, 0.24f, 0.95f);
        shapes.rect(toolbarPanelBounds.x, toolbarPanelBounds.y, toolbarPanelBounds.width, toolbarPanelBounds.height);
        shapes.circle(leftOrbX, orbY, orbRadius, 36);
        shapes.circle(rightOrbX, orbY, orbRadius, 36);

        for (int i = 0; i < player.skills.size(); i++) {
            Rectangle r = slotBounds.get(i);
            boolean selected = (player.selectedSkillIndex == i);
            if (selected) {
                shapes.setColor(1.0f, 0.84f, 0.26f, 1f);
                shapes.rect(r.x - 2f, r.y - 2f, r.width + 4f, r.height + 4f);
                shapes.rect(r.x - 1f, r.y - 1f, r.width + 2f, r.height + 2f);
            } else if (hoveredSlot == i) {
                shapes.setColor(0.85f, 0.85f, 0.90f, 0.9f);
                shapes.rect(r.x, r.y, r.width, r.height);
            } else {
                shapes.setColor(0.35f, 0.32f, 0.36f, 0.85f);
                shapes.rect(r.x, r.y, r.width, r.height);
            }
        }
        shapes.end();

        // 3. Text layer
        batch.begin();

        // Top-left hero stats & controls
        font.getData().setScale(1.1f);
        font.setColor(1.0f, 0.85f, 0.38f, 1f);
        font.draw(batch, String.format("LVL %d %s  |  Kills: %d  |  Wave %d",
            player.level, player.characterClass.displayName.toUpperCase(), player.kills, waveNumber),
            24f, screenH - 22f);

        font.getData().setScale(0.92f);
        font.setColor(0.84f, 0.84f, 0.88f, 1f);
        font.draw(batch, "WASD: Move & Face   |   Left-Click: Use Selected Skill", 24f, screenH - 48f);
        font.draw(batch, "1-4 or Click Bar: Select Skill   |   Scroll: Zoom", 24f, screenH - 70f);
        font.draw(batch, "Right-Click Drag / Alt+Drag: Orbit Camera   |   R: Respawn   |   Esc: Pause", 24f, screenH - 92f);

        if (player.isWarCryBuffActive()) {
            font.setColor(1.0f, 0.88f, 0.25f, 1f);
            font.draw(batch, String.format("WAR CRY ACTIVE (+35%% DMG) %.1fs", player.getWarCryBuffRemaining()),
                24f, screenH - 130f);
        }

        // Orb labels
        font.getData().setScale(0.95f);
        drawCentered(batch, "HP", leftOrbX, orbY + 20f, Color.WHITE);
        drawCentered(batch, Math.round(player.hp) + "/" + Math.round(player.maxHp), leftOrbX, orbY - 2f, Color.WHITE);

        drawCentered(batch, "FURY", rightOrbX, orbY + 20f, Color.WHITE);
        drawCentered(batch, Math.round(player.fury) + "/" + Math.round(player.maxFury), rightOrbX, orbY - 2f, Color.WHITE);

        // Skill slot text
        for (int i = 0; i < player.skills.size(); i++) {
            Skill skill = player.skills.get(i);
            Rectangle r = slotBounds.get(i);
            boolean unlocked = player.isSkillUnlocked(i);
            boolean selected = (player.selectedSkillIndex == i);

            font.getData().setScale(0.85f);
            font.setColor(0.88f, 0.86f, 0.78f, 1f);
            font.draw(batch, String.valueOf(i + 1), r.x + 5f, r.y + r.height - 9f);

            if (selected) {
                font.setColor(1.0f, 0.86f, 0.28f, 1f);
                font.draw(batch, "LMB", r.x + r.width - 31f, r.y + r.height - 9f);
            }

            if (!unlocked) {
                font.getData().setScale(0.90f);
                drawCentered(batch, "LV " + skill.unlockLevel, r.x + r.width * 0.5f, r.y + r.height * 0.52f, Color.GRAY);
            } else {
                font.getData().setScale(0.86f);
                drawCentered(batch, skill.shortLabel, r.x + r.width * 0.5f, r.y + r.height * 0.54f, Color.WHITE);

                font.getData().setScale(0.75f);
                if (player.cooldowns[i] > 0f) {
                    drawCentered(batch, String.format("%.1fs", player.cooldowns[i]),
                        r.x + r.width * 0.5f, r.y + 18f, new Color(1f, 0.7f, 0.4f, 1f));
                } else if (skill.furyCost > 0f) {
                    Color costCol = player.hasEnoughFury(skill) ? new Color(1f, 0.68f, 0.22f, 1f) : Color.SALMON;
                    drawCentered(batch, (int) skill.furyCost + " Fury", r.x + r.width * 0.5f, r.y + 18f, costCol);
                } else {
                    drawCentered(batch, "+" + (int) skill.furyGain + " Fury",
                        r.x + r.width * 0.5f, r.y + 18f, new Color(0.65f, 0.95f, 0.65f, 1f));
                }
            }
        }

        // XP label above bar
        font.getData().setScale(0.78f);
        drawCentered(batch, String.format("XP %d / %d", player.xp, player.xpToNextLevel),
            screenW * 0.5f, 24f, new Color(0.92f, 0.86f, 1.0f, 0.95f));

        // Hover tooltip text
        if (hoveredSlot >= 0 && hoveredSlot < player.skills.size()) {
            Skill s = player.skills.get(hoveredSlot);
            Rectangle r = slotBounds.get(hoveredSlot);
            float tipW = 310f;
            float tipX = MathUtils.clamp(r.x + r.width * 0.5f - tipW * 0.5f, 12f, screenW - tipW - 12f);
            float tipY = r.y + r.height + 16f;

            font.getData().setScale(0.95f);
            font.setColor(1.0f, 0.86f, 0.34f, 1f);
            String title = s.name + (player.isSkillUnlocked(hoveredSlot) ? "" : " (Unlocks at Lv " + s.unlockLevel + ")");
            font.draw(batch, title, tipX + 10f, tipY + 56f);

            font.getData().setScale(0.82f);
            font.setColor(0.88f, 0.88f, 0.92f, 1f);
            font.draw(batch, s.description, tipX + 10f, tipY + 34f, tipW - 20f, Align.left, true);
        }

        // Floating world combat texts
        for (FloatingText ft : floatingTexts) {
            projected.set(ft.worldPos);
            worldCam.project(projected);
            if (projected.z >= 0f && projected.z <= 1f) {
                font.getData().setScale(ft.scale);
                Color c = ft.color;
                font.setColor(c.r, c.g, c.b, ft.getAlpha());
                drawCentered(batch, ft.text, projected.x, projected.y, font.getColor());
            }
        }

        // Death screen text
        if (!player.isAlive()) {
            font.getData().setScale(2.0f);
            drawCentered(batch, "YOU HAVE FALLEN", screenW * 0.5f, screenH * 0.5f + 26f, new Color(0.95f, 0.22f, 0.18f, 1f));
            font.getData().setScale(1.1f);
            drawCentered(batch, "Press R to Respawn in the Courtyard", screenW * 0.5f, screenH * 0.5f - 14f, Color.WHITE);
        }

        font.getData().setScale(1.0f);
        batch.end();
    }

    private void renderEnemyHealthBars(List<Enemy> enemies, PerspectiveCamera worldCam) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            float barHeightY = (e.type == Enemy.Type.BRUTE) ? 2.95f : 2.25f;
            projected.set(e.position.x, barHeightY, e.position.z);
            worldCam.project(projected);
            if (projected.z < 0f || projected.z > 1f) continue;

            float w = (e.type == Enemy.Type.BRUTE) ? 68f : 48f;
            float h = 7f;
            float x = projected.x - w * 0.5f;
            float y = projected.y;
            float ratio = MathUtils.clamp(e.hp / Math.max(1f, e.maxHp), 0f, 1f);

            shapes.setColor(0.08f, 0.06f, 0.08f, 0.85f);
            shapes.rect(x - 1f, y - 1f, w + 2f, h + 2f);
            shapes.setColor(e.isStunned() ? Color.GOLD : new Color(0.84f, 0.16f, 0.14f, 1f));
            shapes.rect(x, y, w * ratio, h);
        }
        shapes.end();
    }

    private void drawOrbFilled(float cx, float cy, float radius, float ratio, Color fill, Color bg) {
        ratio = MathUtils.clamp(ratio, 0f, 1f);
        shapes.setColor(bg);
        shapes.circle(cx, cy, radius, 36);

        if (ratio > 0.001f) {
            shapes.setColor(fill);
            int slices = 28;
            float maxY = cy - radius + radius * 2f * ratio;
            float step = (radius * 2f) / slices;
            for (int i = 0; i < slices; i++) {
                float y0 = cy - radius + i * step;
                if (y0 >= maxY) break;
                float y1 = Math.min(maxY, y0 + step);
                float ym = (y0 + y1) * 0.5f;
                float dy = ym - cy;
                float halfW = (float) Math.sqrt(Math.max(0f, radius * radius - dy * dy));
                shapes.rect(cx - halfW, y0, halfW * 2f, y1 - y0 + 0.5f);
            }
        }
        // Subtle specular highlight
        shapes.setColor(1f, 1f, 1f, 0.16f);
        shapes.circle(cx - radius * 0.28f, cy + radius * 0.30f, radius * 0.24f, 16);
    }

    private void drawCentered(SpriteBatch sb, String text, float cx, float y, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(sb, text, cx - layout.width * 0.5f, y);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
