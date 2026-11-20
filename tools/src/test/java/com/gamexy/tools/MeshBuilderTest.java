package com.gamexy.tools;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeshBuilderTest {

    @Test
    void boxGeneratesTwelveTrianglesAndValidUnitNormals() {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec stone = new MaterialSpec("Stone", 0.5f, 0.5f, 0.5f, 0f, 0.9f);
        mb.addBox(stone, 0f, 0f, 0f, 2f, 2f, 2f);

        List<PrimitiveData> prims = mb.getPrimitives();
        assertEquals(1, prims.size());

        PrimitiveData p = prims.get(0);
        assertEquals(36, p.indices.size());
        assertEquals(36 * 3, p.positions.size());
        assertEquals(36 * 3, p.normals.size());

        for (int i = 0; i < p.normals.size(); i += 3) {
            float nx = p.normals.get(i);
            float ny = p.normals.get(i + 1);
            float nz = p.normals.get(i + 2);
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            assertEquals(1.0f, len, 1e-4f);
        }
    }

    @Test
    void groupsGeometryByMaterialName() {
        MeshBuilder mb = new MeshBuilder();
        MaterialSpec matA = new MaterialSpec("A", 1f, 0f, 0f, 0f, 1f);
        MaterialSpec matB = new MaterialSpec("B", 0f, 1f, 0f, 0f, 1f);

        mb.addBox(matA, 0f, 0f, 0f, 1f, 1f, 1f);
        mb.addCylinder(matB, 0f, 1f, 0f, 0.5f, 0.5f, 1f, 6, 0f, 0f, 0f);
        mb.addSphere(matA, 0f, 2f, 0f, 0.5f, 0.5f, 0.5f, 4, 6);

        assertEquals(2, mb.getPrimitives().size());
        assertTrue(mb.getPrimitives().get(0).indices.size() > 36);
        assertTrue(mb.getPrimitives().get(1).indices.size() > 0);
    }
}
