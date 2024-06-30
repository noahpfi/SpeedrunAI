package com.swirb.pvpbots.basicallyGarbage.ai;

public class TanHActivation implements Activation {

    public double activate(double d) {
        return (Math.exp(d) - Math.exp(-d)) / (Math.exp(d) + Math.exp(-d));
    }
}
