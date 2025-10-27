package jaredbgreat.climaticbiome.util;

import java.util.Arrays;

/**
 * Simple assertions verifying ModMath utility behaviour without requiring
 * the broader Minecraft runtime.  The tests focus on the chunk and block
 * indexing behaviour used by MapRegistry implementations.
 */
public class ModMathTest {
    private static final int RSIZE = 4096 / 16; // Mirrors MapMaker.RSIZE.
    private static final int[] TEST_COORDS = {
            -2050, -1537, -1025, -769, -513, -385, -384, -383, -129, -1,
            0, 1, 127, 255, 383, 384, 385, 512, 768, 1024, 1536, 2048
    };

    public static void main(String[] args) {
        validateModulusAgainstFloorMod();
        validateBiomeIndexingForScale(3);
        validateBiomeIndexingForScale(4);
        validatePeriodicConsistency(3);
        validatePeriodicConsistency(4);
        System.out.println("All ModMath tests passed.");
    }

    private static void validateModulusAgainstFloorMod() {
        for (int divisor : Arrays.asList(3, 16, 127, 768, 1024, 4096)) {
            for (int dividend : TEST_COORDS) {
                int expected = Math.floorMod(dividend, divisor);
                int actual = ModMath.modRight(dividend, divisor);
                assert actual == expected :
                        String.format("modRight(%d, %d) produced %d; expected %d", dividend, divisor, actual, expected);
            }
        }
    }

    private static void validateBiomeIndexingForScale(int scale) {
        int cWidth = RSIZE * scale;
        int cOffset = cWidth / 2;
        int dataSize = cWidth * cWidth;

        for (int x : TEST_COORDS) {
            for (int z : TEST_COORDS) {
                int index = computeIndex(x, z, cWidth, cOffset);
                int expectedIndex = Math.floorMod(x + cOffset, cWidth) * cWidth
                        + Math.floorMod(z + cOffset, cWidth);
                assert index == expectedIndex :
                        String.format("Scale %d index mismatch for (%d,%d): %d != %d", scale, x, z, index, expectedIndex);
                assert index >= 0 && index < dataSize :
                        String.format("Scale %d produced out-of-range index %d for (%d,%d)", scale, index, x, z);
            }
        }
    }

    private static void validatePeriodicConsistency(int scale) {
        int cWidth = RSIZE * scale;
        int cOffset = cWidth / 2;

        for (int baseX : TEST_COORDS) {
            for (int baseZ : TEST_COORDS) {
                int reference = computeIndex(baseX, baseZ, cWidth, cOffset);
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        int shiftedX = baseX + (dx * cWidth);
                        int shiftedZ = baseZ + (dz * cWidth);
                        int index = computeIndex(shiftedX, shiftedZ, cWidth, cOffset);
                        assert index == reference :
                                String.format("Scale %d mismatch for shifted coords (%d,%d) vs (%d,%d)",
                                        scale, baseX, baseZ, shiftedX, shiftedZ);
                    }
                }
            }
        }
    }

    private static int computeIndex(int x, int z, int cWidth, int cOffset) {
        int modX = ModMath.modRight(x + cOffset, cWidth);
        int modZ = ModMath.modRight(z + cOffset, cWidth);
        return (modX * cWidth) + modZ;
    }
}
