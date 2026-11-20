package com.gamexy.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.gamexy.camera.IsometricOrbitCamera;
import com.gamexy.character.Enemy;
import com.gamexy.character.PlayerCharacter;
import com.gamexy.character.Skill;
import com.gamexy.math.AngleMath;
import com.gamexy.targeting.TargetingSystem;
import com.gamexy.world.DungeonWorld;

/**
 * Resolves skill requests, melee hit detection, crit/damage math, kill/XP progression,
 * and enemy melee attacks against the player.
 */
public class CombatSystem {

    private static final float CRIT_CHANCE = 0.18f;
    private static final float CRIT_MULTIPLIER = 1.6f;

    private final PlayerCharacter player;
    private final DungeonWorld world;
    private final TargetingSystem targeting;
    private final IsometricOrbitCamera orbitCam;
    private final CombatFeedbackSystem feedback;
    private final AutoApproachController autoApproach = new AutoApproachController();
    private final LeapController leapController = new LeapController();
    private final Vector3 groundCursor = new Vector3();

    private Runnable onSelectedEnemyKilled;

    public CombatSystem(PlayerCharacter player,
                        DungeonWorld world,
                        TargetingSystem targeting,
                        IsometricOrbitCamera orbitCam,
                        CombatFeedbackSystem feedback) {
        this.player = player;
        this.world = world;
        this.targeting = targeting;
        this.orbitCam = orbitCam;
        this.feedback = feedback;
    }

    public void setOnSelectedEnemyKilled(Runnable onSelectedEnemyKilled) {
        this.onSelectedEnemyKilled = onSelectedEnemyKilled;
    }

    public void cancelAutoApproach() {
        autoApproach.cancel();
    }

    public void cancelActiveActions() {
        autoApproach.cancel();
        leapController.cancel(player.position);
    }

    public boolean isAutoApproaching() {
        return autoApproach.isActive();
    }

    public boolean isLeaping() {
        return leapController.isLeaping();
    }

    public boolean updateAutoApproach(float dt) {
        return autoApproach.update(
            dt, player, targeting, world.getCollisionResolver(), feedback, this::executeSelectedSkill);
    }

    public boolean updateLeap(float dt) {
        if (!leapController.isLeaping()) {
            return false;
        }
        player.setFacingDeg(leapController.getFacingDeg(player.position));
        return leapController.update(
            dt, player.position, player.radius, world.getCollisionResolver(), this::onLeapLanded);
    }

    /**
     * Selects the requested skill. When an enemy is selected, either casts immediately (if in range or War Cry)
     * or starts auto-approaching the target. When no enemy is selected, only selects the skill on the hotbar.
     */
    public void requestUseSkill(int skillIndex) {
        if (!player.isAlive() || !player.selectSkill(skillIndex)) {
            return;
        }
        if (leapController.isLeaping()) {
            return;
        }
        Skill skill = player.getSelectedSkill();
        Enemy selectedEnemy = targeting.getSelectedEnemy();

        if (selectedEnemy == null) {
            autoApproach.cancel();
            return;
        }

        if (skill.type == Skill.Type.WAR_CRY) {
            autoApproach.cancel();
            executeSelectedSkill();
            return;
        }

        if (!player.hasEnoughFury(skill)) {
            autoApproach.cancel();
            feedback.spawnNotEnoughFury(player.position);
            return;
        }

        float dist = AngleMath.distanceXZ(player.position, selectedEnemy.position);
        float strikeReach = AutoApproachController.getStrikeReach(skill, selectedEnemy);
        if (dist <= strikeReach) {
            autoApproach.cancel();
            executeSelectedSkill();
        } else {
            autoApproach.start(selectedEnemy, skillIndex);
        }
    }

    public void updateEnemies(float dt) {
        for (Enemy enemy : world.getEnemies()) {
            boolean hitPlayer = enemy.update(dt, player, world::resolveCollision);
            if (hitPlayer && player.isAlive()) {
                float dmg = enemy.getDamage();
                player.takeDamage(dmg);
                feedback.spawnPlayerHitText(player.position, dmg);
            }
        }
    }

    private void executeSelectedSkill() {
        if (!player.isAlive()) {
            return;
        }
        int idx = player.getSelectedSkillIndex();
        Skill skill = player.getSelectedSkill();
        if (!player.isSkillReady(idx)) {
            return;
        }
        if (!player.hasEnoughFury(skill)) {
            feedback.spawnNotEnoughFury(player.position);
            return;
        }

        aimPlayerForAttack();
        if (skill.type == Skill.Type.LEAP) {
            launchLeap(idx, skill);
            return;
        }

        player.triggerSkillState(idx);
        feedback.spawnSkillVfx(skill, player.position, player.getFacingDeg());
        resolveMeleeHits(skill);
    }

    private void launchLeap(int skillIndex, Skill skill) {
        Enemy selectedEnemy = targeting.getSelectedEnemy();
        if (selectedEnemy == null) {
            return;
        }
        float flightDuration = leapController.start(
            player.position, player.radius, selectedEnemy, selectedEnemy.type.radius, skill);
        player.triggerSkillState(skillIndex, flightDuration);
        feedback.spawnLeapTakeoff(player.position, skill);
    }

    private void onLeapLanded() {
        Skill skill = leapController.getActiveSkill();
        if (skill == null || !player.isAlive()) {
            return;
        }
        feedback.spawnSkillVfx(skill, player.position, player.getFacingDeg());
        resolveMeleeHits(skill);
    }

    private void aimPlayerForAttack() {
        Enemy selectedEnemy = targeting.getSelectedEnemy();
        if (selectedEnemy != null) {
            faceToward(selectedEnemy.position);
        } else if (orbitCam.screenToGround(Gdx.input.getX(), Gdx.input.getY(), groundCursor)) {
            faceToward(groundCursor);
        }
    }

    private void faceToward(Vector3 targetPos) {
        float dx = targetPos.x - player.position.x;
        float dz = targetPos.z - player.position.z;
        if (dx * dx + dz * dz > 1e-4f) {
            player.setFacingDeg(MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees);
        }
    }

    private void resolveMeleeHits(Skill skill) {
        float halfArc = skill.arcDegrees * 0.5f;
        for (Enemy enemy : world.getEnemies()) {
            if (!isEnemyInSkillArea(enemy, skill, halfArc)) {
                continue;
            }
            boolean crit = MathUtils.random() < CRIT_CHANCE;
            float dmg = player.getEffectiveDamage(skill) * (crit ? CRIT_MULTIPLIER : 1.0f);
            boolean killed = enemy.takeDamage(dmg, player.position, skill.knockback, skill.stunDuration);

            feedback.spawnDamageText(enemy.position, dmg, crit);
            if (skill.stunDuration > 0f && !killed) {
                feedback.spawnStunnedText(enemy.position);
            }
            if (killed) {
                handleEnemyKilled(enemy);
            }
        }
    }

    private boolean isEnemyInSkillArea(Enemy enemy, Skill skill, float halfArc) {
        if (!enemy.isAlive()) {
            return false;
        }
        float dx = enemy.position.x - player.position.x;
        float dz = enemy.position.z - player.position.z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);
        if (dist > skill.range + enemy.type.radius) {
            return false;
        }
        if (skill.arcDegrees < 359f && dist > 0.2f) {
            float angleToEnemy = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
            float diff = AngleMath.shortestAngleDiff(player.getFacingDeg(), angleToEnemy);
            return Math.abs(diff) <= halfArc;
        }
        return true;
    }

    private void handleEnemyKilled(Enemy enemy) {
        player.recordKill();
        int xpGain = enemy.type.xpReward;
        feedback.spawnXpText(enemy.position, xpGain);
        if (player.addXp(xpGain)) {
            feedback.spawnLevelUp(player);
        }
        if (enemy == targeting.getSelectedTarget()) {
            if (onSelectedEnemyKilled != null) {
                onSelectedEnemyKilled.run();
            }
            autoApproach.cancel();
            targeting.selectNextAvailableEnemy(player.position, world.getEnemies());
        }
    }
}
