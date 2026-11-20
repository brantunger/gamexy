package com.gamexy.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;

/**
 * Accumulates 4-byte aligned binary vertex/index buffers and matching glTF 2.0 bufferViews & accessors.
 */
final class GltfBufferAccumulator {

    private static final int TARGET_ARRAY_BUFFER = 34962;
    private static final int TARGET_ELEMENT_ARRAY_BUFFER = 34963;
    private static final int COMPONENT_FLOAT = 5126;
    private static final int COMPONENT_UNSIGNED_SHORT = 5123;

    private final ByteArrayOutputStream binStream = new ByteArrayOutputStream();
    private final StringBuilder bufferViewsJson = new StringBuilder();
    private final StringBuilder accessorsJson = new StringBuilder();

    private int bufferViewIndex = 0;
    private int accessorIndex = 0;

    int writePositions(List<Float> positions) throws IOException {
        int offset = align4();
        int vertCount = positions.size() / 3;
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;

        ByteBuffer buf = ByteBuffer.allocate(positions.size() * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (int v = 0; v < vertCount; v++) {
            float x = positions.get(v * 3);
            float y = positions.get(v * 3 + 1);
            float z = positions.get(v * 3 + 2);
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
            buf.putFloat(x).putFloat(y).putFloat(z);
        }
        binStream.write(buf.array());

        int bv = appendBufferView(offset, buf.capacity(), TARGET_ARRAY_BUFFER);
        return appendAccessor(String.format(Locale.US,
            "{\"bufferView\":%d,\"componentType\":%d,\"count\":%d,\"type\":\"VEC3\",\"min\":[%.5f,%.5f,%.5f],\"max\":[%.5f,%.5f,%.5f]}",
            bv, COMPONENT_FLOAT, vertCount, minX, minY, minZ, maxX, maxY, maxZ));
    }

    int writeNormals(List<Float> normals) throws IOException {
        int offset = align4();
        int vertCount = normals.size() / 3;
        ByteBuffer buf = ByteBuffer.allocate(normals.size() * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float f : normals) {
            buf.putFloat(f);
        }
        binStream.write(buf.array());

        int bv = appendBufferView(offset, buf.capacity(), TARGET_ARRAY_BUFFER);
        return appendAccessor(String.format(Locale.US,
            "{\"bufferView\":%d,\"componentType\":%d,\"count\":%d,\"type\":\"VEC3\"}",
            bv, COMPONENT_FLOAT, vertCount));
    }

    int writeIndices(List<Integer> indices) throws IOException {
        int offset = align4();
        ByteBuffer buf = ByteBuffer.allocate(indices.size() * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (int idx : indices) {
            buf.putShort((short) idx);
        }
        binStream.write(buf.array());

        int bv = appendBufferView(offset, buf.capacity(), TARGET_ELEMENT_ARRAY_BUFFER);
        return appendAccessor(String.format(Locale.US,
            "{\"bufferView\":%d,\"componentType\":%d,\"count\":%d,\"type\":\"SCALAR\"}",
            bv, COMPONENT_UNSIGNED_SHORT, indices.size()));
    }

    byte[] toAlignedBinaryBytes() {
        align4();
        return binStream.toByteArray();
    }

    String getBufferViewsJson() {
        return bufferViewsJson.toString();
    }

    String getAccessorsJson() {
        return accessorsJson.toString();
    }

    private int appendBufferView(int offset, int length, int target) {
        int index = bufferViewIndex++;
        if (bufferViewsJson.length() > 0) {
            bufferViewsJson.append(',');
        }
        bufferViewsJson.append(String.format(Locale.US,
            "{\"buffer\":0,\"byteOffset\":%d,\"byteLength\":%d,\"target\":%d}",
            offset, length, target));
        return index;
    }

    private int appendAccessor(String json) {
        int index = accessorIndex++;
        if (accessorsJson.length() > 0) {
            accessorsJson.append(',');
        }
        accessorsJson.append(json);
        return index;
    }

    private int align4() {
        int pad = (4 - (binStream.size() % 4)) % 4;
        for (int i = 0; i < pad; i++) {
            binStream.write(0);
        }
        return binStream.size();
    }
}
