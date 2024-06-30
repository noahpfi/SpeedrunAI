package com.swirb.pvpbots.basicallyGarbage.ai;

public class RELUActivation implements Activation {

    public double activate(double d) {
        return Math.max(0, d);
    }
}
