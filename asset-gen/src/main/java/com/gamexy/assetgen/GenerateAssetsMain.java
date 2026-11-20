package com.gamexy.assetgen;

import com.gamexy.assetgen.MeshBuilder.MaterialSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generates all standalone .glb 3D model files for gamexy into assets/models/.
 * Every generated file is a standard glTF 2.0 binary that can be inspected in Blender
 * or swapped out with custom artist models without changing game code.
 */
public final class GenerateAssetsMain {

    public static void main(String[] args) throws IOException {
        Path outDir = args.length > 0
            ? Paths.get(args[0])
            : Paths.get("assets", "models");

        buildBarbarian(outDir.resolve("barbarian.glb"));
        buildBattleAxe(outDir.resolve("battle_axe.glb"));
        buildSkeleton(outDir.resolve("skeleton.glb"));
        buildBrute(outDir.resolve("brute.glb"));
        buildPillar(outDir.resolve("pillar.glb"));
        buildBrazier(outDir.resolve("brazier.glb"));
        buildCrate(outDir.resolve("crate.glb"));
        buildFloorTile(outDir.resolve("floor_tile.glb"));

        System.out.println("Generated .glb assets in: " + outDir.toAbsolutePath());
    }

    /**
     * Barbarian hero facing +Z, feet resting at Y = 0, height ~2.1 units.
     */
    private static void buildBarbarian(Path out) throws IOException {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec skin = new MaterialSpec("Skin", 0.78f, 0.56f, 0.42f, 0.0f, 0.72f);
        MaterialSpec fur = new MaterialSpec("Fur", 0.28f, 0.20f, 0.14f, 0.0f, 0.90f);
        MaterialSpec leather = new MaterialSpec("Leather", 0.36f, 0.19f, 0.10f, 0.05f, 0.75f);
        MaterialSpec steel = new MaterialSpec("Steel", 0.62f, 0.65f, 0.70f, 0.85f, 0.32f);
        MaterialSpec horn = new MaterialSpec("Horn", 0.86f, 0.81f, 0.68f, 0.0f, 0.55f);
        MaterialSpec cape = new MaterialSpec("CrimsonCape", 0.55f, 0.10f, 0.10f, 0.0f, 0.85f);
        MaterialSpec beard = new MaterialSpec("Beard", 0.20f, 0.12f, 0.07f, 0.0f, 0.88f);
        MaterialSpec gold = new MaterialSpec("GoldTrim", 0.82f, 0.64f, 0.22f, 0.88f, 0.28f);

        // Fur boots
        mb.addBox(fur, -0.22f, 0.22f, 0.02f, 0.24f, 0.44f, 0.30f);
        mb.addBox(fur,  0.22f, 0.22f, 0.02f, 0.24f, 0.44f, 0.30f);
        // Legs
        mb.addBox(skin, -0.22f, 0.62f, 0.0f, 0.22f, 0.38f, 0.22f);
        mb.addBox(skin,  0.22f, 0.62f, 0.0f, 0.22f, 0.38f, 0.22f);
        // Fur kilt / loincloth + heavy leather belt
        mb.addBox(fur, 0.0f, 0.86f, 0.0f, 0.72f, 0.32f, 0.44f);
        mb.addBox(leather, 0.0f, 1.00f, 0.0f, 0.76f, 0.12f, 0.46f);
        mb.addBox(gold, 0.0f, 1.00f, 0.23f, 0.18f, 0.14f, 0.06f);

        // Broad muscular torso
        mb.addBox(skin, 0.0f, 1.34f, 0.0f, 0.84f, 0.62f, 0.46f);
        // Crossed leather harness
        mb.addBox(leather, 0.0f, 1.34f, 0.0f, 0.88f, 0.14f, 0.50f, 0f, 0f, 28f);
        mb.addBox(leather, 0.0f, 1.34f, 0.0f, 0.88f, 0.14f, 0.50f, 0f, 0f, -28f);

        // Iron spiked pauldrons (shoulders)
        mb.addSphere(steel, -0.54f, 1.58f, 0.0f, 0.24f, 0.22f, 0.26f, 6, 8);
        mb.addSphere(steel,  0.54f, 1.58f, 0.0f, 0.24f, 0.22f, 0.26f, 6, 8);
        mb.addCone(steel, -0.66f, 1.72f, 0.0f, 0.06f, 0.22f, 6, 0f, 0f, 35f);
        mb.addCone(steel,  0.66f, 1.72f, 0.0f, 0.06f, 0.22f, 6, 0f, 0f, -35f);

        // Arms & steel bracers (right arm slightly forward for weapon grip)
        mb.addBox(skin, -0.56f, 1.24f, 0.04f, 0.22f, 0.48f, 0.22f, -10f, 0f, 8f);
        mb.addBox(steel, -0.58f, 1.02f, 0.10f, 0.24f, 0.22f, 0.24f, -10f, 0f, 8f);

        mb.addBox(skin, 0.56f, 1.26f, 0.16f, 0.22f, 0.48f, 0.22f, -35f, 0f, -6f);
        mb.addBox(steel, 0.56f, 1.08f, 0.30f, 0.24f, 0.22f, 0.24f, -35f, 0f, -6f);

        // Crimson war cape flowing behind (-Z)
        mb.addBox(cape, 0.0f, 1.12f, -0.32f, 0.72f, 1.08f, 0.07f, 14f, 0f, 0f);

        // Head + beard
        mb.addBox(skin, 0.0f, 1.82f, 0.02f, 0.36f, 0.36f, 0.36f);
        mb.addBox(beard, 0.0f, 1.69f, 0.18f, 0.30f, 0.20f, 0.14f);

        // Horned iron helmet
        mb.addCylinder(steel, 0.0f, 1.96f, 0.02f, 0.24f, 0.21f, 0.22f, 8, 0f, 0f, 0f);
        mb.addBox(gold, 0.0f, 1.90f, 0.21f, 0.08f, 0.18f, 0.06f); // nose guard
        // Left & right horns
        mb.addCone(horn, -0.30f, 2.08f, 0.02f, 0.06f, 0.34f, 6, 0f, 0f, 52f);
        mb.addCone(horn,  0.30f, 2.08f, 0.02f, 0.06f, 0.34f, 6, 0f, 0f, -52f);

        GlbWriter.writeGlb(out, "Barbarian", mb);
    }

    /**
     * Double-headed Barbarian battle axe, grip origin at (0, 0, 0), extending along +Y.
     */
    private static void buildBattleAxe(Path out) throws IOException {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec wood = new MaterialSpec("WoodHaft", 0.34f, 0.20f, 0.10f, 0.0f, 0.82f);
        MaterialSpec steel = new MaterialSpec("AxeSteel", 0.72f, 0.75f, 0.80f, 0.90f, 0.24f);
        MaterialSpec edge = new MaterialSpec("AxeEdge", 0.90f, 0.92f, 0.96f, 0.95f, 0.16f);
        MaterialSpec gold = new MaterialSpec("PommelGold", 0.82f, 0.64f, 0.22f, 0.85f, 0.30f);

        // Haft
        mb.addCylinder(wood, 0.0f, 0.45f, 0.0f, 0.045f, 0.05f, 1.55f, 8, 0f, 0f, 0f);
        // Pommel & collar
        mb.addSphere(gold, 0.0f, -0.34f, 0.0f, 0.075f, 0.075f, 0.075f, 5, 6);
        mb.addCylinder(steel, 0.0f, 1.05f, 0.0f, 0.09f, 0.09f, 0.22f, 8, 0f, 0f, 0f);
        // Top spike
        mb.addCone(edge, 0.0f, 1.32f, 0.0f, 0.06f, 0.28f, 6, 0f, 0f, 0f);

        // Double axe blades (+Z and -Z)
        mb.addBox(steel, 0.0f, 1.05f,  0.24f, 0.06f, 0.42f, 0.34f);
        mb.addBox(edge,  0.0f, 1.05f,  0.42f, 0.03f, 0.50f, 0.10f);

        mb.addBox(steel, 0.0f, 1.05f, -0.24f, 0.06f, 0.42f, 0.34f);
        mb.addBox(edge,  0.0f, 1.05f, -0.42f, 0.03f, 0.50f, 0.10f);

        GlbWriter.writeGlb(out, "BattleAxe", mb);
    }

    /**
     * Undead skeleton warrior facing +Z, height ~1.9 units.
     */
    private static void buildSkeleton(Path out) throws IOException {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec bone = new MaterialSpec("Bone", 0.82f, 0.79f, 0.68f, 0.0f, 0.68f);
        MaterialSpec darkArmor = new MaterialSpec("RustedIron", 0.26f, 0.22f, 0.22f, 0.70f, 0.55f);
        MaterialSpec glowEye = new MaterialSpec("SoulFire", 1.0f, 0.18f, 0.10f, 1.0f, 0.0f, 0.2f, 1.0f, 0.22f, 0.08f);
        MaterialSpec shieldWood = new MaterialSpec("ShieldWood", 0.32f, 0.21f, 0.14f, 0.0f, 0.85f);

        // Legs
        mb.addCylinder(bone, -0.16f, 0.42f, 0.0f, 0.06f, 0.07f, 0.84f, 6, 0f, 0f, 0f);
        mb.addCylinder(bone,  0.16f, 0.42f, 0.0f, 0.06f, 0.07f, 0.84f, 6, 0f, 0f, 0f);
        // Pelvis & spine
        mb.addBox(bone, 0.0f, 0.88f, 0.0f, 0.44f, 0.14f, 0.24f);
        mb.addCylinder(bone, 0.0f, 1.24f, 0.0f, 0.06f, 0.06f, 0.58f, 6, 0f, 0f, 0f);
        // Ribcage bars
        for (int i = 0; i < 3; i++) {
            mb.addBox(bone, 0.0f, 1.12f + i * 0.13f, 0.02f, 0.48f - i * 0.03f, 0.07f, 0.28f);
        }
        // Rusted shoulder armor
        mb.addBox(darkArmor, -0.34f, 1.46f, 0.0f, 0.20f, 0.14f, 0.22f);
        mb.addBox(darkArmor,  0.34f, 1.46f, 0.0f, 0.20f, 0.14f, 0.22f);
        // Arms
        mb.addCylinder(bone, -0.36f, 1.18f, 0.10f, 0.05f, 0.05f, 0.56f, 6, -25f, 0f, 0f);
        mb.addCylinder(bone,  0.36f, 1.18f, 0.14f, 0.05f, 0.05f, 0.56f, 6, -35f, 0f, 0f);
        // Round buckler shield on left arm
        mb.addCylinder(shieldWood, -0.44f, 1.06f, 0.26f, 0.30f, 0.30f, 0.07f, 8, 90f, 0f, 20f);
        mb.addSphere(darkArmor, -0.46f, 1.06f, 0.30f, 0.09f, 0.09f, 0.06f, 4, 6);
        // Rusty sword in right hand
        mb.addBox(darkArmor, 0.38f, 1.14f, 0.56f, 0.06f, 0.10f, 0.85f, -15f, 0f, 0f);
        mb.addBox(darkArmor, 0.38f, 1.05f, 0.22f, 0.24f, 0.06f, 0.06f);
        // Skull + glowing red eyes
        mb.addBox(bone, 0.0f, 1.68f, 0.02f, 0.30f, 0.32f, 0.30f);
        mb.addBox(bone, 0.0f, 1.52f, 0.08f, 0.20f, 0.10f, 0.18f);
        mb.addBox(glowEye, -0.07f, 1.70f, 0.17f, 0.06f, 0.05f, 0.04f);
        mb.addBox(glowEye,  0.07f, 1.70f, 0.17f, 0.06f, 0.05f, 0.04f);

        GlbWriter.writeGlb(out, "Skeleton", mb);
    }

    /**
     * Hulking demonic brute champion facing +Z, height ~2.6 units.
     */
    private static void buildBrute(Path out) throws IOException {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec demonSkin = new MaterialSpec("DemonSkin", 0.52f, 0.14f, 0.12f, 0.0f, 0.65f);
        MaterialSpec darkIron = new MaterialSpec("DarkIron", 0.18f, 0.18f, 0.22f, 0.85f, 0.38f);
        MaterialSpec horn = new MaterialSpec("ObsidianHorn", 0.14f, 0.12f, 0.14f, 0.2f, 0.45f);
        MaterialSpec glow = new MaterialSpec("Hellfire", 1.0f, 0.48f, 0.06f, 1.0f, 0.0f, 0.2f, 1.0f, 0.45f, 0.05f);

        // Thick legs
        mb.addBox(darkIron, -0.32f, 0.36f, 0.0f, 0.34f, 0.72f, 0.36f);
        mb.addBox(darkIron,  0.32f, 0.36f, 0.0f, 0.34f, 0.72f, 0.36f);
        // Massive hunched torso
        mb.addBox(demonSkin, 0.0f, 1.24f, 0.0f, 1.18f, 1.06f, 0.72f);
        mb.addBox(darkIron, 0.0f, 0.82f, 0.0f, 1.02f, 0.24f, 0.68f);
        // Spiked back/shoulders
        mb.addCone(darkIron, -0.45f, 1.88f, -0.10f, 0.10f, 0.36f, 6, -15f, 0f, 20f);
        mb.addCone(darkIron,  0.45f, 1.88f, -0.10f, 0.10f, 0.36f, 6, -15f, 0f, -20f);
        // Huge arms
        mb.addBox(demonSkin, -0.78f, 1.18f, 0.10f, 0.34f, 0.88f, 0.34f, -15f, 0f, 10f);
        mb.addBox(demonSkin,  0.78f, 1.18f, 0.18f, 0.34f, 0.88f, 0.34f, -30f, 0f, -10f);
        // Iron war-club in right hand
        mb.addCylinder(darkIron, 0.84f, 1.12f, 0.72f, 0.08f, 0.22f, 1.25f, 7, 70f, 0f, 0f);
        // Head + horns + glowing eyes
        mb.addBox(demonSkin, 0.0f, 1.94f, 0.14f, 0.48f, 0.44f, 0.46f);
        mb.addCone(horn, -0.28f, 2.24f, 0.10f, 0.08f, 0.46f, 6, 15f, 0f, 38f);
        mb.addCone(horn,  0.28f, 2.24f, 0.10f, 0.08f, 0.46f, 6, 15f, 0f, -38f);
        mb.addBox(glow, -0.11f, 1.98f, 0.38f, 0.08f, 0.06f, 0.04f);
        mb.addBox(glow,  0.11f, 1.98f, 0.38f, 0.08f, 0.06f, 0.04f);

        GlbWriter.writeGlb(out, "Brute", mb);
    }

    /**
     * Gothic cracked stone dungeon pillar, height ~4.8 units.
     */
    private static void buildPillar(Path out) throws IOException {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec stoneDark = new MaterialSpec("StoneDark", 0.26f, 0.26f, 0.29f, 0.0f, 0.90f);
        MaterialSpec stoneLight = new MaterialSpec("StoneLight", 0.36f, 0.36f, 0.39f, 0.0f, 0.86f);
        MaterialSpec moss = new MaterialSpec("Moss", 0.18f, 0.28f, 0.16f, 0.0f, 0.92f);

        // Stepped base plinth
        mb.addBox(stoneDark, 0.0f, 0.22f, 0.0f, 1.55f, 0.44f, 1.55f);
        mb.addBox(stoneLight, 0.0f, 0.56f, 0.0f, 1.25f, 0.24f, 1.25f);
        // Octagonal shaft
        mb.addCylinder(stoneLight, 0.0f, 2.38f, 0.0f, 0.56f, 0.52f, 3.40f, 8, 0f, 0f, 0f);
        // Stone band + top capital
        mb.addBox(stoneDark, 0.0f, 4.22f, 0.0f, 1.32f, 0.32f, 1.32f);
        mb.addBox(stoneLight, 0.0f, 4.52f, 0.0f, 1.52f, 0.28f, 1.52f);
        // Hint of moss at base
        mb.addBox(moss, 0.56f, 0.50f, 0.52f, 0.32f, 0.16f, 0.34f);

        GlbWriter.writeGlb(out, "Pillar", mb);
    }

    /**
     * Forged iron dungeon brazier with glowing fire embers.
     */
    private static void buildBrazier(Path out) throws IOException {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec iron = new MaterialSpec("ForgedIron", 0.18f, 0.18f, 0.20f, 0.85f, 0.42f);
        MaterialSpec ember = new MaterialSpec("Ember", 1.0f, 0.35f, 0.05f, 1.0f, 0.0f, 0.3f, 1.0f, 0.40f, 0.06f);
        MaterialSpec flame = new MaterialSpec("Flame", 1.0f, 0.78f, 0.18f, 1.0f, 0.0f, 0.2f, 1.0f, 0.72f, 0.12f);

        // Pedestal + bowl
        mb.addCylinder(iron, 0.0f, 0.10f, 0.0f, 0.55f, 0.42f, 0.20f, 8, 0f, 0f, 0f);
        mb.addCylinder(iron, 0.0f, 0.60f, 0.0f, 0.18f, 0.24f, 0.85f, 8, 0f, 0f, 0f);
        mb.addCylinder(iron, 0.0f, 1.14f, 0.0f, 0.34f, 0.62f, 0.34f, 8, 0f, 0f, 0f);
        // Glowing coals + stylized flame cones
        mb.addSphere(ember, 0.0f, 1.32f, 0.0f, 0.44f, 0.16f, 0.44f, 5, 8);
        mb.addCone(flame, 0.0f, 1.62f, 0.0f, 0.24f, 0.58f, 6, 0f, 0f, 0f);
        mb.addCone(ember, -0.14f, 1.52f, 0.10f, 0.14f, 0.38f, 5, 10f, 0f, 12f);
        mb.addCone(ember,  0.14f, 1.50f, -0.10f, 0.14f, 0.36f, 5, -10f, 0f, -12f);

        GlbWriter.writeGlb(out, "Brazier", mb);
    }

    /**
     * Wooden dungeon crate with iron corner reinforcement.
     */
    private static void buildCrate(Path out) throws IOException {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec wood = new MaterialSpec("CrateWood", 0.42f, 0.27f, 0.14f, 0.0f, 0.84f);
        MaterialSpec iron = new MaterialSpec("CrateIron", 0.22f, 0.22f, 0.25f, 0.80f, 0.45f);

        mb.addBox(wood, 0.0f, 0.55f, 0.0f, 1.04f, 1.04f, 1.04f);
        mb.addBox(iron, 0.0f, 0.08f, 0.0f, 1.12f, 0.16f, 1.12f);
        mb.addBox(iron, 0.0f, 1.02f, 0.0f, 1.12f, 0.16f, 1.12f);
        mb.addBox(iron, 0.0f, 0.55f, 0.0f, 1.10f, 0.14f, 1.10f, 0f, 0f, 45f);

        GlbWriter.writeGlb(out, "Crate", mb);
    }

    /**
     * Stone dungeon floor slab (4x4 units, top at Y = 0).
     */
    private static void buildFloorTile(Path out) throws IOException {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec slateA = new MaterialSpec("SlateA", 0.19f, 0.19f, 0.22f, 0.0f, 0.90f);
        MaterialSpec slateB = new MaterialSpec("SlateB", 0.23f, 0.22f, 0.25f, 0.0f, 0.88f);
        MaterialSpec mortar = new MaterialSpec("Mortar", 0.11f, 0.11f, 0.13f, 0.0f, 0.96f);

        // Mortar bed
        mb.addBox(mortar, 0.0f, -0.12f, 0.0f, 4.0f, 0.20f, 4.0f);
        // 4 inset flagstones
        mb.addBox(slateA, -1.0f, -0.02f, -1.0f, 1.90f, 0.04f, 1.90f);
        mb.addBox(slateB,  1.0f, -0.02f, -1.0f, 1.90f, 0.04f, 1.90f);
        mb.addBox(slateB, -1.0f, -0.02f,  1.0f, 1.90f, 0.04f, 1.90f);
        mb.addBox(slateA,  1.0f, -0.02f,  1.0f, 1.90f, 0.04f, 1.90f);

        GlbWriter.writeGlb(out, "FloorTile", mb);
    }
}
