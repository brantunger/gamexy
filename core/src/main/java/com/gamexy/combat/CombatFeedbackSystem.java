package com.gamexy.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.character.PlayerCharacter;
import com.gamexy.character.Skill;
import com.gamexy.render.VisualEffectRenderer;
import com.gamexy.ui.FloatingText;
import com.gamexy.world.VisualEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Manages short-lived 3D combat visual effects and floating world-space combat text.
 */
public class CombatFeedbackSystem {

    private static final Color COLOR_NO_FURY = new Color(1f, 0.58f, 0.18f, 1f);
    private static final Color COLOR_PLAYER_HIT = new Color(1f, 0.25f, 0.22f, 1f);
    private static final Color COLOR_XP_GAIN = new Color(0.76f, 0.54f, 1.0f, 1f);

    private final List<VisualEffect> effects = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private final List<FloatingText> floatingTextsView = Collections.unmodifiableList(floatingTexts);

    public List<FloatingText> getFloatingTexts() {
        return floatingTextsView;
    }

    public void update(float delta) {
        Iterator<VisualEffect> vfxIt = effects.iterator();
        while (vfxIt.hasNext()) {
            if (!vfxIt.next().update(delta)) {
                vfxIt.remove();
            }
        }
        Iterator<FloatingText> textIt = floatingTexts.iterator();
        while (textIt.hasNext()) {
            if (!textIt.next().update(delta)) {
                textIt.remove();
            }
        }
    }

    public void renderEffects(ShapeRenderer sr) {
        for (VisualEffect fx : effects) {
            VisualEffectRenderer.render(sr, fx);
        }
    }

    public void clear() {
        effects.clear();
        floatingTexts.clear();
    }

    public void spawnSkillVfx(Skill skill, Vector3 origin, float facingDeg) {
        if (skill.type == Skill.Type.CLEAVE) {
            effects.add(new VisualEffect(VisualEffect.Kind.CLEAVE_ARC, origin,
                facingDeg, skill.range, skill.arcDegrees, skill.accentColor, 0.26f));
        } else if (skill.type == Skill.Type.GROUND_SLAM) {
            effects.add(new VisualEffect(VisualEffect.Kind.SHOCKWAVE, origin,
                facingDeg, skill.range, 360f, skill.accentColor, 0.45f));
        } else if (skill.type == Skill.Type.WHIRLWIND) {
            effects.add(new VisualEffect(VisualEffect.Kind.WHIRLWIND, origin,
                facingDeg, skill.range, 360f, skill.accentColor, 0.44f));
        } else if (skill.type == Skill.Type.WAR_CRY) {
            effects.add(new VisualEffect(VisualEffect.Kind.WAR_CRY, origin,
                facingDeg, skill.range, 360f, skill.accentColor, 0.55f));
            spawnText("WAR CRY!", origin, Color.GOLD, 1.35f, 1.2f);
        } else if (skill.type == Skill.Type.LEAP) {
            effects.add(new VisualEffect(VisualEffect.Kind.LEAP_IMPACT, origin,
                facingDeg, skill.range, 360f, skill.accentColor, 0.42f));
        }
    }

    public void spawnLeapTakeoff(Vector3 origin, Skill skill) {
        effects.add(new VisualEffect(VisualEffect.Kind.LEAP_IMPACT, origin,
            0f, 1.4f, 360f, skill.accentColor, 0.25f));
        if (skill.furyGain > 0f) {
            spawnText("+" + Math.round(skill.furyGain) + " FURY", origin,
                new Color(1.0f, 0.72f, 0.24f, 1f), 1.0f, 0.9f);
        }
    }

    public void spawnDamageText(Vector3 pos, float dmg, boolean crit) {
        Color color = crit ? Color.YELLOW : Color.WHITE;
        String label = crit ? Math.round(dmg) + "!" : String.valueOf(Math.round(dmg));
        spawnText(label, pos, color, crit ? 1.35f : 1.05f, 0.85f);
    }

    public void spawnStunnedText(Vector3 pos) {
        spawnText("STUNNED", pos, Color.GOLD, 0.9f, 0.95f);
    }

    public void spawnXpText(Vector3 pos, int xpGain) {
        spawnText("+" + xpGain + " XP", pos, COLOR_XP_GAIN, 1.0f, 1.15f);
    }

    public void spawnPlayerHitText(Vector3 pos, float dmg) {
        spawnText("-" + Math.round(dmg), pos, COLOR_PLAYER_HIT, 1.15f, 0.9f);
    }

    public void spawnNotEnoughFury(Vector3 pos) {
        if (floatingTexts.size() < 8) {
            spawnText("NOT ENOUGH FURY", pos, COLOR_NO_FURY, 1.0f, 0.85f);
        }
    }

    public void spawnLevelUp(PlayerCharacter player) {
        effects.add(new VisualEffect(VisualEffect.Kind.LEVEL_UP, player.position,
            0f, 1.8f, 360f, Color.GOLD, 1.4f));
        String msg = "LEVEL UP! LV " + player.getLevel();
        for (Skill s : player.skills) {
            if (s.unlockLevel == player.getLevel()) {
                msg += " — UNLOCKED " + s.name.toUpperCase() + "!";
                break;
            }
        }
        spawnText(msg, player.position, Color.GOLD, 1.45f, 2.2f);
    }

    public void spawnText(String text, Vector3 pos, Color color, float scale, float duration) {
        float jitterX = MathUtils.random(-0.35f, 0.35f);
        float jitterZ = MathUtils.random(-0.35f, 0.35f);
        floatingTexts.add(new FloatingText(text, pos.x + jitterX, 2.2f, pos.z + jitterZ, color, scale, duration));
    }
}
