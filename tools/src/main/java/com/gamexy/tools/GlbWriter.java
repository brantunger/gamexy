package com.gamexy.tools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Minimal, standards-compliant glTF 2.0 binary (.glb) container writer.
 */
public final class GlbWriter {

    private static final int GLTF_MAGIC = 0x46546C67; // "glTF"
    private static final int GLTF_VERSION = 2;
    private static final int CHUNK_JSON = 0x4E4F534A; // "JSON"
    private static final int CHUNK_BIN = 0x004E4942;  // "BIN\0"

    private GlbWriter() {}

    public static void writeGlb(Path outputPath, String modelName, MeshBuilder builder) throws IOException {
        List<PrimitiveData> prims = builder.getPrimitives();
        if (prims.isEmpty()) {
            throw new IllegalArgumentException("Cannot export empty mesh: " + modelName);
        }

        GltfBufferAccumulator buffers = new GltfBufferAccumulator();
        StringBuilder materialsJson = new StringBuilder();
        StringBuilder primitivesJson = new StringBuilder();

        for (int i = 0; i < prims.size(); i++) {
            PrimitiveData p = prims.get(i);
            if (i > 0) {
                materialsJson.append(',');
                primitivesJson.append(',');
            }
            materialsJson.append(serializeMaterial(p.material));

            int posAcc = buffers.writePositions(p.positions);
            int normAcc = buffers.writeNormals(p.normals);
            int idxAcc = buffers.writeIndices(p.indices);

            primitivesJson.append(String.format(Locale.US,
                "{\"attributes\":{\"POSITION\":%d,\"NORMAL\":%d},\"indices\":%d,\"material\":%d,\"mode\":4}",
                posAcc, normAcc, idxAcc, i));
        }

        byte[] binBytes = buffers.toAlignedBinaryBytes();
        String json = assembleGltfJson(modelName, primitivesJson.toString(), materialsJson.toString(),
            buffers.getAccessorsJson(), buffers.getBufferViewsJson(), binBytes.length);
        byte[] jsonBytes = padJsonChunk(json);

        writeGlbContainer(outputPath, jsonBytes, binBytes);
    }

    private static String serializeMaterial(MaterialSpec m) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US,
            "{\"name\":\"%s\",\"pbrMetallicRoughness\":{\"baseColorFactor\":[%.4f,%.4f,%.4f,%.4f],\"metallicFactor\":%.4f,\"roughnessFactor\":%.4f}",
            escape(m.name), m.r, m.g, m.b, m.a, m.metallic, m.roughness));
        if (m.hasEmission()) {
            sb.append(String.format(Locale.US, ",\"emissiveFactor\":[%.4f,%.4f,%.4f]", m.er, m.eg, m.eb));
        }
        if (m.isTranslucent()) {
            sb.append(",\"alphaMode\":\"BLEND\"");
        }
        sb.append('}');
        return sb.toString();
    }

    private static String assembleGltfJson(String modelName, String primitivesJson, String materialsJson,
                                           String accessorsJson, String bufferViewsJson, int binByteLength) {
        String safeName = escape(modelName);
        return String.format(Locale.US,
            "{\"asset\":{\"version\":\"2.0\",\"generator\":\"gamexy-asset-gen\"}," +
            "\"scene\":0,\"scenes\":[{\"name\":\"Scene\",\"nodes\":[0]}]," +
            "\"nodes\":[{\"name\":\"%s\",\"mesh\":0}]," +
            "\"meshes\":[{\"name\":\"%s\",\"primitives\":[%s]}]," +
            "\"materials\":[%s]," +
            "\"accessors\":[%s]," +
            "\"bufferViews\":[%s]," +
            "\"buffers\":[{\"byteLength\":%d}]}",
            safeName, safeName, primitivesJson, materialsJson, accessorsJson, bufferViewsJson, binByteLength);
    }

    private static byte[] padJsonChunk(String json) {
        byte[] rawJson = json.getBytes(StandardCharsets.UTF_8);
        int paddedLen = (rawJson.length + 3) & ~3;
        byte[] padded = new byte[paddedLen];
        System.arraycopy(rawJson, 0, padded, 0, rawJson.length);
        for (int i = rawJson.length; i < paddedLen; i++) {
            padded[i] = 0x20; // space padding per glTF 2.0 spec
        }
        return padded;
    }

    private static void writeGlbContainer(Path outputPath, byte[] jsonBytes, byte[] binBytes) throws IOException {
        int totalLength = 12 + 8 + jsonBytes.length + 8 + binBytes.length;
        ByteBuffer out = ByteBuffer.allocate(totalLength).order(ByteOrder.LITTLE_ENDIAN);
        out.putInt(GLTF_MAGIC);
        out.putInt(GLTF_VERSION);
        out.putInt(totalLength);

        out.putInt(jsonBytes.length);
        out.putInt(CHUNK_JSON);
        out.put(jsonBytes);

        out.putInt(binBytes.length);
        out.putInt(CHUNK_BIN);
        out.put(binBytes);

        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        Files.write(outputPath, out.array());
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
