import com.google.gson.Gson;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.codec.binary.Hex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        Document doc = getDocument("https://manaa.space/post/uploader/oQMZVleROdqYJ9L1");
        String html = doc.outerHtml();

        String p = regex("p: \"(.*?)\"", html);
        String k = regex("k: \"(.*?)\"", html);

        Gson gson = new Gson();
        Content content = gson.fromJson(decode(k, p), Content.class);

        for(String str : content.getContent()) {
            System.out.println(str);
        }
    }

    public static String decode(String k, String p) throws Exception {

        String secret = decode64(mnbe(URLDecoder.decode(k, "UTF-8")));
        String encryptionKeyHex = secret.substring(hexBits(128));

        String tokenString = decode64(p);

        int versionOffset = hexBits(8);
        int timeOffset = versionOffset + hexBits(64);
        int ivOffset = timeOffset + hexBits(128);
        int hmacOffset = tokenString.length() - hexBits(256);

        String ivHex = tokenString.substring(timeOffset, ivOffset);
        String cipherTextHex = tokenString.substring(ivOffset, hmacOffset);

        SecretKeySpec keySpec = new SecretKeySpec(Hex.decodeHex(encryptionKeyHex.toCharArray()), "AES");

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(Hex.decodeHex(ivHex.toCharArray())));

        return new String(c.doFinal(Hex.decodeHex(cipherTextHex.toCharArray())));
    }

    public static String mnbe(String str) {

        String edic = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&()*+,./:;<=>?@[]^_`{|}~\"";
        char[] ddic = new char[256];
        int dbp = 0, dnm = 0, dv = -1;
        StringBuilder dstr = new StringBuilder();

        int i;
        for(i = 0; i < 256; i++)
            ddic[i] = (char) -1;
        for(i = 0; i < 91; i++)
            ddic[edic.charAt(i)] = (char) i;

        for(int x = 0; x < str.length(); x++) {
            i = str.charAt(x);
            if (i > 255 || ddic[i] == -1)
                continue;
            if (dv == -1)
                dv = ddic[i];
            else {
                dv += ddic[i] * 91;
                dbp |= (dv << dnm);
                dnm += (dv & 8191) > 88 ? 13 : 14;
                do {
                    dstr.append((char) (dbp & 255));
                    dbp >>= 8;
                    dnm -= 8;
                } while (dnm > 7);
                    dv = -1;
            }
        }

        String retStr;
        if (dv != -1)
            dstr.append((char) ((dbp | dv << dnm) & 255));
        retStr = dstr.toString();

        return retStr;
    }

    // turn bits into number of chars in a hex string
    public static int hexBits(int bits) {
        return bits / 8 * 2;
    }

    // convert base64 string to hex string
    public static String decode64(String str) {
        byte[] bytes = Base64.getUrlDecoder().decode(str.replace("/=+$/", ""));
        return Hex.encodeHexString(bytes);
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

class Content {

    private String[] content;

    public String[] getContent() {
        return content;
    }
}