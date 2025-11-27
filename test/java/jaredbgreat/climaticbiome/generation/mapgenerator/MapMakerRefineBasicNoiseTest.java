package jaredbgreat.climaticbiome.generation.mapgenerator;

import java.lang.reflect.Method;

import jaredbgreat.climaticbiome.configuration.ClimaticWorldSettings;
import jaredbgreat.climaticbiome.util.SpatialHash;

/**
 * A focused harness to exercise the private refineBasicNoise routine.  The
 * method averages a 3x3 neighbourhood of noise values (including the center
 * cell), so its output should match the arithmetic mean of the surrounding
 * values.
 */
public class MapMakerRefineBasicNoiseTest {

    public static void main(String[] args) throws Exception {
        ClimaticWorldSettings settings = ClimaticWorldSettings.getQueued();
        MapMaker maker = new MapMaker(new SpatialHash(1L), new SpatialHash(2L),
                new SpatialHash(3L), settings);

        int size = MapMaker.RSIZE * maker.scale.whole;
        int[][] noise = buildNoiseGrid(size + 2);
        ChunkTile[] premap = new ChunkTile[size * size];

        int[] refined = invokeRefineBasicNoise(maker, noise, premap);

        validateAverage(noise, refined, size, 10, 20);
        validateAverage(noise, refined, size, 111, 197);

        System.out.println("MapMakerRefineBasicNoiseTest passed.");
    }

    private static int[][] buildNoiseGrid(int size) {
        int[][] noise = new int[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                noise[x][y] = (x * 1000) + y;
            }
        }
        return noise;
    }

    private static int[] invokeRefineBasicNoise(MapMaker maker, int[][] noise,
                                                ChunkTile[] premap) throws Exception {
        Method refine = MapMaker.class.getDeclaredMethod("refineBasicNoise",
                int[][].class, ChunkTile[].class);
        refine.setAccessible(true);
        return (int[]) refine.invoke(maker, noise, premap);
    }

    private static void validateAverage(int[][] noise, int[] refined, int size,
                                        int x, int y) {
        int expected = 0;
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                expected += noise[i][j];
            }
        }
        expected /= 9;

        int index = ((y - 1) * size) + (x - 1);
        if (refined[index] != expected) {
            throw new AssertionError(String.format(
                    "refineBasicNoise average mismatch at (%d,%d): %d vs %d",
                    x, y, refined[index], expected));
        }
    }
}
