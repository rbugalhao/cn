package utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Utils {

    public static byte[] imageToBytes(String imagePath) {
        try {
            // Read the image from the file path
            BufferedImage image = ImageIO.read(new File(imagePath));

            // Convert the BufferedImage to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String type = imagePath.substring(imagePath.lastIndexOf(".") + 1);
            ImageIO.write(image, type, baos); // You can specify other formats like "png", "bmp" etc.
            baos.flush();
            byte[] imageInBytes = baos.toByteArray();
            baos.close();

            return imageInBytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
