package jaredbgreat.climaticbiome.generation.map;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jaredbgreat.climaticbiome.generation.mapgenerator.BiomeBasin;

/**
 * Thread-local buffers used by {@link MapRegistry} to avoid repeated
 * allocations of small working arrays during biome lookups.
 */
final class BasinBufferPool {
    private static final int INT_ARRAY_POOL_LIMIT = 4;
    private static final ReferenceQueue<Thread> THREAD_REF_QUEUE = new ReferenceQueue<>();
    private static final Map<Reference<? extends Thread>, BasinBufferPool> ACTIVE_POOLS
            = new ConcurrentHashMap<>();
    private static final ThreadLocal<BasinBufferPool> LOCAL =
            ThreadLocal.withInitial(BasinBufferPool::new);

    static {
        Thread cleaner = new Thread(() -> {
            while(true) {
                try {
                    Reference<? extends Thread> ref = THREAD_REF_QUEUE.remove();
                    BasinBufferPool pool = ACTIVE_POOLS.remove(ref);
                    if(pool != null) {
                        pool.clear();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "BasinBufferPool-Reaper");
        cleaner.setDaemon(true);
        cleaner.start();
    }

    static BasinBufferPool local() {
        return LOCAL.get();
    }

    static int intArrayPoolLimit() {
        return INT_ARRAY_POOL_LIMIT;
    }

    static int clearLocal() {
        BasinBufferPool pool = LOCAL.get();
        int cachedEntries = pool.clearAndCount();
        pool.unregister();
        LOCAL.remove();
        return cachedEntries;
    }

    private int[] chunkTiles;
    private int[] chunkGenTiles;
    private final Map<Integer, int[]> intArrays = new LinkedHashMap<Integer, int[]>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, int[]> eldest) {
            return size() > INT_ARRAY_POOL_LIMIT;
        }
    };

    private BiomeBasin[][] chunkBasins;
    private BiomeBasin[][] chunkGenBasins;
    private final Map<Long, BiomeBasin[][]> basinGrids = new HashMap<>();
    private final Map<Long, BiomeBasin[][]> basinGenGrids = new HashMap<>();
    private final Reference<Thread> ownerThread;

    private BasinBufferPool() {
        ownerThread = new WeakReference<>(Thread.currentThread(), THREAD_REF_QUEUE);
        ACTIVE_POOLS.put(ownerThread, this);
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

    int getIntArrayPoolSize() {
        return intArrays.size();
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

    private void unregister() {
        ACTIVE_POOLS.remove(ownerThread);
        ownerThread.clear();
    }

    private void clear() {
        chunkTiles = null;
        chunkGenTiles = null;
        intArrays.clear();
        chunkBasins = null;
        chunkGenBasins = null;
        basinGrids.clear();
        basinGenGrids.clear();
    }

    private int clearAndCount() {
        int size = intArrays.size();
        clear();
        return size;
    }
}
