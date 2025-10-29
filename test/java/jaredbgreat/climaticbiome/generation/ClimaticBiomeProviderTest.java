package jaredbgreat.climaticbiome.generation;

import java.lang.reflect.Field;

import net.minecraft.world.World;

public final class ClimaticBiomeProviderTest {
    private ClimaticBiomeProviderTest() {}

    public static void main(String[] args) throws Exception {
        World world = new World();

        verifyAltChunksFlag(world, true);
        verifyAltChunksFlag(world, false);

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

    private static Object getFinder(ClimaticBiomeProvider provider) throws Exception {
        Field finderField = ClimaticBiomeProvider.class.getDeclaredField("finder");
        finderField.setAccessible(true);
        return finderField.get(provider);
    }

    private static boolean readAltChunksFlag(Object finder) throws Exception {
        Field altChunksField = finder.getClass().getDeclaredField("altChunks");
        altChunksField.setAccessible(true);
        return altChunksField.getBoolean(finder);
    }
}
