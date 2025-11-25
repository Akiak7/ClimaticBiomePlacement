import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Synthetic profiler that mirrors the allocation patterns of MapRegistry's
 * biome grid helpers.  It compares the legacy per-call allocations against
 * the new pooled-buffer approach so we can quantify allocation savings
 * without needing a full Minecraft runtime.
 */
public final class MapRegistryBufferProfiler {
    private static final int ITERATIONS = 5000;

    private MapRegistryBufferProfiler() {
    }

    public static void main(String[] args) {
        int iterations = (args.length > 0) ? Integer.parseInt(args[0]) : ITERATIONS;
        AllocationCounters legacyCounters = new AllocationCounters();
        long legacyStart = System.nanoTime();
        long legacyChecksum = runLegacy(iterations, legacyCounters);
        long legacyTime = System.nanoTime() - legacyStart;

        AllocationCounters pooledCounters = new AllocationCounters();
        long pooledStart = System.nanoTime();
        long pooledChecksum = runPooled(iterations, pooledCounters);
        long pooledTime = System.nanoTime() - pooledStart;

        if(legacyChecksum != pooledChecksum) {
            throw new IllegalStateException("Mismatched checksums: "
                    + legacyChecksum + " vs " + pooledChecksum);
        }

        System.out.println("Iterations: " + iterations);
        System.out.println("Legacy int[] allocations: " + legacyCounters.intArrays);
        System.out.println("Legacy basin[][] allocations: " + legacyCounters.basinArrays);
        System.out.println("Pooled int[] allocations: " + pooledCounters.intArrays);
        System.out.println("Pooled basin[][] allocations: " + pooledCounters.basinArrays);
        System.out.println("Legacy elapsed (ms): " + legacyTime / 1_000_000.0);
        System.out.println("Pooled elapsed (ms): " + pooledTime / 1_000_000.0);
    }

    private static long runLegacy(int iterations, AllocationCounters counters) {
        Random rng = new Random(9001L);
        long checksum = 0L;
        for(int i = 0; i < iterations; i++) {
            checksum += legacyChunkBiomeGrid(rng, counters, i);
            checksum += legacyChunkBiomeGen(rng, counters, i);
            checksum += legacyBiomeGrid(rng, counters, i);
        }
        return checksum;
    }

    private static long runPooled(int iterations, AllocationCounters counters) {
        Random rng = new Random(9001L);
        SimpleBufferPool pool = new SimpleBufferPool();
        long checksum = 0L;
        for(int i = 0; i < iterations; i++) {
            checksum += pooledChunkBiomeGrid(rng, counters, pool, i);
            checksum += pooledChunkBiomeGen(rng, counters, pool, i);
            checksum += pooledBiomeGrid(rng, counters, pool, i);
        }
        return checksum;
    }

    private static long legacyChunkBiomeGrid(Random rng, AllocationCounters counters, int iteration) {
        int[] tiles = counters.newIntArray(9);
        SimpleBiomeBasin[][] basins = counters.newBasinGrid(3, 3);
        fillChunkBasins(rng, tiles, basins, iteration);
        return sumChunk(basins);
    }

    private static long legacyChunkBiomeGen(Random rng, AllocationCounters counters, int iteration) {
        int[] tiles = counters.newIntArray(9);
        SimpleBiomeBasin[][] basins = counters.newBasinGrid(3, 3);
        fillChunkBasins(rng, tiles, basins, iteration);
        return sumChunk(basins);
    }

    private static long legacyBiomeGrid(Random rng, AllocationCounters counters, int iteration) {
        int h = 16 * ((iteration % 4) + 1);
        int w = 16 * (((iteration + 2) % 4) + 1);
        int ch = ((h - 1) / 16) + 3;
        int cw = ((w - 1) / 16) + 3;
        int numc = ch * cw;

        int[] tiles = counters.newIntArray(numc);
        SimpleBiomeBasin[][] basins = counters.newBasinGrid(ch, cw);
        fillGridBasins(rng, tiles, basins, ch, cw);
        return sumGrid(basins, h, w);
    }

    private static long pooledChunkBiomeGrid(Random rng, AllocationCounters counters,
            SimpleBufferPool pool, int iteration) {
        int[] tiles = pool.acquireChunkTiles(counters);
        SimpleBiomeBasin[][] basins = pool.acquireChunkBasins(counters);
        fillChunkBasins(rng, tiles, basins, iteration);
        return sumChunk(basins);
    }

    private static long pooledChunkBiomeGen(Random rng, AllocationCounters counters,
            SimpleBufferPool pool, int iteration) {
        int[] tiles = pool.acquireChunkGenTiles(counters);
        SimpleBiomeBasin[][] basins = pool.acquireChunkGenBasins(counters);
        fillChunkBasins(rng, tiles, basins, iteration);
        return sumChunk(basins);
    }

    private static long pooledBiomeGrid(Random rng, AllocationCounters counters,
            SimpleBufferPool pool, int iteration) {
        int h = 16 * ((iteration % 4) + 1);
        int w = 16 * (((iteration + 2) % 4) + 1);
        int ch = ((h - 1) / 16) + 3;
        int cw = ((w - 1) / 16) + 3;
        int numc = ch * cw;

        int[] tiles = pool.acquireIntArray(numc, counters);
        SimpleBiomeBasin[][] basins = pool.acquireBasinGrid(ch, cw, counters);
        fillGridBasins(rng, tiles, basins, ch, cw);
        return sumGrid(basins, h, w);
    }

    private static void fillChunkBasins(Random rng, int[] tiles, SimpleBiomeBasin[][] basins,
            int iteration) {
        for(int i = 0; i < tiles.length; i++) {
            int x1 = i / 3;
            int z1 = i % 3;
            int x2 = iteration + x1;
            int z2 = iteration + z1;
            tiles[i] = rng.nextInt();
            basins[x1][z1] = new SimpleBiomeBasin(
                    (x1 * 16) + rng.nextInt(16),
                    (z1 * 16) + rng.nextInt(16),
                    tiles[i], 1.0 + rng.nextDouble());
        }
    }

    private static void fillGridBasins(Random rng, int[] tiles, SimpleBiomeBasin[][] basins,
            int ch, int cw) {
        for(int i = 0; i < tiles.length; i++) {
            int x1 = i / cw;
            int z1 = i % cw;
            int x2 = x1;
            int z2 = z1;
            tiles[i] = rng.nextInt();
            basins[x1][z1] = new SimpleBiomeBasin(
                    (x1 * 16) + rng.nextInt(16),
                    (z1 * 16) + rng.nextInt(16),
                    tiles[i], 1.0 + rng.nextDouble());
        }
    }

    private static long sumChunk(SimpleBiomeBasin[][] basins) {
        long checksum = 0L;
        for(int i = 0; i < 16; i++) {
            for(int j = 0; j < 16; j++) {
                checksum += SimpleBiomeBasin.summateEffect(basins, 16 + i, 16 + j);
            }
        }
        return checksum;
    }

    private static long sumGrid(SimpleBiomeBasin[][] basins, int h, int w) {
        long checksum = 0L;
        for(int i = 0; i < w; i++) {
            for(int j = 0; j < h; j++) {
                checksum += SimpleBiomeBasin.summateEffect(basins, 16 + i, 16 + j);
            }
        }
        return checksum;
    }

    private static final class AllocationCounters {
        long intArrays;
        long basinArrays;

        int[] newIntArray(int size) {
            intArrays++;
            return new int[size];
        }

        SimpleBiomeBasin[][] newBasinGrid(int rows, int cols) {
            basinArrays++;
            return new SimpleBiomeBasin[rows][cols];
        }
    }

    private static final class SimpleBufferPool {
        private static final int INT_ARRAY_POOL_LIMIT = 4;
        private int[] chunkTiles;
        private int[] chunkGenTiles;
        private final Map<Integer, int[]> intArrays = new LinkedHashMap<Integer, int[]>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, int[]> eldest) {
                return size() > INT_ARRAY_POOL_LIMIT;
            }
        };
        private SimpleBiomeBasin[][] chunkBasins;
        private SimpleBiomeBasin[][] chunkGenBasins;
        private final Map<Long, SimpleBiomeBasin[][]> basinGrids = new HashMap<>();
        private final Map<Long, SimpleBiomeBasin[][]> basinGenGrids = new HashMap<>();

        int[] acquireChunkTiles(AllocationCounters counters) {
            if(chunkTiles == null) {
                chunkTiles = counters.newIntArray(9);
            }
            return chunkTiles;
        }

        int[] acquireChunkGenTiles(AllocationCounters counters) {
            if(chunkGenTiles == null) {
                chunkGenTiles = counters.newIntArray(9);
            }
            return chunkGenTiles;
        }

        int[] acquireIntArray(int size, AllocationCounters counters) {
            int[] array = intArrays.get(size);
            if(array == null) {
                array = counters.newIntArray(size);
                intArrays.put(size, array);
            }
            return array;
        }

        SimpleBiomeBasin[][] acquireChunkBasins(AllocationCounters counters) {
            if(chunkBasins == null) {
                chunkBasins = counters.newBasinGrid(3, 3);
            }
            return chunkBasins;
        }

        SimpleBiomeBasin[][] acquireChunkGenBasins(AllocationCounters counters) {
            if(chunkGenBasins == null) {
                chunkGenBasins = counters.newBasinGrid(3, 3);
            }
            return chunkGenBasins;
        }

        SimpleBiomeBasin[][] acquireBasinGrid(int rows, int cols, AllocationCounters counters) {
            long key = (((long)rows) << 32) | (cols & 0xffffffffL);
            SimpleBiomeBasin[][] grid = basinGrids.get(key);
            if(grid == null) {
                grid = counters.newBasinGrid(rows, cols);
                basinGrids.put(key, grid);
            }
            return grid;
        }

        SimpleBiomeBasin[][] acquireBasinGenGrid(int rows, int cols, AllocationCounters counters) {
            long key = (((long)rows) << 32) | (cols & 0xffffffffL);
            SimpleBiomeBasin[][] grid = basinGenGrids.get(key);
            if(grid == null) {
                grid = counters.newBasinGrid(rows, cols);
                basinGenGrids.put(key, grid);
            }
            return grid;
        }
    }

    private static final class SimpleBiomeBasin {
        private final int x;
        private final int z;
        private final int value;
        private final double strength;

        SimpleBiomeBasin(int x, int z, int value, double strength) {
            this.x = x;
            this.z = z;
            this.value = value;
            this.strength = strength;
        }

        double getWeaknessAt(int atx, int aty) {
            double xdisplace = (double)(x - atx);
            double ydisplace = (double)(z - aty);
            return (xdisplace * xdisplace) + (ydisplace * ydisplace);
        }

        static int summateEffect(SimpleBiomeBasin[][] basins, int x, int z) {
            double effect = 0.0;
            int indexx = 0;
            int indexy = 0;
            for(int i = 0; i < basins.length; i++) {
                for(int j = 0; j < basins[i].length; j++) {
                    SimpleBiomeBasin basin = basins[i][j];
                    double power = basin.strength / basin.getWeaknessAt(x, z);
                    if(effect < power) {
                        effect = power;
                        indexx = i;
                        indexy = j;
                    }
                }
            }
            return basins[indexx][indexy].value;
        }
    }
}
