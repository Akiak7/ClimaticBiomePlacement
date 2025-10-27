package jaredbgreat.climaticbiome.generation.map;

import java.util.HashMap;
import java.util.Map;

import jaredbgreat.climaticbiome.generation.mapgenerator.BiomeBasin;

/**
 * Thread-local buffers used by {@link MapRegistry} to avoid repeated
 * allocations of small working arrays during biome lookups.
 */
final class BasinBufferPool {
    private static final ThreadLocal<BasinBufferPool> LOCAL =
            ThreadLocal.withInitial(BasinBufferPool::new);

    static BasinBufferPool local() {
        return LOCAL.get();
    }

    private int[] chunkTiles;
    private int[] chunkGenTiles;
    private final Map<Integer, int[]> intArrays = new HashMap<>();

    private BiomeBasin[][] chunkBasins;
    private BiomeBasin[][] chunkGenBasins;
    private final Map<Long, BiomeBasin[][]> basinGrids = new HashMap<>();
    private final Map<Long, BiomeBasin[][]> basinGenGrids = new HashMap<>();

    private BasinBufferPool() {
    }

    int[] acquireChunkTiles() {
        if(chunkTiles == null) {
            chunkTiles = new int[9];
        }
        return chunkTiles;
    }

    int[] acquireChunkGenTiles() {
        if(chunkGenTiles == null) {
            chunkGenTiles = new int[9];
        }
        return chunkGenTiles;
    }

    int[] acquireIntArray(int size) {
        int[] array = intArrays.get(size);
        if(array == null) {
            array = new int[size];
            intArrays.put(size, array);
        }
        return array;
    }

    BiomeBasin[][] acquireChunkBasins() {
        if(chunkBasins == null) {
            chunkBasins = new BiomeBasin[3][3];
        }
        return chunkBasins;
    }

    BiomeBasin[][] acquireChunkGenBasins() {
        if(chunkGenBasins == null) {
            chunkGenBasins = new BiomeBasin[3][3];
        }
        return chunkGenBasins;
    }

    BiomeBasin[][] acquireBasinGrid(int rows, int cols) {
        long key = (((long)rows) << 32) | (cols & 0xffffffffL);
        BiomeBasin[][] grid = basinGrids.get(key);
        if(grid == null) {
            grid = new BiomeBasin[rows][cols];
            basinGrids.put(key, grid);
        }
        return grid;
    }

    BiomeBasin[][] acquireBasinGenGrid(int rows, int cols) {
        long key = (((long)rows) << 32) | (cols & 0xffffffffL);
        BiomeBasin[][] grid = basinGenGrids.get(key);
        if(grid == null) {
            grid = new BiomeBasin[rows][cols];
            basinGenGrids.put(key, grid);
        }
        return grid;
    }
}
