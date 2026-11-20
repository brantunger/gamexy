package com.gamexy.tools;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * CLI entrypoint that generates all standalone .glb 3D model files for gamexy into assets/models/.
 */
public final class GenerateAssetsMain {

    private GenerateAssetsMain() {}

    public static void main(String[] args) throws IOException {
        Path outDir = args.length > 0
            ? Paths.get(args[0])
            : Paths.get("assets", "models");

        GlbWriter.writeGlb(outDir.resolve("barbarian.glb"), "Barbarian", CharacterModelFactory.createBarbarian());
        GlbWriter.writeGlb(outDir.resolve("battle_axe.glb"), "BattleAxe", CharacterModelFactory.createBattleAxe());
        GlbWriter.writeGlb(outDir.resolve("skeleton.glb"), "Skeleton", CharacterModelFactory.createSkeleton());
        GlbWriter.writeGlb(outDir.resolve("brute.glb"), "Brute", CharacterModelFactory.createBrute());

        GlbWriter.writeGlb(outDir.resolve("pillar.glb"), "Pillar", PropModelFactory.createPillar());
        GlbWriter.writeGlb(outDir.resolve("brazier.glb"), "Brazier", PropModelFactory.createBrazier());
        GlbWriter.writeGlb(outDir.resolve("crate.glb"), "Crate", PropModelFactory.createCrate());
        GlbWriter.writeGlb(outDir.resolve("floor_tile.glb"), "FloorTile", PropModelFactory.createFloorTile());

        System.out.println("Generated .glb assets in: " + outDir.toAbsolutePath());
    }
}
