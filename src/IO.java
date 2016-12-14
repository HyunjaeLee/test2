import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IO {

    public static BufferedImage[] getImages(String[] urls) throws IOException {

        int length = urls.length;

        BufferedImage[] images = new BufferedImage[length];

        for(int i = 0; i < length; i++) {

            URLConnection connection = new URL(urls[i]).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");

            images[i] = ImageIO.read(connection.getInputStream());
        }

        return images;
    }

    public static BufferedImage[] cropImages(BufferedImage[] images) {

        List<BufferedImage> imageList = new ArrayList<BufferedImage>();

        for(BufferedImage image : images) {

            int height = image.getHeight();
            int width = image.getWidth();
            int halfWidth = width / 2;

            if(height < width) {

                for (int i = 0; i < 2; i++) {

                    BufferedImage smallImage = image.getSubimage(halfWidth * i, 0, halfWidth, height);
                    imageList.add(smallImage);
                }
            } else {
                imageList.add(image);
            }
        }

        return imageList.toArray(new BufferedImage[imageList.size()]);
    }

    public static void saveImages(BufferedImage[] images) throws IOException {

        String userHome = System.getProperty("user.home");

        for(int i = 0; i < images.length; i++) {
            ImageIO.write(images[i], "jpeg", new File(userHome + "/Downloads/" + i + ".jpeg"));
        }
    }

    public static void zip(BufferedImage[] images, String filename) throws IOException {

        String name = System.getProperty("user.home") + "/Downloads/" + filename;
        FileOutputStream fos = new FileOutputStream(name);
        ZipOutputStream zos = new ZipOutputStream(fos);

        for(int i = 0; i < images.length; i++) {

            ZipEntry entry = new ZipEntry(i + ".jpeg");
            zos.putNextEntry(entry);
            ImageIO.write(images[i], "jpeg", zos);
            zos.closeEntry();
        }

        zos.close();
    }

    public static void download(String url, String file) throws IOException {

        URLConnection connection = new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/5.0");
        ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
        FileOutputStream fos = new FileOutputStream(new File(file));
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }
}
