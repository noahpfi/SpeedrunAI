package com.swirb.pvpbots.basicallyGarbage.otherNN;

import com.swirb.pvpbots.basicallyGarbage.ai.Activation;
import com.swirb.pvpbots.basicallyGarbage.nn.Matrix;

public class ActivationLayer extends Layer {

    public Activation activation;
    public Activation activationPrime;

    public ActivationLayer(Activation activation, Activation activationPrime) {
        this.activation = activation;
        this.activationPrime = activationPrime;
    }

    public Matrix forwardPropagation(Matrix input) {
        this.input = input;
        this.output = this.input.activate(this.activation);
        return this.output;
    }

    public Matrix backwardPropagation(Matrix outputError, double learningRate) {
        return this.input.activate(this.activationPrime).multiply(outputError);
    }
}
