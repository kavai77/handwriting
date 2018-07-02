package com.himadri.handwriting;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import static java.util.stream.IntStream.range;

@RestController
public class PredictionService {

    private static final String PNG_DATA_HEADER = "data:image/png;base64,";
    private final int IMG_DIM = 28;
    private static final int IMG_MARGIN = 4;


    private NeuralNetwork neuralNetwork;

    @Autowired
    public PredictionService(NeuralNetwork neuralNetwork) {
        this.neuralNetwork = neuralNetwork;
    }

    @PostMapping(value = "/prediction", consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Prediction prediction(@RequestBody String body) throws IOException {
        if (!StringUtils.startsWith(body, PNG_DATA_HEADER)) {
            throw new IllegalArgumentException(body);
        }
        double[] input = readPixels(body);
        final Prediction prediction = neuralNetwork.classify(input);
        return prediction;
    }

    @GetMapping(value = "/warmup")
    public void warmup() {
    }

    double[] readPixels(@RequestBody String body) throws IOException {
        final byte[] imageBytes = Base64.getDecoder().decode(StringUtils.removeStart(body, PNG_DATA_HEADER));
        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        Rectangle boundary = findImageBoundary(image);
        BufferedImage outImage = new BufferedImage(IMG_DIM, IMG_DIM, BufferedImage.TYPE_BYTE_GRAY);
        final Graphics2D g2 = outImage.createGraphics();
        double effectiveDim = IMG_DIM - 2 * IMG_MARGIN;
        double scale = Math.min(effectiveDim / boundary.getWidth(), effectiveDim / boundary.getHeight());
        g2.scale(scale, scale);
        g2.drawImage(image, -boundary.x + (int)Math.round((IMG_DIM / scale - boundary.width) / 2),
                -boundary.y + (int)Math.round((IMG_DIM / scale - boundary.height) / 2), null);
        g2.dispose();
        byte[] rawPixels = ((DataBufferByte)(outImage.getRaster().getDataBuffer())).getData();
        byte[] pixels = new byte[rawPixels.length];
        for (int i = 0; i < IMG_DIM; i++) {
            for (int j = 0; j < IMG_DIM; j++) {
                pixels[j * IMG_DIM + i] = rawPixels[i * IMG_DIM + j];
            }
        }
        double[] input = new double[pixels.length];
        range(0, pixels.length).forEach(i -> input[i] = pixels[i] == 0 ? 0 : 1);
        return input;
    }

    private Rectangle findImageBoundary(BufferedImage image) {
        OptionalInt minX = range(0, image.getHeight()).filter(
                x -> range(0, image.getWidth()).map(y -> image.getRGB(x, y)).anyMatch(p -> p != 0)
        ).findFirst();
        OptionalInt maxX = revRange(0, image.getHeight()).filter(
                x -> range(0, image.getWidth()).map(y -> image.getRGB(x, y)).anyMatch(p -> p != 0)
        ).findFirst();
        OptionalInt minY = range(0, image.getWidth()).filter(
                y -> range(0, image.getHeight()).map(x -> image.getRGB(x, y)).anyMatch(p -> p != 0)
        ).findFirst();
        OptionalInt maxY = revRange(0, image.getWidth()).filter(
                y -> range(0, image.getHeight()).map(x -> image.getRGB(x, y)).anyMatch(p -> p != 0)
        ).findFirst();
        if (Arrays.stream(new OptionalInt[]{minX, minY, maxX, maxY}).allMatch(OptionalInt::isPresent)) {
            return new Rectangle(minX.getAsInt(), minY.getAsInt(),
                    maxX.getAsInt() - minX.getAsInt() + 1,
                    maxY.getAsInt() - minY.getAsInt() + 1);
        } else {
            return new Rectangle(0, 0, image.getWidth(), image.getHeight());
        }
    }

    static IntStream revRange(int from, int to) {
        return range(from, to).map(i -> to - i + from - 1);
    }

}
