package com.himadri.handwriting;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.stream.IntStream;

@Ignore
public class MnistConverter {
    @Test
    public void convertLabelFile() throws Exception {
        try (DataInputStream is = new DataInputStream(new FileInputStream("training-data/train-labels.idx1-ubyte"));
             PrintWriter pw = new PrintWriter(new File("training-data/mnist-train-label.txt"))) {
            int magicNumber = is.readInt();
            int nbOfImages = is.readInt();
            for (int i = 0; i < nbOfImages; i++) {
                pw.println(is.readByte());
            }
        }
    }

    @Test
    public void convertImagesFile() throws Exception {
        try (DataInputStream is = new DataInputStream(new FileInputStream("training-data/train-images.idx3-ubyte"));
             PrintWriter pw = new PrintWriter(new File("training-data/mnist-train-data.txt"))) {
            int magicNumber = is.readInt();
            int nbOfImages = is.readInt();
            int imageHeight = is.readInt();
            int imageWidth = is.readInt();
            byte[] imageBuf = new byte[imageHeight * imageWidth];
            byte[] imageTransBuf = new byte[imageHeight * imageWidth];
            for (int i = 0; i < nbOfImages; i++) {
                IOUtils.readFully(is, imageBuf);
                for (int x = 0; x < imageHeight; x++) {
                    for (int y = 0; y < imageWidth; y++) {
                        imageTransBuf[x * imageWidth + y] = imageBuf[y * imageHeight + x];
                    }
                }
                IntStream.range(0, imageHeight * imageWidth)
                        .map(a -> imageTransBuf[a])
                        .map(a -> a & 0xFF)
                        .mapToDouble(a -> a / 255.0)
                        .forEach(a -> {pw.print(a);pw.print(' ');});
                pw.println();
            }
        }
    }
}
