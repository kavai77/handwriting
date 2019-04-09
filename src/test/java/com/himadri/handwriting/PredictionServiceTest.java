package com.himadri.handwriting;

import com.google.common.collect.ImmutableList;
import com.himadri.handwriting.model.Pixels;
import com.himadri.handwriting.predictionengine.LeNet5Network;
import com.himadri.handwriting.predictionengine.NeuralNetwork;
import org.junit.Ignore;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Ignore
public class PredictionServiceTest {
    private NeuralNetwork neuralNetwork = new NeuralNetwork();
    private LeNet5Network leNet5Network = new LeNet5Network();
    private PredictionService predictionService = new PredictionService(ImmutableList.of(leNet5Network, neuralNetwork));

    @Test
    public void displayTrainingSet() throws Exception {

        final double[][] doubles = neuralNetwork.readDoubleMatrix(getClass().getResourceAsStream("/testData.txt"));
        for (int i = 0; i < doubles.length; i++) {
            for (int j = 0; j < doubles[i].length; j++) {
                doubles[i][j] = doubles[i][j] > 0.5 ? 1 : 0;
            }
        }
        writeAllPixelsTwoImage(doubles, "training-data/all.png");
        writePixelsToImage(doubles[3000], "training-data/test.png");
    }

    @Test
    public void displayMnistTrainingSet() throws Exception {

        final double[][] doubles = neuralNetwork.readDoubleMatrix(getClass().getResourceAsStream("/mnist-train-data.txt"));
        for (int i = 0; i < doubles.length; i++) {
            for (int j = 0; j < doubles[i].length; j++) {
                doubles[i][j] = doubles[i][j] > 0.5 ? 1 : 0;
            }
        }
        writeAllPixelsTwoImage(doubles, "mnist-all.png");
    }

    @Test
    public void displayRealValue() throws Exception {
        final String body = "data:image/png;base64,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";
        final double[] doubles = predictionService.readPixels(body).getTransformedPixels();
        System.out.println(neuralNetwork.classify(new Pixels(doubles, null)));
        writePixelsToImage(doubles, "real.png");
    }

    private void writePixelsToImage(double[] pixels, String filename) throws IOException {
        int imgDim = (int) Math.sqrt(pixels.length);
        double[] pixelLine = new double[imgDim];
        for (int i = 0; i < imgDim; i++) {
            System.arraycopy(pixels, i * imgDim, pixelLine, 0, imgDim);
            System.out.println(Arrays.toString(pixelLine));
        }
        BufferedImage outImage = new BufferedImage(imgDim, imgDim, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < imgDim; i++) {
            for (int j = 0; j < imgDim; j++) {
                final float pixel = (float)(pixels[i * imgDim + j]);
                final int dataElement = outImage.getColorModel().getDataElement(new float[]{pixel, pixel, pixel}, 0);
                outImage.setRGB(i, j, dataElement);
            }
        }
        ImageIO.write(outImage, "png", new File(filename));
    }

    private void writeAllPixelsTwoImage(double[][] pixels, String filename) throws IOException {
        int imgDim = (int) Math.sqrt(pixels[0].length);
        int width = (int)Math.ceil(Math.sqrt(pixels.length));
        BufferedImage outImage = new BufferedImage(imgDim * width, imgDim * width, BufferedImage.TYPE_INT_RGB);
        outer:
        for (int row = 0; true; row++) {
            for (int col = 0; col < width; col++) {
                int index = row * width + col;
                if (index == pixels.length) break outer;
                for (int i = 0; i < imgDim; i++) {
                    for (int j = 0; j < imgDim; j++) {
                        final float pixel = (float) (pixels[index][i * imgDim + j]);
                        final int dataElement = outImage.getColorModel().getDataElement(new float[]{pixel, pixel, pixel}, 0);
                        outImage.setRGB(row * imgDim + i, col * imgDim + j, dataElement);
                    }
                }
            }
        }
        ImageIO.write(outImage, "png", new File(filename));
    }
}