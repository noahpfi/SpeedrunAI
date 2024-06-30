package com.swirb.pvpbots.basicallyGarbage.otherNN;

import com.swirb.pvpbots.basicallyGarbage.nn.Matrix;

public class FcLayer extends Layer {

    public Matrix weights;
    public Matrix bias;

    public FcLayer(int inputSize, int outputSize) {
        this.weights = Matrix.fillRandom(inputSize, outputSize, inputSize, outputSize);
        this.bias = Matrix.fillRandom(inputSize, outputSize, 1.0D, outputSize);
    }

    public Matrix forwardPropagation(Matrix input) {
        this.input = input;
        this.output = Matrix.multiply(this.input, this.weights);
        this.output.add(this.bias);
        return this.output;
    }

    public Matrix backwardPropagation(Matrix outputError, double learningRate) {
        Matrix inputError = Matrix.multiply(outputError, Matrix.transpose(this.weights));
        Matrix weightsError = Matrix.multiply(Matrix.transpose(this.input), outputError);
        this.weights = this.weights.subtract(weightsError.multiply(learningRate));
        this.bias = this.bias.subtract(outputError.multiply(learningRate));
        return inputError;
    }
}
