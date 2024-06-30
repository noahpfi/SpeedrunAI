package com.swirb.pvpbots.basicallyGarbage.ai;

public class TanHPrimeActivation implements Activation {

    public double activate(double d) {
        return 1.0D - Math.pow((Math.exp(d) - Math.exp(-d)) / (Math.exp(d) + Math.exp(-d)), 2);
    }
}
