package com.himadri.handwriting;

import com.himadri.handwriting.model.Pixels;
import com.himadri.handwriting.model.Prediction;
import com.himadri.handwriting.predictionengine.PredictionEngine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.IntStream.range;

@RestController
@RequestMapping("/service")
public class PredictionService {

    private static final String PNG_DATA_HEADER = "data:image/png;base64,";
    private final int IMG_DIM = 28;
    private static final int IMG_MARGIN = 4;

    private final List<PredictionEngine> predictionEngineList;

    @Autowired
    public PredictionService(List<PredictionEngine> predictionEngineList) {
        this.predictionEngineList = predictionEngineList;
    }

    @PostMapping(value = "/prediction", consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Prediction> prediction(HttpServletRequest httpServletRequest, @RequestBody String body) throws IOException {
        if (!StringUtils.startsWith(body, PNG_DATA_HEADER)) {
            throw new IllegalArgumentException(body);
        }
        Pixels input = readPixels(body);

        List<Prediction> predictions = predictionEngineList
            .stream()
            .map(a -> a.classify(input))
            .sorted(Comparator.comparing(Prediction::getMethod))
            .collect(Collectors.toList());

        //storageService.storePrediction(getClientIp(httpServletRequest), input, predictions);

        return predictions;
    }

    Pixels readPixels(@RequestBody String body) throws IOException {
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
        double[] rawInput = new double[pixels.length];
        range(0, pixels.length).forEach(i -> input[i] = pixels[i] == 0 ? 0 : 1);
        range(0, rawInput.length).forEach(i -> rawInput[i] = rawPixels[i] == 0 ? 0 : 1);
        return new Pixels(imageBytes, input, rawInput);
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

    private String getClientIp(HttpServletRequest request) {

        String remoteAddr = request.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isEmpty(remoteAddr)) {
            remoteAddr = request.getRemoteAddr();
        }

        return remoteAddr;
    }
}
