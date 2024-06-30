package com.swirb.pvpbots.basicallyGarbage.nn;

import com.swirb.pvpbots.basicallyGarbage.ai.Activation;
import com.swirb.pvpbots.basicallyGarbage.ai.Rng;

import java.util.ArrayList;
import java.util.List;

public class Matrix {

    public double[][] data;
    public int rows;
    public int columns;

    public Matrix(int rows, int columns) {
        this.data = new double[rows][columns];
        this.rows = rows;
        this.columns = columns;
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < columns; j++) {
                this.data[i][j] = Math.random() * 2 - 1;
            }
        }
    }

    public void add(double value) {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                this.data[i][j] += value;
            }
        }
    }

    public void add(Matrix matrix) {
        if(this.columns != matrix.columns || this.rows != matrix.rows) {
            System.out.println("Shape Mismatch");
            return;
        }
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                this.data[i][j] += matrix.data[i][j];
            }
        }
    }

    public Matrix subtract(Matrix matrix) {
        Matrix temp = new Matrix(this.rows, this.columns);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                temp.data[i][j] = temp.data[i][j] - matrix.data[i][j];
            }
        }
        return temp;
    }

    public static Matrix subtract(Matrix a, Matrix b) {
        Matrix temp = new Matrix(a.rows, a.columns);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.columns; j++) {
                temp.data[i][j] = a.data[i][j] - b.data[i][j];
            }
        }
        return temp;
    }

    public static Matrix transpose(Matrix matrix) {
        Matrix temp = new Matrix(matrix.columns, matrix.rows);
        for (int i = 0; i < matrix.rows; i++) {
            for (int j = 0; j < matrix.columns; j++) {
                temp.data[j][i] = matrix.data[i][j];
            }
        }
        return temp;
    }

    public static Matrix multiply(Matrix a, Matrix b) {
        Matrix temp = new Matrix(a.rows, b.columns);
        for (int i = 0; i < temp.rows; i++) {
            for (int j = 0; j < temp.columns; j++) {
                double sum = 0.0D;
                for (int k = 0; k < a.columns; k++) {
                    sum += a.data[i][k] * b.data[k][j];
                }
                temp.data[i][j] = sum;
            }
        }
        return temp;
    }

    public void multiplyReplace(Matrix matrix) {
        for (int i = 0; i < matrix.rows; i++) {
            for (int j = 0; j < matrix.columns; j++) {
                this.data[i][j] *= matrix.data[i][j];
            }
        }
    }

    public static double mean(Matrix matrix) {
        double sum = 0.0D;
        for (int i = 0; i < matrix.rows; i++) {
            for (int j = 0; j < matrix.columns; j++) {
                sum += matrix.data[i][j];
            }
        }
        return sum / (matrix.rows * matrix.columns);
    }

    public Matrix power(Matrix matrix) {
        Matrix temp = new Matrix(this.rows, this.columns);
        for (int i = 0; i < temp.rows; i++) {
            for (int j = 0; j < temp.columns; j++) {
                temp.data[i][j] = Math.pow(this.data[i][j], matrix.data[i][j]);
            }
        }
        return temp;
    }

    public Matrix power(double value) {
        Matrix temp = new Matrix(this.rows, this.columns);
        for (int i = 0; i < temp.rows; i++) {
            for (int j = 0; j < temp.columns; j++) {
                temp.data[i][j] = Math.pow(this.data[i][j], value);
            }
        }
        return temp;
    }


    public Matrix multiply(Matrix matrix) {
        Matrix temp = new Matrix(this.rows, matrix.columns);
        for (int i = 0; i < temp.rows; i++) {
            for (int j = 0; j < temp.columns; j++) {
                double sum = 0.0D;
                for (int k = 0; k < this.columns; k++) {
                    sum += this.data[i][k] * matrix.data[k][j];
                }
                temp.data[i][j] = sum;
            }
        }
        return temp;
    }

    public Matrix divide(Matrix matrix) {
        Matrix temp = new Matrix(this.rows, matrix.columns);
        for (int i = 0; i < temp.rows; i++) {
            for (int j = 0; j < temp.columns; j++) {
                double sum = 0.0D;
                for (int k = 0; k < this.columns; k++) {
                    sum += this.data[i][k] / matrix.data[k][j];
                }
                temp.data[i][j] = sum;
            }
        }
        return temp;
    }

    public Matrix multiply(double value) {
        Matrix temp = new Matrix(this.rows, this.columns);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                temp.data[i][j] *= value;
            }
        }
        return temp;
    }

    public Matrix divide(double value) {
        Matrix temp = new Matrix(this.rows, this.columns);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                temp.data[i][j] /= value;
            }
        }
        return temp;
    }

    public void sigmoid() {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                this.data[i][j] = 1 / (1 + Math.exp(-this.data[i][j]));
            }
        }
    }

    public Matrix sigmoidPrime() {
        Matrix temp = new Matrix(this.rows, this.columns);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                temp.data[i][j] = this.data[i][j] * (1 - this.data[i][j]);
            }
        }
        return temp;
    }

    public Matrix activate(Activation activation) {
        Matrix temp = new Matrix(this.rows, this.columns);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                temp.data[i][j] = activation.activate(this.data[i][j]);
            }
        }
        return temp;
    }

    public static Matrix fillRandom(int inputSize, int outputSize, double r0, double r1) {
        Matrix temp = new Matrix(inputSize, outputSize);
        for (int i = 0; i < temp.rows; i++) {
            for (int j = 0; j < temp.columns; j++) {
                temp.data[i][j] = Rng.random(r0, r1) - 0.5D;
            }
        }
        return temp;
    }

    public static Matrix fromArray(double[] array) {
        Matrix temp = new Matrix(array.length,1);
        for (int i = 0; i < array.length; i++) {
            temp.data[i][0] = array[i];
        }
        return temp;
    }

    public List<Double> toArray() {
        List<Double> temp= new ArrayList<>();
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                temp.add(this.data[i][j]);
            }
        }
        return temp;
    }

    public int size() {
        return this.rows * this.columns;
    }
}
