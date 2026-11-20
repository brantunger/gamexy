package com.gamexy.math;

import com.badlogic.gdx.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AngleMathTest {

    @Test
    void shortestAngleDiffWrapsAcrossZeroAndThreeSixty() {
        assertEquals(20f, AngleMath.shortestAngleDiff(350f, 10f), 1e-4f);
        assertEquals(-20f, AngleMath.shortestAngleDiff(10f, 350f), 1e-4f);
        assertEquals(90f, AngleMath.shortestAngleDiff(0f, 90f), 1e-4f);
        assertEquals(-90f, AngleMath.shortestAngleDiff(0f, 270f), 1e-4f);
    }

    @Test
    void distanceXZIgnoresVerticalAxis() {
        Vector3 a = new Vector3(1f, 0f, 2f);
        Vector3 b = new Vector3(4f, 99f, 6f);
        assertEquals(25f, AngleMath.distanceSquaredXZ(a, b), 1e-4f);
        assertEquals(5f, AngleMath.distanceXZ(a, b), 1e-4f);
    }

    @Test
    void facingDegreesToComputesCardinalAngles() {
        Vector3 origin = new Vector3(0f, 0f, 0f);
        assertEquals(0f, AngleMath.facingDegreesTo(origin, new Vector3(0f, 0f, 5f)), 1e-4f);
        assertEquals(90f, AngleMath.facingDegreesTo(origin, new Vector3(5f, 0f, 0f)), 1e-4f);
        assertEquals(-90f, AngleMath.facingDegreesTo(origin, new Vector3(-5f, 0f, 0f)), 1e-4f);
    }
}
