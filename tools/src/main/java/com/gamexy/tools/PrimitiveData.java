package com.gamexy.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Flat-shaded triangle geometry buffer grouped by a single {@link MaterialSpec}.
 */
public final class PrimitiveData {

    public final MaterialSpec material;
    public final List<Float> positions = new ArrayList<>();
    public final List<Float> normals = new ArrayList<>();
    public final List<Integer> indices = new ArrayList<>();

    public PrimitiveData(MaterialSpec material) {
        this.material = material;
    }

    public void addTriangle(float[] a, float[] b, float[] c) {
        float ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2];
        float vx = c[0] - a[0], vy = c[1] - a[1], vz = c[2] - a[2];
        float nx = uy * vz - uz * vy;
        float ny = uz * vx - ux * vz;
        float nz = ux * vy - uy * vx;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1e-6f) {
            return;
        }
        nx /= len;
        ny /= len;
        nz /= len;

        int base = positions.size() / 3;
        addVertex(a[0], a[1], a[2], nx, ny, nz);
        addVertex(b[0], b[1], b[2], nx, ny, nz);
        addVertex(c[0], c[1], c[2], nx, ny, nz);
        indices.add(base);
        indices.add(base + 1);
        indices.add(base + 2);
    }

    public void addQuad(float[] a, float[] b, float[] c, float[] d) {
        addTriangle(a, b, c);
        addTriangle(a, c, d);
    }

    private void addVertex(float x, float y, float z, float nx, float ny, float nz) {
        positions.add(x);
        positions.add(y);
        positions.add(z);
        normals.add(nx);
        normals.add(ny);
        normals.add(nz);
    }
}
