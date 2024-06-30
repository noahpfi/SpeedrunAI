package com.swirb.pvpbots.basicallyGarbage.nn;

import java.util.List;

public class NeuralNetwork {

    Matrix weightsInputHidden;
    Matrix weightsHiddenOutput;
    Matrix biasHidden;
    Matrix biasOutput;
    double learningRate = 0.01;

    public NeuralNetwork(int inputs, int hidden, int outputs) {
        this.weightsInputHidden = new Matrix(hidden, inputs);
        this.weightsHiddenOutput = new Matrix(outputs, hidden);
        this.biasHidden = new Matrix(hidden,1);
        this.biasOutput = new Matrix(outputs,1);
    }

    public List<Double> predict(double[] x) {
        Matrix input = Matrix.fromArray(x);
        Matrix hidden = Matrix.multiply(this.weightsInputHidden, input);
        hidden.add(this.biasHidden);
        hidden.sigmoid();
        Matrix output = Matrix.multiply(this.weightsHiddenOutput,hidden);
        output.add(this.biasOutput);
        output.sigmoid();
        return output.toArray();
    }

    public void train(double [] x, double [] y) {
        Matrix input = Matrix.fromArray(x);
        Matrix hidden = Matrix.multiply(this.weightsInputHidden, input);
        hidden.add(this.biasHidden);
        hidden.sigmoid();
        Matrix output = Matrix.multiply(this.weightsHiddenOutput,hidden);
        output.add(this.biasOutput);
        output.sigmoid();

        Matrix yMatrix = Matrix.fromArray(y);

        Matrix error = Matrix.subtract(yMatrix, output);
        Matrix gradient = output.sigmoidPrime();
        gradient.multiply(error);
        gradient.multiply(this.learningRate);
        Matrix hiddenTransposed = Matrix.transpose(hidden);
        Matrix weightsHiddenOutputDelta =  Matrix.multiply(gradient, hiddenTransposed);
        this.weightsHiddenOutput.add(weightsHiddenOutputDelta);
        this.biasOutput.add(gradient);

        Matrix weightHiddenOutputTransposed = Matrix.transpose(this.weightsHiddenOutput);
        Matrix hiddenErrors = Matrix.multiply(weightHiddenOutputTransposed, error);
        Matrix hiddenGradient = hidden.sigmoidPrime();
        hiddenGradient.multiply(hiddenErrors);
        hiddenGradient.multiply(this.learningRate);
        Matrix inputsTransposed = Matrix.transpose(input);
        Matrix weightsInputHiddenDelta = Matrix.multiply(hiddenGradient, inputsTransposed);
        this.weightsInputHidden.add(weightsInputHiddenDelta);
        this.biasHidden.add(hiddenGradient);
    }

    public void fit(double[][] x, double[][] y, int epochs) {
        for (int i = 0; i < epochs; i++) {
            int sampleN = (int) (Math.random() * x.length);
            this.train(x[sampleN], y[sampleN]);
        }
    }
}
