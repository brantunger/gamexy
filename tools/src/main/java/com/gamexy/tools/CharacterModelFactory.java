package com.gamexy.tools;

/**
 * Procedural 3D model blueprints for playable heroes, weapons, and enemies.
 */
public final class CharacterModelFactory {

    private CharacterModelFactory() {}

    /**
     * Barbarian hero facing +Z, feet resting at Y = 0, height ~2.1 units.
     */
    public static MeshBuilder createBarbarian() {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec skin = new MaterialSpec("Skin", 0.78f, 0.56f, 0.42f, 0.0f, 0.72f);
        MaterialSpec fur = new MaterialSpec("Fur", 0.28f, 0.20f, 0.14f, 0.0f, 0.90f);
        MaterialSpec leather = new MaterialSpec("Leather", 0.36f, 0.19f, 0.10f, 0.05f, 0.75f);
        MaterialSpec steel = new MaterialSpec("Steel", 0.62f, 0.65f, 0.70f, 0.85f, 0.32f);
        MaterialSpec horn = new MaterialSpec("Horn", 0.86f, 0.81f, 0.68f, 0.0f, 0.55f);
        MaterialSpec cape = new MaterialSpec("CrimsonCape", 0.55f, 0.10f, 0.10f, 0.0f, 0.85f);
        MaterialSpec beard = new MaterialSpec("Beard", 0.20f, 0.12f, 0.07f, 0.0f, 0.88f);
        MaterialSpec gold = new MaterialSpec("GoldTrim", 0.82f, 0.64f, 0.22f, 0.88f, 0.28f);

        mb.addBox(fur, -0.22f, 0.22f, 0.02f, 0.24f, 0.44f, 0.30f);
        mb.addBox(fur,  0.22f, 0.22f, 0.02f, 0.24f, 0.44f, 0.30f);
        mb.addBox(skin, -0.22f, 0.62f, 0.0f, 0.22f, 0.38f, 0.22f);
        mb.addBox(skin,  0.22f, 0.62f, 0.0f, 0.22f, 0.38f, 0.22f);
        mb.addBox(fur, 0.0f, 0.86f, 0.0f, 0.72f, 0.32f, 0.44f);
        mb.addBox(leather, 0.0f, 1.00f, 0.0f, 0.76f, 0.12f, 0.46f);
        mb.addBox(gold, 0.0f, 1.00f, 0.23f, 0.18f, 0.14f, 0.06f);

        mb.addBox(skin, 0.0f, 1.34f, 0.0f, 0.84f, 0.62f, 0.46f);
        mb.addBox(leather, 0.0f, 1.34f, 0.0f, 0.88f, 0.14f, 0.50f, 0f, 0f, 28f);
        mb.addBox(leather, 0.0f, 1.34f, 0.0f, 0.88f, 0.14f, 0.50f, 0f, 0f, -28f);

        mb.addSphere(steel, -0.54f, 1.58f, 0.0f, 0.24f, 0.22f, 0.26f, 6, 8);
        mb.addSphere(steel,  0.54f, 1.58f, 0.0f, 0.24f, 0.22f, 0.26f, 6, 8);
        mb.addCone(steel, -0.66f, 1.72f, 0.0f, 0.06f, 0.22f, 6, 0f, 0f, 35f);
        mb.addCone(steel,  0.66f, 1.72f, 0.0f, 0.06f, 0.22f, 6, 0f, 0f, -35f);

        mb.addBox(skin, -0.56f, 1.24f, 0.04f, 0.22f, 0.48f, 0.22f, -10f, 0f, 8f);
        mb.addBox(steel, -0.58f, 1.02f, 0.10f, 0.24f, 0.22f, 0.24f, -10f, 0f, 8f);
        mb.addBox(skin, 0.56f, 1.26f, 0.16f, 0.22f, 0.48f, 0.22f, -35f, 0f, -6f);
        mb.addBox(steel, 0.56f, 1.08f, 0.30f, 0.24f, 0.22f, 0.24f, -35f, 0f, -6f);

        mb.addBox(cape, 0.0f, 1.12f, -0.32f, 0.72f, 1.08f, 0.07f, 14f, 0f, 0f);
        mb.addBox(skin, 0.0f, 1.82f, 0.02f, 0.36f, 0.36f, 0.36f);
        mb.addBox(beard, 0.0f, 1.69f, 0.18f, 0.30f, 0.20f, 0.14f);

        mb.addCylinder(steel, 0.0f, 1.96f, 0.02f, 0.24f, 0.21f, 0.22f, 8, 0f, 0f, 0f);
        mb.addBox(gold, 0.0f, 1.90f, 0.21f, 0.08f, 0.18f, 0.06f);
        mb.addCone(horn, -0.30f, 2.08f, 0.02f, 0.06f, 0.34f, 6, 0f, 0f, 52f);
        mb.addCone(horn,  0.30f, 2.08f, 0.02f, 0.06f, 0.34f, 6, 0f, 0f, -52f);
        return mb;
    }

    /**
     * Double-headed Barbarian battle axe, grip origin at (0, 0, 0), extending along +Y.
     */
    public static MeshBuilder createBattleAxe() {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec wood = new MaterialSpec("WoodHaft", 0.34f, 0.20f, 0.10f, 0.0f, 0.82f);
        MaterialSpec steel = new MaterialSpec("AxeSteel", 0.72f, 0.75f, 0.80f, 0.90f, 0.24f);
        MaterialSpec edge = new MaterialSpec("AxeEdge", 0.90f, 0.92f, 0.96f, 0.95f, 0.16f);
        MaterialSpec gold = new MaterialSpec("PommelGold", 0.82f, 0.64f, 0.22f, 0.85f, 0.30f);

        mb.addCylinder(wood, 0.0f, 0.45f, 0.0f, 0.045f, 0.05f, 1.55f, 8, 0f, 0f, 0f);
        mb.addSphere(gold, 0.0f, -0.34f, 0.0f, 0.075f, 0.075f, 0.075f, 5, 6);
        mb.addCylinder(steel, 0.0f, 1.05f, 0.0f, 0.09f, 0.09f, 0.22f, 8, 0f, 0f, 0f);
        mb.addCone(edge, 0.0f, 1.32f, 0.0f, 0.06f, 0.28f, 6, 0f, 0f, 0f);

        mb.addBox(steel, 0.0f, 1.05f,  0.24f, 0.06f, 0.42f, 0.34f);
        mb.addBox(edge,  0.0f, 1.05f,  0.42f, 0.03f, 0.50f, 0.10f);
        mb.addBox(steel, 0.0f, 1.05f, -0.24f, 0.06f, 0.42f, 0.34f);
        mb.addBox(edge,  0.0f, 1.05f, -0.42f, 0.03f, 0.50f, 0.10f);
        return mb;
    }

    /**
     * Undead skeleton warrior facing +Z, height ~1.9 units.
     */
    public static MeshBuilder createSkeleton() {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec bone = new MaterialSpec("Bone", 0.82f, 0.79f, 0.68f, 0.0f, 0.68f);
        MaterialSpec darkArmor = new MaterialSpec("RustedIron", 0.26f, 0.22f, 0.22f, 0.70f, 0.55f);
        MaterialSpec glowEye = new MaterialSpec("SoulFire", 1.0f, 0.18f, 0.10f, 1.0f, 0.0f, 0.2f, 1.0f, 0.22f, 0.08f);
        MaterialSpec shieldWood = new MaterialSpec("ShieldWood", 0.32f, 0.21f, 0.14f, 0.0f, 0.85f);

        mb.addCylinder(bone, -0.16f, 0.42f, 0.0f, 0.06f, 0.07f, 0.84f, 6, 0f, 0f, 0f);
        mb.addCylinder(bone,  0.16f, 0.42f, 0.0f, 0.06f, 0.07f, 0.84f, 6, 0f, 0f, 0f);
        mb.addBox(bone, 0.0f, 0.88f, 0.0f, 0.44f, 0.14f, 0.24f);
        mb.addCylinder(bone, 0.0f, 1.24f, 0.0f, 0.06f, 0.06f, 0.58f, 6, 0f, 0f, 0f);
        for (int i = 0; i < 3; i++) {
            mb.addBox(bone, 0.0f, 1.12f + i * 0.13f, 0.02f, 0.48f - i * 0.03f, 0.07f, 0.28f);
        }
        mb.addBox(darkArmor, -0.34f, 1.46f, 0.0f, 0.20f, 0.14f, 0.22f);
        mb.addBox(darkArmor,  0.34f, 1.46f, 0.0f, 0.20f, 0.14f, 0.22f);
        mb.addCylinder(bone, -0.36f, 1.18f, 0.10f, 0.05f, 0.05f, 0.56f, 6, -25f, 0f, 0f);
        mb.addCylinder(bone,  0.36f, 1.18f, 0.14f, 0.05f, 0.05f, 0.56f, 6, -35f, 0f, 0f);
        mb.addCylinder(shieldWood, -0.44f, 1.06f, 0.26f, 0.30f, 0.30f, 0.07f, 8, 90f, 0f, 20f);
        mb.addSphere(darkArmor, -0.46f, 1.06f, 0.30f, 0.09f, 0.09f, 0.06f, 4, 6);
        mb.addBox(darkArmor, 0.38f, 1.14f, 0.56f, 0.06f, 0.10f, 0.85f, -15f, 0f, 0f);
        mb.addBox(darkArmor, 0.38f, 1.05f, 0.22f, 0.24f, 0.06f, 0.06f);
        mb.addBox(bone, 0.0f, 1.68f, 0.02f, 0.30f, 0.32f, 0.30f);
        mb.addBox(bone, 0.0f, 1.52f, 0.08f, 0.20f, 0.10f, 0.18f);
        mb.addBox(glowEye, -0.07f, 1.70f, 0.17f, 0.06f, 0.05f, 0.04f);
        mb.addBox(glowEye,  0.07f, 1.70f, 0.17f, 0.06f, 0.05f, 0.04f);
        return mb;
    }

    /**
     * Hulking demonic brute champion facing +Z, height ~2.6 units.
     */
    public static MeshBuilder createBrute() {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec demonSkin = new MaterialSpec("DemonSkin", 0.52f, 0.14f, 0.12f, 0.0f, 0.65f);
        MaterialSpec darkIron = new MaterialSpec("DarkIron", 0.18f, 0.18f, 0.22f, 0.85f, 0.38f);
        MaterialSpec horn = new MaterialSpec("ObsidianHorn", 0.14f, 0.12f, 0.14f, 0.2f, 0.45f);
        MaterialSpec glow = new MaterialSpec("Hellfire", 1.0f, 0.48f, 0.06f, 1.0f, 0.0f, 0.2f, 1.0f, 0.45f, 0.05f);

        mb.addBox(darkIron, -0.32f, 0.36f, 0.0f, 0.34f, 0.72f, 0.36f);
        mb.addBox(darkIron,  0.32f, 0.36f, 0.0f, 0.34f, 0.72f, 0.36f);
        mb.addBox(demonSkin, 0.0f, 1.24f, 0.0f, 1.18f, 1.06f, 0.72f);
        mb.addBox(darkIron, 0.0f, 0.82f, 0.0f, 1.02f, 0.24f, 0.68f);
        mb.addCone(darkIron, -0.45f, 1.88f, -0.10f, 0.10f, 0.36f, 6, -15f, 0f, 20f);
        mb.addCone(darkIron,  0.45f, 1.88f, -0.10f, 0.10f, 0.36f, 6, -15f, 0f, -20f);
        mb.addBox(demonSkin, -0.78f, 1.18f, 0.10f, 0.34f, 0.88f, 0.34f, -15f, 0f, 10f);
        mb.addBox(demonSkin,  0.78f, 1.18f, 0.18f, 0.34f, 0.88f, 0.34f, -30f, 0f, -10f);
        mb.addCylinder(darkIron, 0.84f, 1.12f, 0.72f, 0.08f, 0.22f, 1.25f, 7, 70f, 0f, 0f);
        mb.addBox(demonSkin, 0.0f, 1.94f, 0.14f, 0.48f, 0.44f, 0.46f);
        mb.addCone(horn, -0.28f, 2.24f, 0.10f, 0.08f, 0.46f, 6, 15f, 0f, 38f);
        mb.addCone(horn,  0.28f, 2.24f, 0.10f, 0.08f, 0.46f, 6, 15f, 0f, -38f);
        mb.addBox(glow, -0.11f, 1.98f, 0.38f, 0.08f, 0.06f, 0.04f);
        mb.addBox(glow,  0.11f, 1.98f, 0.38f, 0.08f, 0.06f, 0.04f);
        return mb;
    }
}
