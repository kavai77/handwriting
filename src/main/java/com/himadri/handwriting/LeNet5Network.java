package com.himadri.handwriting;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.BaseNDArray;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;

@Component
public class LeNet5Network {
    private MultiLayerNetwork model;

    @PostConstruct
    public void init() {
        try {
            String simpleMlp = new ClassPathResource("/LeNet5.h5").getFile().getPath();
            model = KerasModelImport.importKerasSequentialModelAndWeights(simpleMlp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Prediction classify(double[] input) {
        INDArray indArray = Nd4j.create(input, new int[]{1, 1, 28, 28});
        int[] predict = model.predict(indArray);
        return new Prediction("Convolutional Network", predict[0], null);
    }
}
