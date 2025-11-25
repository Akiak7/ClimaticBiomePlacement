package jaredbgreat.climaticbiome.generation.map;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Stress test to ensure the buffer pool limits its cached int[] entries and
 * cleans up thread-local state when the owning thread exits.
 */
public final class BasinBufferPoolStressTest {
    private static final int REQUESTS = 256;
    private static final int SIZE_VARIETY = 48;

    public static void main(String[] args) throws InterruptedException {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicInteger clearedSize = new AtomicInteger(-1);

        Thread worker = new Thread(() -> {
            BasinBufferPool pool = BasinBufferPool.local();
            try {
                for(int i = 0; i < REQUESTS; i++) {
                    int requested = ((i % SIZE_VARIETY) + 1) * ((i / SIZE_VARIETY) + 1);
                    pool.acquireIntArray(requested);
                    int poolSize = pool.getIntArrayPoolSize();
                    if(poolSize > BasinBufferPool.intArrayPoolLimit()) {
                        throw new AssertionError("Pool size exceeded limit: " + poolSize);
                    }
                }
            } catch(Throwable t) {
                failure.set(t);
            } finally {
                clearedSize.set(BasinBufferPool.clearLocal());
            }
        }, "BasinBufferPoolStress");

        worker.start();
        worker.join();

        if(failure.get() != null) {
            throw new AssertionError("Stress test failed", failure.get());
        }
        if(clearedSize.get() < 0) {
            throw new AssertionError("Pool cleanup did not run; size not recorded.");
        }
        if(clearedSize.get() > BasinBufferPool.intArrayPoolLimit()) {
            throw new AssertionError(
                    "Pool exceeded limit at cleanup; remaining entries: " + clearedSize.get());
        }
        System.out.println("Stress test completed with bounded pool size.");
    }

    private BasinBufferPoolStressTest() {
    }
}
