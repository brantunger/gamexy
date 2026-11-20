package com.gamexy.assetgen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Procedural low-poly 3D mesh builder supporting multiple PBR materials and transforms.
 * Produces flat-shaded triangles with accurate outward normals ready for glTF 2.0 export.
 */
public class MeshBuilder {

    public static final class MaterialSpec {
        public final String name;
        public final float r, g, b, a;
        public final float metallic;
        public final float roughness;
        public final float er, eg, eb;

        public MaterialSpec(String name, float r, float g, float b, float metallic, float roughness) {
            this(name, r, g, b, 1f, metallic, roughness, 0f, 0f, 0f);
        }

        public MaterialSpec(String name, float r, float g, float b, float a,
                            float metallic, float roughness,
                            float er, float eg, float eb) {
            this.name = name;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.metallic = metallic;
            this.roughness = roughness;
            this.er = er;
            this.eg = eg;
            this.eb = eb;
        }
    }

    public static final class PrimitiveData {
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

    private final Map<String, PrimitiveData> primitives = new LinkedHashMap<>();

    public List<PrimitiveData> getPrimitives() {
        return new ArrayList<>(primitives.values());
    }

    private PrimitiveData prim(MaterialSpec mat) {
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
            transform(-hx, -hy,  hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg), // 0
            transform( hx, -hy,  hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg), // 1
            transform( hx,  hy,  hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg), // 2
            transform(-hx,  hy,  hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg), // 3
            transform(-hx, -hy, -hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg), // 4
            transform( hx, -hy, -hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg), // 5
            transform( hx,  hy, -hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg), // 6
            transform(-hx,  hy, -hz, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg)  // 7
        };
        PrimitiveData p = prim(mat);
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
        PrimitiveData p = prim(mat);
        float[] bottomCenter = transform(0, -hy, 0, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg);
        float[] topCenter = transform(0, hy, 0, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg);

        float[][] bRing = new float[segments][];
        float[][] tRing = new float[segments][];
        for (int i = 0; i < segments; i++) {
            double ang = (2.0 * Math.PI * i) / segments;
            float cos = (float) Math.cos(ang);
            float sin = (float) Math.sin(ang);
            bRing[i] = transform(cos * rBottom, -hy, sin * rBottom, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg);
            tRing[i] = transform(cos * rTop, hy, sin * rTop, cx, cy, cz, rotXDeg, rotYDeg, rotZDeg);
        }

        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            // Side wall (counter-clockwise when viewed from outside)
            if (rBottom > 1e-5f && rTop > 1e-5f) {
                p.addQuad(bRing[next], bRing[i], tRing[i], tRing[next]);
            } else if (rTop <= 1e-5f) {
                p.addTriangle(bRing[next], bRing[i], topCenter);
            } else {
                p.addTriangle(bottomCenter, tRing[i], tRing[next]);
            }
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
        PrimitiveData p = prim(mat);
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

    private static float[] transform(float x, float y, float z,
                                     float tx, float ty, float tz,
                                     float rotXDeg, float rotYDeg, float rotZDeg) {
        double rx = Math.toRadians(rotXDeg);
        double ry = Math.toRadians(rotYDeg);
        double rz = Math.toRadians(rotZDeg);

        // Rotate X
        if (rotXDeg != 0f) {
            float c = (float) Math.cos(rx), s = (float) Math.sin(rx);
            float ny = y * c - z * s;
            float nz = y * s + z * c;
            y = ny;
            z = nz;
        }
        // Rotate Y
        if (rotYDeg != 0f) {
            float c = (float) Math.cos(ry), s = (float) Math.sin(ry);
            float nx = x * c + z * s;
            float nz = -x * s + z * c;
            x = nx;
            z = nz;
        }
        // Rotate Z
        if (rotZDeg != 0f) {
            float c = (float) Math.cos(rz), s = (float) Math.sin(rz);
            float nx = x * c - y * s;
            float ny = x * s + y * c;
            x = nx;
            y = ny;
        }

        return new float[]{x + tx, y + ty, z + tz};
    }
}
