package com.thetinkeringtypist.powerset;

import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    // A placeholder is used for the value because the key is what matters, not the value
    private static final Object VALUE_PLACEHOLDER = new Object();

    private static long startTime = 0L;
    private static long stopTime = 0L;


    public static void main(String[] args) {
        // Size of the set to enumerate power sets for
        final int n = 5;

        // JVM warmup
        System.out.println();
        System.out.printf("Warming up JVM...%n%n");
        System.out.printf("Calculating the the power set of size %d%n", n);
        for (int i = 0; i < 10000; i++) {
            Object dummy = new Object();
            String s = dummy.toString();
        }

        final long freeBefore = Runtime.getRuntime().freeMemory() / 1024 / 1024;    // MiB

        // n = toIndex, which is exclusive
        BitSet base = new BitSet(100);
        base.set(0, n, true);

        startTime = System.nanoTime();

        // ConcurrentHashMap can store more than Integer.MAX_VALUE number of elements.
        // Calling mappingCount() will return the accurate number of key-value mappings
        // if there are no concurrent writes occurring at the time of the call. Calling size()
        // does not guarantee accurate results if there are more than Integer.MAX_VALUE mappings
        // in the map.
//        ConcurrentHashMap<BitSet, Object> sets = powerset(base);
        ConcurrentHashMap<Long, Object> sets = powerset(n);

        stopTime = System.nanoTime();

        final long freeAfter = Runtime.getRuntime().freeMemory() / 1024 / 1024;    // MiB
        System.gc(); // Manual GC invocation. Attempt to get approximate memory measurements.

        System.out.printf("Compute time:              %s", getTimeAsString(startTime, stopTime));
        System.out.println();
        System.out.printf("Calculated cardinality:    %.0f%n", Math.pow(2, n));
        System.out.printf("Enumerated cardinality:    %d%n", sets.mappingCount());
        System.out.println();
        System.out.printf("[BEFORE] Free Heap Space:  %s MiB%n", freeBefore);
        System.out.printf("[AFTER ] Free Heap Space:  %s MiB%n", freeAfter);
    }


    /**
     * Recursive powerset calculation.
     *
     * @see <a href="https://stackoverflow.com/a/1670871">This StackOverflow answer</a>
     * @param set the base bitset.
     * @return the powerset of the given bitset
     */
    private static ConcurrentHashMap<BitSet, Object> powerset(BitSet set) {
        if (Thread.currentThread().isInterrupted()){
            System.exit(-1);
        }

        ConcurrentHashMap<BitSet, Object> sets = new ConcurrentHashMap<>();
        if (set.isEmpty()) {
            sets.put(new BitSet(0), VALUE_PLACEHOLDER);
            return sets;
        }

        int head = set.nextSetBit(0);
        BitSet rest = set.get(0, set.size());
        rest.clear(head);

        for (BitSet s : powerset(rest).keySet()) {
            BitSet newSet = s.get(0, s.size());
            newSet.set(head);

            sets.put(newSet, VALUE_PLACEHOLDER);
            sets.put(s, VALUE_PLACEHOLDER);
        }

        return sets;
    }

    public static ConcurrentHashMap<Long, Object> powerset(final int numElements) {
        ConcurrentHashMap<Long, Object> sets = new ConcurrentHashMap<>();

        final long limit = (long) Math.pow(2, numElements);
        for (long i = 0; i < limit; i++) {
            sets.put(i, VALUE_PLACEHOLDER);
        }

        return sets;
    }


    private static String getTimeAsString(final long startTimeNano, final long stopTimeNano) {
        long timeInSeconds = (stopTimeNano - startTimeNano) / (1000 * 1000 * 1000);

        long sec  = timeInSeconds % 60;
        long min  = (timeInSeconds / 60) % 60;
        long hour = (timeInSeconds / (60 * 60)) % 24;
        long day  = (timeInSeconds / (24 * 60 * 60)) % 24;
        long week = ((timeInSeconds / (24 * 60 * 60)) % 24) % 7;

        return String.format("%dw %dd %dh %dm %ds", week, day, hour, min, sec);
    }
}