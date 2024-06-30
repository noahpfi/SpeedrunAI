package com.swirb.pvpbots.basicallyGarbage.otherNN;

import com.swirb.pvpbots.basicallyGarbage.nn.Matrix;

public class Layer {

    public Matrix input;
    public Matrix output;

    public Layer() {
        this.input = null;
        this.output = null;
    }

    public Matrix forwardPropagation(Matrix input) {
        return null;
    }

    public Matrix backwardPropagation(Matrix outputError, double learningRate) {
        return null;
    }
}
