package com.himadri.handwriting;

import com.google.common.collect.ImmutableList;
import com.himadri.handwriting.predictionengine.LeNet5Network;
import com.himadri.handwriting.predictionengine.NeuralNetwork;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Disabled
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