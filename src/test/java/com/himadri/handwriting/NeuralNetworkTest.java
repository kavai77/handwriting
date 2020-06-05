package com.himadri.handwriting;

import com.himadri.handwriting.model.Pixels;
import com.himadri.handwriting.model.Prediction;
import com.himadri.handwriting.predictionengine.NeuralNetwork;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

@Disabled
public class NeuralNetworkTest {
    private NeuralNetwork sut = new NeuralNetwork();

    @Test
    public void classify() throws Exception {
        final double[][] testData = sut.readDoubleMatrix(NeuralNetworkTest.class.getResourceAsStream("/testData.txt"));
        final double[][] expectedValues = sut.readDoubleMatrix(NeuralNetworkTest.class.getResourceAsStream("/expectedValues.txt"));
        for (int i = 0; i < testData.length; i++) {
            final Prediction prediction = sut.classify(new Pixels(null, testData[i], null));
            assertEquals((int) expectedValues[i][0], prediction.getPrediction());
        }
    }
}