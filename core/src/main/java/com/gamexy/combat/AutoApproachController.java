package com.gamexy.combat;

import com.badlogic.gdx.math.MathUtils;
import com.gamexy.character.Enemy;
import com.gamexy.character.PlayerCharacter;
import com.gamexy.character.Skill;
import com.gamexy.targeting.TargetingSystem;
import com.gamexy.world.ArenaCollisionResolver;

/**
 * Steers the player smoothly toward a selected enemy (sliding around courtyard obstacles)
 * until within melee strike reach of the queued skill.
 */
public class AutoApproachController {

    private Enemy chaseTarget = null;
    private int chaseSkillIndex = -1;

    public void start(Enemy target, int skillIndex) {
        this.chaseTarget = target;
        this.chaseSkillIndex = skillIndex;
    }

    public void cancel() {
        this.chaseTarget = null;
        this.chaseSkillIndex = -1;
    }

    public boolean isActive() {
        return chaseTarget != null;
    }

    public static float getStrikeReach(Skill skill, Enemy target) {
        if (skill.type == Skill.Type.LEAP) {
            return LeapController.MAX_LEAP_RANGE + target.type.radius;
        }
        return Math.max(1.2f, skill.range + target.type.radius - 0.35f);
    }

    /**
     * Advances pursuit toward the chased enemy.
     * Returns true while actively moving toward the target; invokes {@code onInReachAndReady} when ready to strike.
     */
    public boolean update(float dt,
                          PlayerCharacter player,
                          TargetingSystem targeting,
                          ArenaCollisionResolver collisionResolver,
                          CombatFeedbackSystem feedback,
                          Runnable onInReachAndReady) {
        if (!isChaseValid(player, targeting)) {
            cancel();
            return false;
        }

        Skill skill = player.skills.get(chaseSkillIndex);
        if (!player.hasEnoughFury(skill)) {
            cancel();
            feedback.spawnNotEnoughFury(player.position);
            return false;
        }

        float dx = chaseTarget.position.x - player.position.x;
        float dz = chaseTarget.position.z - player.position.z;
        float dist = (float) Math.sqrt(dx * dx + dz * dz);
        if (dist > 1e-4f) {
            player.setFacingDeg(MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees);
        }

        float strikeReach = getStrikeReach(skill, chaseTarget);
        if (dist <= strikeReach) {
            if (player.isSkillReady(chaseSkillIndex)) {
                player.selectSkill(chaseSkillIndex);
                cancel();
                onInReachAndReady.run();
            }
            return false;
        }

        stepAndSlideTowardTarget(dt, player, dx, dz, dist, strikeReach, collisionResolver);
        return true;
    }

    private boolean isChaseValid(PlayerCharacter player, TargetingSystem targeting) {
        return chaseTarget != null
            && chaseTarget.isTargetable()
            && targeting.getSelectedEnemy() == chaseTarget
            && chaseSkillIndex >= 0
            && player.isSkillUnlocked(chaseSkillIndex);
    }

    private void stepAndSlideTowardTarget(float dt,
                                          PlayerCharacter player,
                                          float dx,
                                          float dz,
                                          float dist,
                                          float strikeReach,
                                          ArenaCollisionResolver collisionResolver) {
        float desiredStep = Math.min(Math.max(0f, dist - (strikeReach - 0.12f)), player.getMoveSpeed() * dt);
        float dirX = dx / dist;
        float dirZ = dz / dist;
        float prevX = player.position.x;
        float prevZ = player.position.z;

        player.position.x += dirX * desiredStep;
        player.position.z += dirZ * desiredStep;
        collisionResolver.resolve(player.position, player.radius);

        // Slide smoothly around static courtyard pillars/braziers/crates if blocked head-on
        float progress = (player.position.x - prevX) * dirX + (player.position.z - prevZ) * dirZ;
        if (desiredStep > 1e-3f && progress < desiredStep * 0.25f) {
            float side = ((player.position.x - prevX) * (-dirZ) + (player.position.z - prevZ) * dirX) >= 0f ? 1f : -1f;
            float slideSpeed = player.getMoveSpeed() * 0.85f * dt;
            player.position.x += (-dirZ * side) * slideSpeed;
            player.position.z += (dirX * side) * slideSpeed;
            collisionResolver.resolve(player.position, player.radius);
        }
    }
}
