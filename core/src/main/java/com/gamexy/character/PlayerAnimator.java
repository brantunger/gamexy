package com.gamexy.character;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * Procedural 3D body and weapon animation controller for {@link PlayerCharacter}.
 */
public class PlayerAnimator {

    private float walkTime = 0f;
    private final Matrix4 tmpMat = new Matrix4();

    public void reset() {
        walkTime = 0f;
    }

    public void update(PlayerCharacter player, float delta, boolean isMoving) {
        walkTime = isMoving ? (walkTime + delta * 11f) : 0f;
        applyBodyTransform(player, isMoving);
        applyWeaponTransform(player);
    }

    private void applyBodyTransform(PlayerCharacter player, boolean isMoving) {
        float bobY = isMoving ? Math.abs(MathUtils.sin(walkTime)) * 0.12f : 0f;
        float swayZ = isMoving ? MathUtils.sin(walkTime) * 4.5f : 0f;
        float extraYaw = 0f;
        float bodyPitch = 0f;

        if (player.isAttacking()) {
            float p = player.getAttackProgress();
            Skill.Type animSkill = player.getActiveAnimSkill();
            if (animSkill == Skill.Type.WHIRLWIND) {
                extraYaw = p * 360f;
            } else if (animSkill == Skill.Type.GROUND_SLAM) {
                bobY += MathUtils.sin(p * MathUtils.PI) * 0.55f;
                bodyPitch = MathUtils.sin(p * MathUtils.PI) * 18f;
            } else if (animSkill == Skill.Type.CLEAVE) {
                extraYaw = MathUtils.lerp(-42f, 52f, p);
            } else if (animSkill == Skill.Type.WAR_CRY) {
                bobY += MathUtils.sin(p * MathUtils.PI) * 0.25f;
                bodyPitch = -12f * MathUtils.sin(p * MathUtils.PI);
            } else if (animSkill == Skill.Type.LEAP) {
                bodyPitch = MathUtils.lerp(-18f, 24f, p);
            }
        }

        player.bodyScene.modelInstance.transform.idt()
            .translate(player.position.x, player.position.y + bobY, player.position.z)
            .rotate(Vector3.Y, player.getFacingDeg() + extraYaw)
            .rotate(Vector3.X, bodyPitch)
            .rotate(Vector3.Z, swayZ);
    }

    private void applyWeaponTransform(PlayerCharacter player) {
        tmpMat.set(player.bodyScene.modelInstance.transform);

        if (player.isAttacking()) {
            applyAttackWeaponPose(player.getActiveAnimSkill(), player.getAttackProgress());
        } else {
            applyReadyWeaponPose();
        }

        player.weaponScene.modelInstance.transform.set(tmpMat);
    }

    private void applyAttackWeaponPose(Skill.Type skillType, float p) {
        if (skillType == Skill.Type.CLEAVE) {
            float sweepYaw = MathUtils.lerp(-80f, 85f, p);
            tmpMat.translate(0.55f, 1.15f, 0.42f)
                .rotate(Vector3.Y, sweepYaw)
                .rotate(Vector3.X, 78f);
        } else if (skillType == Skill.Type.GROUND_SLAM) {
            float slamPitch = p < 0.4f
                ? MathUtils.lerp(20f, -65f, p / 0.4f)
                : MathUtils.lerp(-65f, 125f, (p - 0.4f) / 0.6f);
            tmpMat.translate(0.45f, 1.35f, 0.35f)
                .rotate(Vector3.X, slamPitch);
        } else if (skillType == Skill.Type.WHIRLWIND) {
            tmpMat.translate(0.65f, 1.20f, 0.25f)
                .rotate(Vector3.Z, -85f)
                .rotate(Vector3.X, 80f);
        } else if (skillType == Skill.Type.LEAP) {
            float leapPitch = p < 0.65f
                ? MathUtils.lerp(15f, -72f, p / 0.65f)
                : MathUtils.lerp(-72f, 118f, (p - 0.65f) / 0.35f);
            tmpMat.translate(0.46f, 1.42f, 0.32f)
                .rotate(Vector3.X, leapPitch);
        } else {
            tmpMat.translate(0.60f, 1.55f, 0.25f)
                .rotate(Vector3.Z, -20f);
        }
    }

    private void applyReadyWeaponPose() {
        float idleSway = MathUtils.sin(walkTime * 0.8f) * 5f;
        tmpMat.translate(0.58f, 1.08f, 0.38f)
            .rotate(Vector3.X, 34f + idleSway)
            .rotate(Vector3.Z, -14f);
    }
}
