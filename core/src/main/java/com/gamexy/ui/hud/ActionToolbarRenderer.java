package com.gamexy.ui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.gamexy.character.PlayerCharacter;
import com.gamexy.character.Skill;
import com.gamexy.ui.UiFonts;

import java.util.List;

/**
 * Renders the bottom Gothic action toolbar: Hero/Wave/Kills header, 10 skill slots (1-9, 0),
 * XP progress bar, active War Cry buff banner, and skill hover tooltip across 3 HUD passes.
 */
public class ActionToolbarRenderer {

    private final Rectangle buffBounds = new Rectangle();
    private final Rectangle tooltipBounds = new Rectangle();
    private boolean showTooltip = false;

    public void prepareFrame(HudLayout layout, PlayerCharacter player) {
        float barScale = layout.getBarScale();
        int screenW = layout.getScreenW();

        float buffW = Math.min(340f * barScale, screenW - 24f * barScale);
        float buffH = 34f * barScale;
        float buffX = (screenW - buffW) * 0.5f;
        float buffY = layout.toolbarPanelBounds.y + layout.toolbarPanelBounds.height + 10f * barScale;
        buffBounds.set(buffX, buffY, buffW, buffH);

        int hovered = layout.getHoveredSlot();
        showTooltip = hovered >= 0 && hovered < player.skills.size();
        if (showTooltip) {
            float tipMargin = 14f * barScale;
            float tipW = Math.min(420f * barScale, Math.max(120f, screenW - tipMargin * 2f));
            float tipH = 98f * barScale;
            Rectangle slot = layout.getSlotBounds().get(hovered);
            float tipX = MathUtils.clamp(slot.x + slot.width * 0.5f - tipW * 0.5f, tipMargin, screenW - tipW - tipMargin);
            float tipY = layout.toolbarPanelBounds.y + layout.toolbarPanelBounds.height
                + (player.isWarCryBuffActive() ? (buffH + 18f * barScale) : 14f * barScale);
            tooltipBounds.set(tipX, tipY, tipW, tipH);
        }
    }

    public void renderFilled(ShapeRenderer shapes, HudLayout layout, PlayerCharacter player) {
        renderPanelBacking(shapes, layout);
        renderXpBarFilled(shapes, layout, player);
        renderSlotsFilled(shapes, layout, player);

        if (player.isWarCryBuffActive()) {
            shapes.setColor(0.28f, 0.19f, 0.04f, 0.94f);
            shapes.rect(buffBounds.x, buffBounds.y, buffBounds.width, buffBounds.height);
        }
        if (showTooltip) {
            shapes.setColor(0.07f, 0.06f, 0.09f, 0.96f);
            shapes.rect(tooltipBounds.x, tooltipBounds.y, tooltipBounds.width, tooltipBounds.height);
        }
    }

    private void renderPanelBacking(ShapeRenderer shapes, HudLayout layout) {
        float barScale = layout.getBarScale();
        Rectangle panel = layout.toolbarPanelBounds;
        shapes.setColor(0f, 0f, 0f, 0.55f);
        shapes.rect(panel.x + 5f * barScale, panel.y - 5f * barScale, panel.width, panel.height);

        shapes.setColor(0.07f, 0.06f, 0.085f, 0.94f);
        shapes.rect(panel.x, panel.y, panel.width, panel.height);

        shapes.setColor(0.24f, 0.06f, 0.07f, 0.92f);
        shapes.rect(layout.headerBounds.x, layout.headerBounds.y, layout.headerBounds.width, layout.headerBounds.height);
    }

    private void renderXpBarFilled(ShapeRenderer shapes, HudLayout layout, PlayerCharacter player) {
        Rectangle xp = layout.xpBarBounds;
        float xpRatio = MathUtils.clamp((float) player.getXp() / Math.max(1, player.getXpToNextLevel()), 0f, 1f);
        shapes.setColor(0.13f, 0.10f, 0.17f, 1f);
        shapes.rect(xp.x, xp.y, xp.width, xp.height);
        if (xpRatio > 0.001f) {
            shapes.setColor(0.56f, 0.30f, 0.88f, 1f);
            shapes.rect(xp.x, xp.y, xp.width * xpRatio, xp.height);
            float xpGlossH = Math.max(1.5f, 4f * layout.getBarScale());
            shapes.setColor(0.78f, 0.58f, 1.0f, 0.40f);
            shapes.rect(xp.x, xp.y + xp.height - xpGlossH, xp.width * xpRatio, xpGlossH);
        }
    }

    private void renderSlotsFilled(ShapeRenderer shapes, HudLayout layout, PlayerCharacter player) {
        List<Rectangle> slots = layout.getSlotBounds();
        for (int i = 0; i < HudLayout.TOOLBAR_SLOTS; i++) {
            Rectangle r = slots.get(i);
            if (i >= player.skills.size()) {
                shapes.setColor(0.09f, 0.085f, 0.11f, 0.88f);
                shapes.rect(r.x, r.y, r.width, r.height);
                continue;
            }
            renderSkillSlotFill(shapes, r, player, i);
        }
    }

    private void renderSkillSlotFill(ShapeRenderer shapes, Rectangle r, PlayerCharacter player, int index) {
        Skill skill = player.skills.get(index);
        boolean unlocked = player.isSkillUnlocked(index);
        boolean selected = (player.getSelectedSkillIndex() == index);

        if (!unlocked) {
            shapes.setColor(0.10f, 0.10f, 0.12f, 0.94f);
        } else if (selected) {
            shapes.setColor(skill.accentColor.r * 0.52f, skill.accentColor.g * 0.52f, skill.accentColor.b * 0.52f, 0.98f);
        } else {
            shapes.setColor(skill.accentColor.r * 0.26f, skill.accentColor.g * 0.26f, skill.accentColor.b * 0.26f, 0.94f);
        }
        shapes.rect(r.x, r.y, r.width, r.height);

        if (unlocked) {
            float stripeH = Math.max(6f, r.height * 0.09f);
            shapes.setColor(skill.accentColor.r, skill.accentColor.g, skill.accentColor.b, selected ? 1.0f : 0.65f);
            shapes.rect(r.x, r.y + r.height - stripeH, r.width, stripeH);
        }

        float cd = player.getCooldown(index);
        if (unlocked && cd > 0f) {
            float cdRatio = MathUtils.clamp(cd / skill.cooldown, 0f, 1f);
            shapes.setColor(0f, 0f, 0f, 0.72f);
            shapes.rect(r.x, r.y, r.width, r.height * cdRatio);
        }

        if (unlocked && !player.hasEnoughFury(skill)) {
            shapes.setColor(0.05f, 0.05f, 0.12f, 0.48f);
            shapes.rect(r.x, r.y, r.width, r.height);
        }
    }

    public void renderBorders(ShapeRenderer shapes, HudLayout layout, PlayerCharacter player) {
        renderPanelBorders(shapes, layout);
        renderSlotBorders(shapes, layout, player);

        if (player.isWarCryBuffActive()) {
            shapes.setColor(1.0f, 0.84f, 0.26f, 0.95f);
            shapes.rect(buffBounds.x, buffBounds.y, buffBounds.width, buffBounds.height);
        }
        if (showTooltip) {
            shapes.setColor(0.72f, 0.58f, 0.30f, 0.95f);
            shapes.rect(tooltipBounds.x, tooltipBounds.y, tooltipBounds.width, tooltipBounds.height);
        }
    }

    private void renderPanelBorders(ShapeRenderer shapes, HudLayout layout) {
        float barScale = layout.getBarScale();
        Rectangle panel = layout.toolbarPanelBounds;
        float innerInset = Math.max(1.5f, 3f * barScale);

        shapes.setColor(0.68f, 0.54f, 0.28f, 0.98f);
        shapes.rect(panel.x, panel.y, panel.width, panel.height);
        shapes.setColor(0.38f, 0.28f, 0.16f, 0.90f);
        shapes.rect(panel.x + innerInset, panel.y + innerInset,
            panel.width - innerInset * 2f, panel.height - innerInset * 2f);

        float corner = 9f * barScale;
        shapes.setColor(0.92f, 0.76f, 0.36f, 0.95f);
        shapes.rect(panel.x - 1f, panel.y - 1f, corner, corner);
        shapes.rect(panel.x + panel.width - corner + 1f, panel.y - 1f, corner, corner);
        shapes.rect(panel.x - 1f, panel.y + panel.height - corner + 1f, corner, corner);
        shapes.rect(panel.x + panel.width - corner + 1f, panel.y + panel.height - corner + 1f, corner, corner);

        shapes.setColor(0.56f, 0.42f, 0.22f, 0.85f);
        shapes.rect(layout.headerBounds.x, layout.headerBounds.y, layout.headerBounds.width, layout.headerBounds.height);
        shapes.rect(layout.xpBarBounds.x, layout.xpBarBounds.y, layout.xpBarBounds.width, layout.xpBarBounds.height);
    }

    private void renderSlotBorders(ShapeRenderer shapes, HudLayout layout, PlayerCharacter player) {
        List<Rectangle> slots = layout.getSlotBounds();
        int hoveredSlot = layout.getHoveredSlot();
        for (int i = 0; i < HudLayout.TOOLBAR_SLOTS; i++) {
            Rectangle r = slots.get(i);
            boolean hasSkill = i < player.skills.size();
            boolean selected = hasSkill && (player.getSelectedSkillIndex() == i);
            if (selected) {
                shapes.setColor(1.0f, 0.86f, 0.28f, 1f);
                shapes.rect(r.x - 3f, r.y - 3f, r.width + 6f, r.height + 6f);
                shapes.rect(r.x - 2f, r.y - 2f, r.width + 4f, r.height + 4f);
                shapes.rect(r.x - 1f, r.y - 1f, r.width + 2f, r.height + 2f);
            } else if (hoveredSlot == i) {
                shapes.setColor(0.92f, 0.90f, 0.96f, 0.90f);
                shapes.rect(r.x - 1f, r.y - 1f, r.width + 2f, r.height + 2f);
                shapes.rect(r.x, r.y, r.width, r.height);
            } else if (hasSkill) {
                shapes.setColor(0.48f, 0.40f, 0.32f, 0.92f);
                shapes.rect(r.x, r.y, r.width, r.height);
            } else {
                shapes.setColor(0.28f, 0.24f, 0.24f, 0.80f);
                shapes.rect(r.x, r.y, r.width, r.height);
            }
        }
    }

    public void renderText(SpriteBatch batch, UiFonts fonts, GlyphLayout glyphLayout,
                           HudLayout layout, PlayerCharacter player, int waveNumber) {
        renderHeaderAndBuffText(batch, fonts, glyphLayout, layout, player, waveNumber);
        renderSlotsText(batch, fonts, glyphLayout, layout, player);
        renderXpText(batch, fonts, glyphLayout, layout, player);
        if (showTooltip) {
            renderTooltipText(batch, fonts, layout, player);
        }
    }

    private void renderHeaderAndBuffText(SpriteBatch batch, UiFonts fonts, GlyphLayout glyphLayout,
                                         HudLayout layout, PlayerCharacter player, int waveNumber) {
        String headerText = String.format("LVL %d %s   |   WAVE %d   |   KILLS %d",
            player.getLevel(), player.characterClass.displayName.toUpperCase(), waveNumber, player.getKills());
        drawCentered(batch, fonts.headerSerif, glyphLayout, headerText,
            layout.headerBounds.x + layout.headerBounds.width * 0.5f,
            layout.headerBounds.y + layout.headerBounds.height * 0.5f,
            1.0f, 0.88f, 0.44f, 1f);

        if (player.isWarCryBuffActive()) {
            drawCentered(batch, fonts.hudMediumSans, glyphLayout,
                String.format("WAR CRY (+35%% DMG)  %.1fs", player.getWarCryBuffRemaining()),
                layout.getScreenW() * 0.5f, buffBounds.y + buffBounds.height * 0.5f,
                1.0f, 0.90f, 0.34f, 1f);
        }
    }

    private void renderSlotsText(SpriteBatch batch, UiFonts fonts, GlyphLayout glyphLayout,
                                 HudLayout layout, PlayerCharacter player) {
        List<Rectangle> slots = layout.getSlotBounds();
        for (int i = 0; i < HudLayout.TOOLBAR_SLOTS; i++) {
            Rectangle r = slots.get(i);
            String hotkeyLabel = (i < 9) ? String.valueOf(i + 1) : "0";
            boolean hasSkill = i < player.skills.size();

            if (hasSkill) {
                fonts.hudSmallSans.setColor(0.96f, 0.92f, 0.80f, 1f);
            } else {
                fonts.hudSmallSans.setColor(0.50f, 0.48f, 0.52f, 0.85f);
            }
            fonts.hudSmallSans.draw(batch, hotkeyLabel, r.x + r.width * 0.08f, r.y + r.height - r.height * 0.13f);

            if (hasSkill) {
                renderSkillSlotText(batch, fonts, glyphLayout, r, player, i);
            }
        }
    }

    private void renderSkillSlotText(SpriteBatch batch, UiFonts fonts, GlyphLayout glyphLayout,
                                     Rectangle r, PlayerCharacter player, int index) {
        Skill skill = player.skills.get(index);
        boolean unlocked = player.isSkillUnlocked(index);
        boolean selected = (player.getSelectedSkillIndex() == index);

        if (selected) {
            fonts.hudSmallSans.setColor(1.0f, 0.88f, 0.30f, 1f);
            glyphLayout.setText(fonts.hudSmallSans, "LMB");
            fonts.hudSmallSans.draw(batch, "LMB",
                r.x + r.width - glyphLayout.width - r.width * 0.07f,
                r.y + r.height - r.height * 0.13f);
        }

        if (!unlocked) {
            drawCentered(batch, fonts.hudMediumSans, glyphLayout, "LV " + skill.unlockLevel,
                r.x + r.width * 0.5f, r.y + r.height * 0.48f, Color.LIGHT_GRAY);
            return;
        }

        drawCentered(batch, fonts.hudMediumSans, glyphLayout, skill.shortLabel,
            r.x + r.width * 0.5f, r.y + r.height * 0.52f, Color.WHITE);

        float cd = player.getCooldown(index);
        if (cd > 0f) {
            drawCentered(batch, fonts.hudSmallSans, glyphLayout, String.format("%.1fs", cd),
                r.x + r.width * 0.5f, r.y + r.height * 0.19f, 1f, 0.72f, 0.42f, 1f);
        } else if (skill.furyCost > 0f) {
            if (player.hasEnoughFury(skill)) {
                drawCentered(batch, fonts.hudSmallSans, glyphLayout, (int) skill.furyCost + " Fury",
                    r.x + r.width * 0.5f, r.y + r.height * 0.19f, 1f, 0.74f, 0.26f, 1f);
            } else {
                drawCentered(batch, fonts.hudSmallSans, glyphLayout, (int) skill.furyCost + " Fury",
                    r.x + r.width * 0.5f, r.y + r.height * 0.19f, Color.SALMON);
            }
        } else {
            drawCentered(batch, fonts.hudSmallSans, glyphLayout, "+" + (int) skill.furyGain + " Fury",
                r.x + r.width * 0.5f, r.y + r.height * 0.19f, 0.66f, 0.98f, 0.66f, 1f);
        }
    }

    private void renderXpText(SpriteBatch batch, UiFonts fonts, GlyphLayout glyphLayout,
                              HudLayout layout, PlayerCharacter player) {
        drawCentered(batch, fonts.hudSmallSans, glyphLayout,
            String.format("XP  %d / %d", player.getXp(), player.getXpToNextLevel()),
            layout.xpBarBounds.x + layout.xpBarBounds.width * 0.5f,
            layout.xpBarBounds.y + layout.xpBarBounds.height * 0.5f,
            0.96f, 0.92f, 1.0f, 1f);
    }

    private void renderTooltipText(SpriteBatch batch, UiFonts fonts, HudLayout layout, PlayerCharacter player) {
        float barScale = layout.getBarScale();
        int hovered = layout.getHoveredSlot();
        Skill s = player.skills.get(hovered);
        String title = s.name + (player.isSkillUnlocked(hovered) ? "" : " (Unlocks at Lv " + s.unlockLevel + ")");
        fonts.headerSerif.setColor(1.0f, 0.88f, 0.36f, 1f);
        fonts.headerSerif.draw(batch, title,
            tooltipBounds.x + 14f * barScale, tooltipBounds.y + tooltipBounds.height - 14f * barScale);

        fonts.hudSmallSans.setColor(0.92f, 0.92f, 0.96f, 1f);
        fonts.hudSmallSans.draw(batch, s.description,
            tooltipBounds.x + 14f * barScale, tooltipBounds.y + tooltipBounds.height - 44f * barScale,
            tooltipBounds.width - 28f * barScale, Align.left, true);
    }

    private static void drawCentered(SpriteBatch batch, BitmapFont font, GlyphLayout layout,
                                     String text, float cx, float cy, float r, float g, float b, float a) {
        font.setColor(r, g, b, a);
        layout.setText(font, text);
        font.draw(batch, text, cx - layout.width * 0.5f, cy + layout.height * 0.5f);
    }

    private static void drawCentered(SpriteBatch batch, BitmapFont font, GlyphLayout layout,
                                     String text, float cx, float cy, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, text, cx - layout.width * 0.5f, cy + layout.height * 0.5f);
    }
}
