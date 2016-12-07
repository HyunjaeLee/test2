import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        //downloadImage("https://media.manaa.space/comics/1320/39845/dZlWx4j16kg2a3BO.jpeg", Integer.toString(1));

        String url = "https://manaa.space/post/uploader/V85093GxAgERKBLa";
        for(String img : getImages(url)) {
            System.out.println(img);
        }

    }

    private static Document getDocument(String url) {

        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

    public static void getAll(String url) {

        Document doc = getDocument(url);
        doc.select("#shelf a[href]")
                .forEach(element -> System.out.println(element.text() + " " + element.attr("abs:href")));
    }

    public static void getList(String url) {

        Document doc = getDocument(url);
        doc.select(".item").forEach(System.out::println);
    }

    public static String[] getImages(String url) throws Exception {

        Document doc = getDocument(url);
        String html = doc.outerHtml();

        String p = regex("p: \"(.*?)\"", html);
        String k = regex("k: \"(.*?)\"", html);

        return decrypt(k, p);
    }

    public static String regex(String regex, String input) {

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        String result = "";
        if(matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    public static String[] decrypt(String k, String encrypted) throws Exception {

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval(new FileReader("script.js"));

        Invocable invocable = (Invocable) engine;

        ScriptObjectMirror result = (ScriptObjectMirror) invocable.invokeFunction("decrypt", k, encrypted);
        return result.to(String[].class);
    }

    public static void downloadImage(String url, String filename) throws IOException {

        URLConnection connection = new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedImage image = ImageIO.read(connection.getInputStream());

        int height = image.getHeight();
        int width = image.getWidth();

        logger.debug("height: {}, width: {}", height, width);

        String userHome = System.getProperty("user.home");

        if(height < width) {

            int halfWidth = width / 2;

            for(int i = 0; i < 2; i++) {
                BufferedImage smallImage = image.getSubimage(halfWidth * i, 0, halfWidth, height);
                ImageIO.write(smallImage, "jpeg", new File(userHome + "/Downloads/" + filename + "-" + (i + 1) + ".jpeg"));
            }

        } else {
            ImageIO.write(image, "jpeg", new File(userHome + "/Downloads/" + filename + ".jpeg"));
        }
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