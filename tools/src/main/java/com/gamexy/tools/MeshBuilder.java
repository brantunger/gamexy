package com.gamexy.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Procedural low-poly 3D mesh builder supporting multiple PBR materials and Euler transforms.
 * Produces flat-shaded triangles with outward normals ready for glTF 2.0 export.
 */
public class MeshBuilder {

    private final Map<String, PrimitiveData> primitives = new LinkedHashMap<>();

    public List<PrimitiveData> getPrimitives() {
        return new ArrayList<>(primitives.values());
    }

    private PrimitiveData primitiveFor(MaterialSpec mat) {
        return primitives.computeIfAbsent(mat.name, k -> new PrimitiveData(mat));
    }

    public void addBox(MaterialSpec mat, float cx, float cy, float cz,
                       float sx, float sy, float sz) {
        addBox(mat, cx, cy, cz, sx, sy, sz, 0f, 0f, 0f);
    }

    public void addBox(MaterialSpec mat, float cx, float cy, float cz,
                       float sx, float sy, float sz,
                       float rotXDeg, float rotYDeg, float rotZDeg) {
        float hx = sx * 0.5f, hy = sy * 0.5f, hz = sz * 0.5f;
        float[][] v = {
            transform(-hx, -hy,  hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg),
            transform( hx, -hy,  hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg),
            transform( hx,  hy,  hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg),
            transform(-hx,  hy,  hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg),
            transform(-hx, -hy, -hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg),
            transform( hx, -hy, -hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg),
            transform( hx,  hy, -hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg),
            transform(-hx,  hy, -hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg)
        };
        PrimitiveData p = primitiveFor(mat);
        p.addQuad(v[0], v[1], v[2], v[3]); // +Z front
        p.addQuad(v[5], v[4], v[7], v[6]); // -Z back
        p.addQuad(v[1], v[5], v[6], v[2]); // +X right
        p.addQuad(v[4], v[0], v[3], v[7]); // -X left
        p.addQuad(v[3], v[2], v[6], v[7]); // +Y top
        p.addQuad(v[4], v[5], v[1], v[0]); // -Y bottom
    }

    public void addCylinder(MaterialSpec mat, float cx, float cy, float cz,
                            float rBottom, float rTop, float height, int segments,
                            float rotXDeg, float rotYDeg, float rotZDeg) {
        float hy = height * 0.5f;
        PrimitiveData p = primitiveFor(mat);
        float[] bottomCenter = transform(0, -hy, 0, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg);
        float[] topCenter = transform(0, hy, 0, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg);
        float[][] bRing = buildRing(rBottom, -hy, segments, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg);
        float[][] tRing = buildRing(rTop, hy, segments, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg);

        addCylinderWalls(p, bRing, tRing, bottomCenter, topCenter, rBottom, rTop, segments);
        addCylinderCaps(p, bRing, tRing, bottomCenter, topCenter, rBottom, rTop, segments);
    }

    private static float[][] buildRing(float radius, float yOffset, int segments,
                                       float cx, float cy, float cz,
                                       float rotXDeg, float rotYDeg, float rotZDeg) {
        float[][] ring = new float[segments][];
        for (int i = 0; i < segments; i++) {
            double ang = (2.0 * Math.PI * i) / segments;
            float cos = (float) Math.cos(ang);
            float sin = (float) Math.sin(ang);
            ring[i] = transform(cos * radius, yOffset, sin * radius, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg);
        }
        return ring;
    }

    private static void addCylinderWalls(PrimitiveData p, float[][] bRing, float[][] tRing,
                                         float[] bottomCenter, float[] topCenter,
                                         float rBottom, float rTop, int segments) {
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            if (rBottom > 1e-5f && rTop > 1e-5f) {
                p.addQuad(bRing[next], bRing[i], tRing[i], tRing[next]);
            } else if (rTop <= 1e-5f) {
                p.addTriangle(bRing[next], bRing[i], topCenter);
            } else {
                p.addTriangle(bottomCenter, tRing[i], tRing[next]);
            }
        }
    }

    private static void addCylinderCaps(PrimitiveData p, float[][] bRing, float[][] tRing,
                                        float[] bottomCenter, float[] topCenter,
                                        float rBottom, float rTop, int segments) {
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            if (rTop > 1e-5f) {
                p.addTriangle(topCenter, tRing[next], tRing[i]);
            }
            if (rBottom > 1e-5f) {
                p.addTriangle(bottomCenter, bRing[i], bRing[next]);
            }
        }
    }

    public void addCone(MaterialSpec mat, float cx, float cy, float cz,
                        float radius, float height, int segments,
                        float rotXDeg, float rotYDeg, float rotZDeg) {
        addCylinder(mat, cx, cy, cz, radius, 0f, height, segments, rotXDeg, rotYDeg, rotZDeg);
    }

    public void addSphere(MaterialSpec mat, float cx, float cy, float cz,
                          float rx, float ry, float rz, int rings, int sectors) {
        PrimitiveData p = primitiveFor(mat);
        float[][][] grid = buildSphereGrid(cx, cy, cz, rx, ry, rz, rings, sectors);
        for (int r = 0; r < rings; r++) {
            for (int s = 0; s < sectors; s++) {
                int next = (s + 1) % sectors;
                float[] p00 = grid[r][s];
                float[] p01 = grid[r][next];
                float[] p10 = grid[r + 1][s];
                float[] p11 = grid[r + 1][next];
                if (r == 0) {
                    p.addTriangle(p00, p11, p10);
                } else if (r == rings - 1) {
                    p.addTriangle(p00, p01, p10);
                } else {
                    p.addQuad(p00, p01, p11, p10);
                }
            }
        }
    }

    private static float[][][] buildSphereGrid(float cx, float cy, float cz,
                                               float rx, float ry, float rz, int rings, int sectors) {
        float[][][] grid = new float[rings + 1][sectors][];
        for (int r = 0; r <= rings; r++) {
            double phi = Math.PI * r / rings;
            float y = (float) Math.cos(phi) * ry;
            float ringSin = (float) Math.sin(phi);
            for (int s = 0; s < sectors; s++) {
                double theta = 2.0 * Math.PI * s / sectors;
                float x = ringSin * (float) Math.cos(theta) * rx;
                float z = ringSin * (float) Math.sin(theta) * rz;
                grid[r][s] = new float[]{cx + x, cy + y, cz + z};
            }
        }
        return grid;
    }

    private static float[] transform(float x, float y, float z,
                                     float tx, float ty, float tz,
                                     float rotXDeg, float rotYDeg, float rotZDeg) {
        if (rotXDeg != 0f) {
            double rx = Math.toRadians(rotXDeg);
            float c = (float) Math.cos(rx), s = (float) Math.sin(rx);
            float ny = y * c - z * s;
            float nz = y * s + z * c;
            y = ny;
            z = nz;
        }
        if (rotYDeg != 0f) {
            double ry = Math.toRadians(rotYDeg);
            float c = (float) Math.cos(ry), s = (float) Math.sin(ry);
            float nx = x * c + z * s;
            float nz = -x * s + z * c;
            x = nx;
            z = nz;
        }
        if (rotZDeg != 0f) {
            double rz = Math.toRadians(rotZDeg);
            float c = (float) Math.cos(rz), s = (float) Math.sin(rz);
            float nx = x * c - y * s;
            float ny = x * s + y * c;
            x = nx;
            y = ny;
        }
        return new float[]{x + tx, y + ty, z + tz};
    }
}
