package com.himadri.handwriting.predictionengine;

import com.google.common.annotations.VisibleForTesting;
import com.himadri.handwriting.model.Pixels;
import com.himadri.handwriting.model.Prediction;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class NeuralNetwork implements PredictionEngine {
    private RealMatrix theta1 = MatrixUtils.createRealMatrix(readDoubleMatrix(NeuralNetwork.class.getResourceAsStream("/theta1_mnist_lambda1_iteration50.txt")));
    private RealMatrix theta2 = MatrixUtils.createRealMatrix(readDoubleMatrix(NeuralNetwork.class.getResourceAsStream("/theta2_mnist_lambda1_iteration50.txt")));

    public Prediction classify(Pixels pixels) {
        RealMatrix inputMatrix = MatrixUtils.createColumnRealMatrix(prefixWithOne(pixels.getTransformedPixels()));
        RealMatrix h2 = theta1.multiply(inputMatrix);
        h2.walkInRowOrder(new SigmoidVisitor());
        RealMatrix h2Prefixed = MatrixUtils.createColumnRealMatrix(prefixWithOne(h2.getColumn(0)));
        RealMatrix h3 = theta2.multiply(h2Prefixed);
        h3.walkInRowOrder(new SigmoidVisitor());
        final double[] weights = h3.getColumn(0);
        int maxIndex = 0;
        double max = weights[0];
        for (int i = 1; i < weights.length; i++) {
            if (weights[i] > max) {
                max = weights[i];
                maxIndex = i;
            }
        }
        return new Prediction("Fully connected network", maxIndex, max);
    }

    private double[] prefixWithOne(double[] input) {
        double[] prefixInput = new double[input.length + 1];
        prefixInput[0] = 1;
        System.arraycopy(input, 0, prefixInput, 1, input.length);
        return prefixInput;
    }

    @VisibleForTesting
    public double[][] readDoubleMatrix(InputStream is) {
        List<double[]> doublesList = new ArrayList<>();
        try (BufferedReader br  = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                final String[] split = StringUtils.split(StringUtils.normalizeSpace(line), ' ');
                final double[] doubles = Arrays.stream(split).mapToDouble(Double::parseDouble).toArray();
                doublesList.add(doubles);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return doublesList.toArray(new double[][]{});
    }

    private static class SigmoidVisitor extends DefaultRealMatrixChangingVisitor {
        @Override
        public double visit(int row, int column, double value) {
            return 1.0 / (1.0 + Math.exp(-value));
        }
    }


}
