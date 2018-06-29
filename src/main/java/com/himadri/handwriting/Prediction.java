package com.himadri.handwriting;

import java.util.Arrays;

public class Prediction {
    private final int prediction;
    private final double[] weights;

    public Prediction(int prediction, double[] weights) {
        this.prediction = prediction;
        this.weights = weights;
    }

    public int getPrediction() {
        return prediction;
    }

    public double[] getWeights() {
        return weights;
    }

    @Override
    public String toString() {
        return String.format("%d %s", prediction, Arrays.toString(weights));
    }
}
