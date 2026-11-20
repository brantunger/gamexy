package com.gamexy.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlbWriterTest {

    @Test
    void writesValidFourByteAlignedGlbHeaderAndChunks(@TempDir Path tempDir) throws IOException {
        Path out = tempDir.resolve("crate.glb");
        GlbWriter.writeGlb(out, "TestCrate", PropModelFactory.createCrate());

        byte[] bytes = Files.readAllBytes(out);
        assertEquals(0, bytes.length % 4, "GLB total size must be 4-byte aligned");

        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x46546C67, buf.getInt(), "glTF magic header");
        assertEquals(2, buf.getInt(), "glTF version 2");
        assertEquals(bytes.length, buf.getInt(), "declared file length");

        int jsonLen = buf.getInt();
        assertEquals(0x4E4F534A, buf.getInt(), "JSON chunk type");
        assertEquals(0, jsonLen % 4, "JSON chunk must be 4-byte aligned");

        byte[] jsonBytes = new byte[jsonLen];
        buf.get(jsonBytes);
        String json = new String(jsonBytes, StandardCharsets.UTF_8);
        assertTrue(json.contains("\"TestCrate\""));
        assertTrue(json.contains("\"CrateWood\""));

        int binLen = buf.getInt();
        assertEquals(0x004E4942, buf.getInt(), "BIN chunk type");
        assertEquals(0, binLen % 4, "BIN chunk must be 4-byte aligned");
        assertEquals(binLen, buf.remaining());
    }

    @Test
    void rejectsEmptyMesh(@TempDir Path tempDir) {
        assertThrows(IllegalArgumentException.class,
            () -> GlbWriter.writeGlb(tempDir.resolve("empty.glb"), "Empty", new MeshBuilder()));
    }
}
