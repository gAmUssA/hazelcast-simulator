package com.hazelcast.simulator.tests.icache;

import com.hazelcast.config.CacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.simulator.test.TestContext;
import com.hazelcast.simulator.test.TestRunner;
import com.hazelcast.simulator.test.annotations.Performance;
import com.hazelcast.simulator.test.annotations.Run;
import com.hazelcast.simulator.test.annotations.Setup;
import com.hazelcast.simulator.test.annotations.Teardown;
import com.hazelcast.simulator.test.annotations.Warmup;
import com.hazelcast.simulator.utils.ThreadSpawner;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static com.hazelcast.simulator.tests.icache.helpers.CacheUtils.createCacheManager;

/**
 * A performance test for the cache. The key is integer and value is a integer
 */
public class PerformanceICacheTest {

    private static final ILogger LOGGER = Logger.getLogger(PerformanceICacheTest.class);

    // properties
    public String basename = PerformanceICacheTest.class.getSimpleName();
    public int threadCount = 10;
    public int keyCount = 1000000;
    public int logFrequency = 10000;
    public int performanceUpdateFrequency = 10000;
    public int writePercentage = 10;

    private Cache<Object, Object> cache;
    private final AtomicLong operations = new AtomicLong();
    private TestContext testContext;

    @Setup
    public void setup(TestContext testContext) throws Exception {
        if (writePercentage < 0) {
            throw new IllegalArgumentException("Write percentage can't be smaller than 0");
        }

        if (writePercentage > 100) {
            throw new IllegalArgumentException("Write percentage can't be larger than 100");
        }

        this.testContext = testContext;
        HazelcastInstance hazelcastInstance = testContext.getTargetInstance();

        CacheManager cacheManager = createCacheManager(hazelcastInstance);

        CacheConfig<Integer, Integer> config = new CacheConfig<Integer, Integer>();
        config.setName(basename);

        try {
            cacheManager.createCache(basename, config);
        } catch (CacheException hack) {
            // temp hack to deal with multiple nodes wanting to make the same cache
            LOGGER.severe(hack);
        }
        cache = cacheManager.getCache(basename);
    }

    @Teardown
    public void teardown() throws Exception {
        cache.close();
    }

    @Warmup(global = true)
    public void warmup() throws Exception {
        for (int k = 0; k < keyCount; k++) {
            cache.put(k, 0);

            if (k % 10000 == 0) {
                LOGGER.info("Warmup: " + k);
            }
        }
    }

    @Run
    public void run() {
        ThreadSpawner spawner = new ThreadSpawner(testContext.getTestId());
        for (int k = 0; k < threadCount; k++) {
            spawner.spawn(new Worker());
        }
        spawner.awaitCompletion();
    }

    @Performance
    public long getOperationCount() {
        return operations.get();
    }

    private class Worker implements Runnable {
        private final Random random = new Random();
        private final Map<Integer, Long> result = new HashMap<Integer, Long>();

        @Override
        public void run() {
            for (int k = 0; k < keyCount; k++) {
                result.put(k, 0L);
            }

            long iteration = 0;
            while (!testContext.isStopped()) {
                Integer key = random.nextInt(keyCount);
                if (shouldWrite(iteration)) {
                    cache.put(key, (int) iteration);
                } else {
                    cache.get(key);
                }

                iteration++;
                if (iteration % logFrequency == 0) {
                    LOGGER.info(Thread.currentThread().getName() + " At iteration: " + iteration);
                }
                if (iteration % performanceUpdateFrequency == 0) {
                    operations.addAndGet(performanceUpdateFrequency);
                }
            }
            operations.addAndGet(iteration % performanceUpdateFrequency);
        }

        private boolean shouldWrite(long iteration) {
            if (writePercentage == 0) {
                return false;
            } else if (writePercentage == 100) {
                return true;
            } else {
                return (iteration % 100) < writePercentage;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        PerformanceICacheTest test = new PerformanceICacheTest();
        new TestRunner<PerformanceICacheTest>(test).run();
    }
}
