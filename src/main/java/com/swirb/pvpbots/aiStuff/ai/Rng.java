package com.swirb.pvpbots.basicallyGarbage.ai;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Rng {

    private static final Random random = new Random();

    public static Random random() {
        return random;
    }

    public static boolean pass(double chance) {
        return random.nextDouble() <= chance;
    }

    public static double randomValue() {
        return random(-1, 1);
    }

    public static double random(double r0, double r1) {
        return r0 + (r1 - r0) * random.nextDouble();
    }

    public static <T> T random(Collection<T> ts) {
        if (ts.size() == 0) {
            throw new UnsupportedOperationException("collection is empty");
        }
        return ts.stream().findAny().get();
    }

    public static <T> T random(T[] ts) {
        if (ts.length == 0) {
            throw new UnsupportedOperationException("[AI] -> rng: Array is empty");
        }
        return ts[random.nextInt(ts.length)];
    }

    public static <T> T random(List<T> ts) {
        if (ts.size() == 0) {
            throw new UnsupportedOperationException("[AI] -> rng: List is empty");
        }
        return ts.get(random.nextInt(ts.size()));
    }

    public static <T> T random(Set<T> ts) {
        if (ts.size() == 0) {
            throw new UnsupportedOperationException("[AI] -> rng: Set is empty");
        }
        return ts.stream().findAny().get();
    }
}
