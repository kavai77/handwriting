package com.himadri.handwriting;

import java.util.Arrays;

public class Prediction {
    private final String method;
    private final int prediction;
    private final Double confidence;

    public Prediction(String method, int prediction, Double confidence) {
        this.method = method;
        this.prediction = prediction;
        this.confidence = confidence;
    }

    public String getMethod() {
        return method;
    }

    public int getPrediction() {
        return prediction;
    }

    public Double getConfidence() {
        return confidence;
    }
}
