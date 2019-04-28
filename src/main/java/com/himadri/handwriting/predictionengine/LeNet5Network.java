package com.himadri.handwriting.predictionengine;

import com.himadri.handwriting.model.Pixels;
import com.himadri.handwriting.model.Prediction;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class LeNet5Network implements PredictionEngine {
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

    @Override
    public Prediction classify(Pixels pixels) {
        INDArray indArray = Nd4j.create(pixels.getRawPixels(), new int[]{1, 1, 28, 28});
        final INDArray output = model.output(indArray, Layer.TrainingMode.TEST).getRow(0);
        final int prediction = Nd4j.getBlasWrapper().iamax(output);
        double confidence = output.getDouble(prediction);
        return new Prediction("Convolutional Network", prediction, confidence, true);
    }
}
