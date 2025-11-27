package jaredbgreat.climaticbiome.generation;

import java.lang.reflect.Field;
import java.util.Arrays;

import jaredbgreat.climaticbiome.generation.map.IMapRegistry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public final class ClimaticBiomeProviderTest {
    private ClimaticBiomeProviderTest() {}

    public static void main(String[] args) throws Exception {
        World world = new World();

        verifyAltChunksFlag(world, true);
        verifyAltChunksFlag(world, false);

        verifyAlignedGetBiomesUsesChunkGrid(world);
        verifyMisalignedGetBiomesUsesUnalignedGrid(world);
        verifySizeMismatchGetBiomesUsesUnalignedGrid(world);

        System.out.println("ClimaticBiomeProvider altChunks flag propagation test passed.");
    }

    private static void verifyAltChunksFlag(World world, boolean expected) throws Exception {
        ClimaticBiomeProvider provider = new ClimaticBiomeProvider(world, expected);
        if (provider.isAltChunks() != expected) {
            throw new AssertionError("Provider altChunks flag mismatch for value " + expected);
        }

        Object finder = getFinder(provider);
        boolean finderAltChunks = readAltChunksFlag(finder);
        if (finderAltChunks != expected) {
            throw new AssertionError("Finder altChunks flag mismatch for value " + expected);
        }
    }

    private static void verifyAlignedGetBiomesUsesChunkGrid(World world) throws Exception {
        ClimaticBiomeProvider provider = new ClimaticBiomeProvider(world, false);
        FakeMapRegistry registry = new FakeMapRegistry();
        setFinder(provider, registry);

        Biome[] result = provider.getBiomes(null, 16, 32, 16, 16, false);

        if (registry.chunkGridCalls != 1) {
            throw new AssertionError("Aligned request should call getChunkBiomeGrid once");
        }
        if (registry.unalignedGridCalls != 0) {
            throw new AssertionError("Aligned request should not call getUnalignedBiomeGrid");
        }
        assertFilledWith(result, FakeMapRegistry.CHUNK_BIOME);
    }

    private static void verifyMisalignedGetBiomesUsesUnalignedGrid(World world) throws Exception {
        ClimaticBiomeProvider provider = new ClimaticBiomeProvider(world, false);
        FakeMapRegistry registry = new FakeMapRegistry();
        setFinder(provider, registry);

        Biome[] result = provider.getBiomes(null, 8, 16, 16, 16, false);

        if (registry.chunkGridCalls != 0) {
            throw new AssertionError("Misaligned request should not call getChunkBiomeGrid");
        }
        if (registry.unalignedGridCalls != 1) {
            throw new AssertionError("Misaligned request should call getUnalignedBiomeGrid once");
        }
        assertFilledWith(result, FakeMapRegistry.UNALIGNED_BIOME);
    }

    private static void verifySizeMismatchGetBiomesUsesUnalignedGrid(World world) throws Exception {
        ClimaticBiomeProvider provider = new ClimaticBiomeProvider(world, false);
        FakeMapRegistry registry = new FakeMapRegistry();
        setFinder(provider, registry);

        Biome[] result = provider.getBiomes(null, 0, 0, 8, 8, false);

        if (registry.chunkGridCalls != 0) {
            throw new AssertionError("Non-16x16 request should not call getChunkBiomeGrid");
        }
        if (registry.unalignedGridCalls != 1) {
            throw new AssertionError("Non-16x16 request should call getUnalignedBiomeGrid once");
        }
        assertFilledWith(result, FakeMapRegistry.UNALIGNED_BIOME);
    }

    private static Object getFinder(ClimaticBiomeProvider provider) throws Exception {
        Field finderField = ClimaticBiomeProvider.class.getDeclaredField("finder");
        finderField.setAccessible(true);
        return finderField.get(provider);
    }

    private static void setFinder(ClimaticBiomeProvider provider, IMapRegistry registry) throws Exception {
        Field finderField = ClimaticBiomeProvider.class.getDeclaredField("finder");
        finderField.setAccessible(true);
        finderField.set(provider, registry);
    }

    private static boolean readAltChunksFlag(Object finder) throws Exception {
        Field altChunksField = finder.getClass().getDeclaredField("altChunks");
        altChunksField.setAccessible(true);
        return altChunksField.getBoolean(finder);
    }

    private static void assertFilledWith(Biome[] result, Biome expected) {
        if (!Arrays.stream(result).allMatch(expected::equals)) {
            throw new AssertionError("Unexpected biome values returned from getBiomes");
        }
    }

    private static final class FakeMapRegistry implements IMapRegistry {
        static final Biome CHUNK_BIOME = new Biome(new Biome.BiomeProperties("chunk")) {};
        static final Biome UNALIGNED_BIOME = new Biome(new Biome.BiomeProperties("unaligned")) {};

        int chunkGridCalls;
        int unalignedGridCalls;

        @Override
        public void findSaveDir() {}

        @Override
        public int chunkToMap(int c) {
            return 0;
        }

        @Override
        public int blockToMap(int c) {
            return 0;
        }

        @Override
        public Biome getBiomeChunk(int x, int z) {
            return CHUNK_BIOME;
        }

        @Override
        public Biome[] getChunkBiomeGrid(int x, int z, Biome[] in) {
            chunkGridCalls++;
            Arrays.fill(in, CHUNK_BIOME);
            return in;
        }

        @Override
        public Biome[] getUnalignedBiomeGrid(int x, int z, int h, int w, Biome[] in) {
            unalignedGridCalls++;
            Arrays.fill(in, UNALIGNED_BIOME);
            return in;
        }

        @Override
        public Biome[] getChunkBiomeGen(int x, int z, Biome[] in) {
            return getChunkBiomeGrid(x, z, in);
        }

        @Override
        public void cleanCaches() {}

        @Override
        public float[] getTerrainBiomeGen(int x, int z, float[] in) {
            return in;
        }

        @Override
        public float getBaseHeight(int x, int z) {
            return 0;
        }

        @Override
        public float getHeightScale(int x, int z) {
            return 0;
        }

        @Override
        public float[] getHeightData(int x, int z) {
            return new float[0];
        }
    }
}
