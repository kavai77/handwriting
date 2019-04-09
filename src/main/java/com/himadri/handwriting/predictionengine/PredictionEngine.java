package com.himadri.handwriting.predictionengine;

import com.himadri.handwriting.model.Pixels;
import com.himadri.handwriting.model.Prediction;

public interface PredictionEngine {
    Prediction classify(Pixels pixels);
}
