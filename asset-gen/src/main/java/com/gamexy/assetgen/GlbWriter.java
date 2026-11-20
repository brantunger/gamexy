package com.gamexy.assetgen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Minimal, standards-compliant glTF 2.0 binary (.glb) writer.
 * Exports multi-material meshes with PBR Metallic-Roughness materials,
 * positions, normals, and triangle indices.
 */
public final class GlbWriter {

    private static final int GLTF_MAGIC = 0x46546C67; // "glTF"
    private static final int GLTF_VERSION = 2;
    private static final int CHUNK_JSON = 0x4E4F534A; // "JSON"
    private static final int CHUNK_BIN = 0x004E4942;  // "BIN\0"

    private GlbWriter() {}

    public static void writeGlb(Path outputPath, String modelName, MeshBuilder builder) throws IOException {
        List<MeshBuilder.PrimitiveData> prims = builder.getPrimitives();
        if (prims.isEmpty()) {
            throw new IllegalArgumentException("Cannot export empty mesh: " + modelName);
        }

        ByteArrayOutputStream binStream = new ByteArrayOutputStream();
        StringBuilder materialsJson = new StringBuilder();
        StringBuilder bufferViewsJson = new StringBuilder();
        StringBuilder accessorsJson = new StringBuilder();
        StringBuilder primitivesJson = new StringBuilder();

        int bvIndex = 0;
        int accIndex = 0;

        for (int i = 0; i < prims.size(); i++) {
            MeshBuilder.PrimitiveData p = prims.get(i);
            MeshBuilder.MaterialSpec m = p.material;

            if (i > 0) {
                materialsJson.append(',');
                primitivesJson.append(',');
            }

            // Material JSON
            materialsJson.append(String.format(Locale.US,
                "{\"name\":\"%s\",\"pbrMetallicRoughness\":{\"baseColorFactor\":[%.4f,%.4f,%.4f,%.4f],\"metallicFactor\":%.4f,\"roughnessFactor\":%.4f}",
                escape(m.name), m.r, m.g, m.b, m.a, m.metallic, m.roughness));
            if (m.er > 0f || m.eg > 0f || m.eb > 0f) {
                materialsJson.append(String.format(Locale.US,
                    ",\"emissiveFactor\":[%.4f,%.4f,%.4f]", m.er, m.eg, m.eb));
            }
            if (m.a < 0.999f) {
                materialsJson.append(",\"alphaMode\":\"BLEND\"");
            }
            materialsJson.append('}');

            // Write POSITIONS
            int posOffset = align4(binStream);
            int vertCount = p.positions.size() / 3;
            float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
            ByteBuffer posBuf = ByteBuffer.allocate(p.positions.size() * 4).order(ByteOrder.LITTLE_ENDIAN);
            for (int v = 0; v < vertCount; v++) {
                float x = p.positions.get(v * 3);
                float y = p.positions.get(v * 3 + 1);
                float z = p.positions.get(v * 3 + 2);
                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (z < minZ) minZ = z;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
                if (z > maxZ) maxZ = z;
                posBuf.putFloat(x).putFloat(y).putFloat(z);
            }
            binStream.write(posBuf.array());
            int posLen = posBuf.capacity();
            int posBv = bvIndex++;
            appendCommaIfNeeded(bufferViewsJson);
            bufferViewsJson.append(String.format(Locale.US,
                "{\"buffer\":0,\"byteOffset\":%d,\"byteLength\":%d,\"target\":34962}",
                posOffset, posLen));
            int posAcc = accIndex++;
            appendCommaIfNeeded(accessorsJson);
            accessorsJson.append(String.format(Locale.US,
                "{\"bufferView\":%d,\"componentType\":5126,\"count\":%d,\"type\":\"VEC3\",\"min\":[%.5f,%.5f,%.5f],\"max\":[%.5f,%.5f,%.5f]}",
                posBv, vertCount, minX, minY, minZ, maxX, maxY, maxZ));

            // Write NORMALS
            int normOffset = align4(binStream);
            ByteBuffer normBuf = ByteBuffer.allocate(p.normals.size() * 4).order(ByteOrder.LITTLE_ENDIAN);
            for (float f : p.normals) {
                normBuf.putFloat(f);
            }
            binStream.write(normBuf.array());
            int normLen = normBuf.capacity();
            int normBv = bvIndex++;
            appendCommaIfNeeded(bufferViewsJson);
            bufferViewsJson.append(String.format(Locale.US,
                "{\"buffer\":0,\"byteOffset\":%d,\"byteLength\":%d,\"target\":34962}",
                normOffset, normLen));
            int normAcc = accIndex++;
            appendCommaIfNeeded(accessorsJson);
            accessorsJson.append(String.format(Locale.US,
                "{\"bufferView\":%d,\"componentType\":5126,\"count\":%d,\"type\":\"VEC3\"}",
                normBv, vertCount));

            // Write INDICES (UNSIGNED_SHORT = 5123 for native libGDX / gdx-gltf support)
            int idxOffset = align4(binStream);
            ByteBuffer idxBuf = ByteBuffer.allocate(p.indices.size() * 2).order(ByteOrder.LITTLE_ENDIAN);
            for (int idx : p.indices) {
                idxBuf.putShort((short) idx);
            }
            binStream.write(idxBuf.array());
            int idxLen = idxBuf.capacity();
            int idxBv = bvIndex++;
            appendCommaIfNeeded(bufferViewsJson);
            bufferViewsJson.append(String.format(Locale.US,
                "{\"buffer\":0,\"byteOffset\":%d,\"byteLength\":%d,\"target\":34963}",
                idxOffset, idxLen));
            int idxAcc = accIndex++;
            appendCommaIfNeeded(accessorsJson);
            accessorsJson.append(String.format(Locale.US,
                "{\"bufferView\":%d,\"componentType\":5123,\"count\":%d,\"type\":\"SCALAR\"}",
                idxBv, p.indices.size()));

            // Primitive JSON
            primitivesJson.append(String.format(Locale.US,
                "{\"attributes\":{\"POSITION\":%d,\"NORMAL\":%d},\"indices\":%d,\"material\":%d,\"mode\":4}",
                posAcc, normAcc, idxAcc, i));
        }

        align4(binStream);
        byte[] binBytes = binStream.toByteArray();

        String json = String.format(Locale.US,
            "{\"asset\":{\"version\":\"2.0\",\"generator\":\"gamexy-asset-gen\"}," +
            "\"scene\":0,\"scenes\":[{\"name\":\"Scene\",\"nodes\":[0]}]," +
            "\"nodes\":[{\"name\":\"%s\",\"mesh\":0}]," +
            "\"meshes\":[{\"name\":\"%s\",\"primitives\":[%s]}]," +
            "\"materials\":[%s]," +
            "\"accessors\":[%s]," +
            "\"bufferViews\":[%s]," +
            "\"buffers\":[{\"byteLength\":%d}]}",
            escape(modelName), escape(modelName),
            primitivesJson, materialsJson, accessorsJson, bufferViewsJson, binBytes.length);

        byte[] rawJson = json.getBytes(StandardCharsets.UTF_8);
        int paddedJsonLen = (rawJson.length + 3) & ~3;
        byte[] jsonBytes = new byte[paddedJsonLen];
        System.arraycopy(rawJson, 0, jsonBytes, 0, rawJson.length);
        for (int i = rawJson.length; i < paddedJsonLen; i++) {
            jsonBytes[i] = 0x20; // space padding per glTF 2.0 spec
        }

        int totalLength = 12 + 8 + jsonBytes.length + 8 + binBytes.length;
        ByteBuffer out = ByteBuffer.allocate(totalLength).order(ByteOrder.LITTLE_ENDIAN);
        // Header
        out.putInt(GLTF_MAGIC);
        out.putInt(GLTF_VERSION);
        out.putInt(totalLength);
        // JSON Chunk
        out.putInt(jsonBytes.length);
        out.putInt(CHUNK_JSON);
        out.put(jsonBytes);
        // BIN Chunk
        out.putInt(binBytes.length);
        out.putInt(CHUNK_BIN);
        out.put(binBytes);

        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, out.array());
    }

    private static int align4(ByteArrayOutputStream stream) {
        int pad = (4 - (stream.size() % 4)) % 4;
        for (int i = 0; i < pad; i++) {
            stream.write(0);
        }
        return stream.size();
    }

    private static void appendCommaIfNeeded(StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append(',');
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
