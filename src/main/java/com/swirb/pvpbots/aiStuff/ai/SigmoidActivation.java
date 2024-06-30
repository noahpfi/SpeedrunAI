package com.swirb.pvpbots.basicallyGarbage.ai;

public class SigmoidActivation implements Activation {

    public double activate(double d) {
        return 1.0D / (1.0D + Math.exp(-d));
    }
}
