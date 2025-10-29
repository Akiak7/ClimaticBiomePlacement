package jaredbgreat.climaticbiome.generation.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Lightweight harness that simulates loading JEID save data without the full
 * Minecraft runtime.  It mirrors the byte-assembly loops used by
 * {@link NewMapRegistry#readMap(NewRegionMap)} and
 * {@link NewMapRegistry#convertMap(NewRegionMap, File)} to ensure that upper
 * bytes are deserialised without sign-extension.
 */
public final class NewMapRegistryLoadSimulation {
    private NewMapRegistryLoadSimulation() {
    }

    public static void main(String[] args) throws Exception {
        simulateModernSaveLoad();
        simulateLegacySaveLoad();

        System.out.println("JEID save load simulation completed successfully.");
    }

    private static void simulateModernSaveLoad()
            throws IOException {
        long[] expected = {
                0x0123_4567_89L,
                0x00AB_CDEF_12L,
                0x0000_0000_01L
        };
        File file = File.createTempFile("jeid-modern", ".cbmap");
        file.deleteOnExit();

        try(FileOutputStream out = new FileOutputStream(file)) {
            for(long value : expected) {
                out.write((int)(value & 0xffL));
                out.write((int)((value >> 8) & 0xffL));
                out.write((int)((value >> 16) & 0xffL));
                out.write((int)((value >> 24) & 0xffL));
                out.write((int)((value >> 32) & 0xffL));
            }
        }

        long[] actual = new long[expected.length];
        try(FileInputStream in = new FileInputStream(file)) {
            for(int i = 0; i < actual.length; i++) {
                long b0 = readUnsignedByte(in);
                long b1 = readUnsignedByte(in);
                long b2 = readUnsignedByte(in);
                long b3 = readUnsignedByte(in);
                long b4 = readUnsignedByte(in);
                actual[i] = b0 | (b1 << 8) | (b2 << 16) | (b3 << 24) | (b4 << 32);
            }
        }

        if(!Arrays.equals(expected, actual)) {
            throw new IllegalStateException("Modern JEID load mismatch: "
                    + Arrays.toString(actual) + " vs " + Arrays.toString(expected));
        }
    }

    private static void simulateLegacySaveLoad()
            throws IOException {
        long[] expected = {
                (0x34L << 32) | 0x12L,
                (0xAAL << 32) | 0x55L
        };
        File file = File.createTempFile("jeid-legacy", ".cbmap");
        file.deleteOnExit();

        try(FileOutputStream out = new FileOutputStream(file)) {
            for(long value : expected) {
                out.write((int)(value & 0xffL));
                out.write((int)((value >> 32) & 0xffL));
            }
        }

        long[] actual = new long[expected.length];
        try(FileInputStream in = new FileInputStream(file)) {
            for(int i = 0; i < actual.length; i++) {
                long lower = readUnsignedByte(in);
                long upper = readUnsignedByte(in);
                actual[i] = lower | (upper << 32);
            }
        }

        if(!Arrays.equals(expected, actual)) {
            throw new IllegalStateException("Legacy JEID load mismatch: "
                    + Arrays.toString(actual) + " vs " + Arrays.toString(expected));
        }
    }

    private static long readUnsignedByte(FileInputStream fs) throws IOException {
        int value = fs.read();
        if(value == -1) {
            throw new IOException("Unexpected end of stream during JEID load simulation");
        }
        return ((long)value) & 0xffL;
    }
}
