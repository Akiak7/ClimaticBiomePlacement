package jaredbgreat.climaticbiome.generation.cache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Exercises Cache.cleanup() to ensure all remaining entries survive both
 * resize and non-resize cleanup paths.
 */
public class CacheCleanupTest {
    public static void main(String[] args) {
        validateCleanupAfterShrink();
        validateCleanupWithoutResize();

        System.out.println("Cache cleanup tests passed.");
    }

    private static void validateCleanupAfterShrink() {
        Cache<TestCachable> cache = new Cache<>(16);
        List<TestCachable> items = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            TestCachable item = new TestCachable(i, i);
            items.add(item);
            cache.add(item);
        }

        int expectedTableSize = 18; // 36 entries shrunk by half, bounded by minSize.
        List<TestCachable> survivors = new ArrayList<>();
        for (TestCachable item : items) {
            int bucket = positiveMod(item.getCoords().hashCode(), expectedTableSize);
            if ((bucket > 1) && (survivors.size() < 2)) {
                survivors.add(item);
            } else {
                item.markOld();
            }
        }
        assert survivors.size() == 2 : "Failed to identify two survivor entries for the test.";

        cache.cleanup();

        assert getInternalTableLength(cache) == expectedTableSize :
                "Cache cleanup did not shrink the table as expected.";
        for (TestCachable survivor : survivors) {
            TestCachable cached = cache.get(survivor.getCoords());
            assert cached == survivor : "Survivor missing from cache after cleanup: " + survivor.getCoords();
        }
    }

    private static void validateCleanupWithoutResize() {
        int initialSize = 8;
        Cache<TestCachable> cache = new Cache<>(initialSize);

        List<TestCachable> collidingItems = findCollidingItems(initialSize, 3);
        for (TestCachable item : collidingItems) {
            cache.add(item);
        }

        collidingItems.get(1).markOld();
        cache.cleanup();

        assert getInternalTableLength(cache) == initialSize :
                "Cache cleanup unexpectedly resized when only deleting stale entries.";
        TestCachable early = cache.get(collidingItems.get(0).getCoords());
        TestCachable late = cache.get(collidingItems.get(2).getCoords());
        assert early == collidingItems.get(0) : "First colliding entry missing after cleanup.";
        assert late == collidingItems.get(2) : "Tail colliding entry missing after cleanup rebucketing.";
    }

    private static int positiveMod(int value, int mod) {
        return (value & 0x7fffffff) % mod;
    }

    private static List<TestCachable> findCollidingItems(int tableSize, int needed) {
        List<TestCachable> colliders = new ArrayList<>();
        Integer targetBucket = null;
        for (int i = 0; colliders.size() < needed; i++) {
            TestCachable candidate = new TestCachable(i, 0);
            int bucket = positiveMod(candidate.getCoords().hashCode(), tableSize);
            if ((targetBucket == null) || (bucket == targetBucket)) {
                targetBucket = bucket;
                colliders.add(candidate);
            }
        }
        return colliders;
    }

    private static int getInternalTableLength(Cache<?> cache) {
        try {
            Field dataField = Cache.class.getDeclaredField("data");
            dataField.setAccessible(true);
            Object[] table = (Object[]) dataField.get(cache);
            return table.length;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Unable to inspect cache backing array", e);
        }
    }

    private static class TestCachable implements ICachable {
        private final Coords coords;
        private boolean old;

        private TestCachable(int x, int z) {
            coords = new Coords(x, z);
            old = false;
        }

        @Override
        public void use() {
            // For the purposes of this test the use flag is ignored.
        }

        @Override
        public boolean isOldData() {
            return old;
        }

        public void markOld() {
            old = true;
        }

        @Override
        public Coords getCoords() {
            return coords;
        }
    }
}
