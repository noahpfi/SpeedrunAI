package com.swirb.pvpbots.basicallyGarbage.otherNN;

import com.swirb.pvpbots.basicallyGarbage.nn.Matrix;

public class Losses {

    public static double mse(Matrix actualY, Matrix predictedY) {
        return Matrix.mean(actualY.subtract(predictedY).power(2.0D));
    }

    public static double msePrime(Matrix actualY, Matrix predictedY) {
        return predictedY.subtract(actualY).divide(actualY.size()).multiply(2.0D);
    }
}
