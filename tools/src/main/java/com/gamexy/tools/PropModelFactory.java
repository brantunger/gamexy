package com.gamexy.tools;

/**
 * Procedural 3D model blueprints for static dungeon architecture and courtyard props.
 */
public final class PropModelFactory {

    private PropModelFactory() {}

    /**
     * Gothic cracked stone dungeon pillar, height ~4.8 units.
     */
    public static MeshBuilder createPillar() {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec stoneDark = new MaterialSpec("StoneDark", 0.26f, 0.26f, 0.29f, 0.0f, 0.90f);
        MaterialSpec stoneLight = new MaterialSpec("StoneLight", 0.36f, 0.36f, 0.39f, 0.0f, 0.86f);
        MaterialSpec moss = new MaterialSpec("Moss", 0.18f, 0.28f, 0.16f, 0.0f, 0.92f);

        mb.addBox(stoneDark, 0.0f, 0.22f, 0.0f, 1.55f, 0.44f, 1.55f);
        mb.addBox(stoneLight, 0.0f, 0.56f, 0.0f, 1.25f, 0.24f, 1.25f);
        mb.addCylinder(stoneLight, 0.0f, 2.38f, 0.0f, 0.56f, 0.52f, 3.40f, 8, 0f, 0f, 0f);
        mb.addBox(stoneDark, 0.0f, 4.22f, 0.0f, 1.32f, 0.32f, 1.32f);
        mb.addBox(stoneLight, 0.0f, 4.52f, 0.0f, 1.52f, 0.28f, 1.52f);
        mb.addBox(moss, 0.56f, 0.50f, 0.52f, 0.32f, 0.16f, 0.34f);
        return mb;
    }

    /**
     * Forged iron dungeon brazier with glowing fire embers.
     */
    public static MeshBuilder createBrazier() {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec iron = new MaterialSpec("ForgedIron", 0.18f, 0.18f, 0.20f, 0.85f, 0.42f);
        MaterialSpec ember = new MaterialSpec("Ember", 1.0f, 0.35f, 0.05f, 1.0f, 0.0f, 0.3f, 1.0f, 0.40f, 0.06f);
        MaterialSpec flame = new MaterialSpec("Flame", 1.0f, 0.78f, 0.18f, 1.0f, 0.0f, 0.2f, 1.0f, 0.72f, 0.12f);

        mb.addCylinder(iron, 0.0f, 0.10f, 0.0f, 0.55f, 0.42f, 0.20f, 8, 0f, 0f, 0f);
        mb.addCylinder(iron, 0.0f, 0.60f, 0.0f, 0.18f, 0.24f, 0.85f, 8, 0f, 0f, 0f);
        mb.addCylinder(iron, 0.0f, 1.14f, 0.0f, 0.34f, 0.62f, 0.34f, 8, 0f, 0f, 0f);
        mb.addSphere(ember, 0.0f, 1.32f, 0.0f, 0.44f, 0.16f, 0.44f, 5, 8);
        mb.addCone(flame, 0.0f, 1.62f, 0.0f, 0.24f, 0.58f, 6, 0f, 0f, 0f);
        mb.addCone(ember, -0.14f, 1.52f, 0.10f, 0.14f, 0.38f, 5, 10f, 0f, 12f);
        mb.addCone(ember,  0.14f, 1.50f, -0.10f, 0.14f, 0.36f, 5, -10f, 0f, -12f);
        return mb;
    }

    /**
     * Wooden dungeon crate with iron corner reinforcement.
     */
    public static MeshBuilder createCrate() {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec wood = new MaterialSpec("CrateWood", 0.42f, 0.27f, 0.14f, 0.0f, 0.84f);
        MaterialSpec iron = new MaterialSpec("CrateIron", 0.22f, 0.22f, 0.25f, 0.80f, 0.45f);

        mb.addBox(wood, 0.0f, 0.55f, 0.0f, 1.04f, 1.04f, 1.04f);
        mb.addBox(iron, 0.0f, 0.08f, 0.0f, 1.12f, 0.16f, 1.12f);
        mb.addBox(iron, 0.0f, 1.02f, 0.0f, 1.12f, 0.16f, 1.12f);
        mb.addBox(iron, 0.0f, 0.55f, 0.0f, 1.10f, 0.14f, 1.10f, 0f, 0f, 45f);
        return mb;
    }

    /**
     * Stone dungeon floor slab (4x4 units, top at Y = 0).
     */
    public static MeshBuilder createFloorTile() {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec slateA = new MaterialSpec("SlateA", 0.19f, 0.19f, 0.22f, 0.0f, 0.90f);
        MaterialSpec slateB = new MaterialSpec("SlateB", 0.23f, 0.22f, 0.25f, 0.0f, 0.88f);
        MaterialSpec mortar = new MaterialSpec("Mortar", 0.11f, 0.11f, 0.13f, 0.0f, 0.96f);

        mb.addBox(mortar, 0.0f, -0.12f, 0.0f, 4.0f, 0.20f, 4.0f);
        mb.addBox(slateA, -1.0f, -0.02f, -1.0f, 1.90f, 0.04f, 1.90f);
        mb.addBox(slateB,  1.0f, -0.02f, -1.0f, 1.90f, 0.04f, 1.90f);
        mb.addBox(slateB, -1.0f, -0.02f,  1.0f, 1.90f, 0.04f, 1.90f);
        mb.addBox(slateA,  1.0f, -0.02f,  1.0f, 1.90f, 0.04f, 1.90f);
        return mb;
    }
}
