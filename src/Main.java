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
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.net.URLDecoder;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {



        String[] images = getManga("https://manaa.space/post/uploader/V85093GxAgERKBLa").getContent();

        IO.pdf(IO.cropImages(IO.getImages(images)), "도쿄구울 리메이크 1화.pdf");
    }

    public static Manga getManga(String url) throws Exception {

        Document doc = getDocument(url);
        String html = doc.outerHtml();

        String p = regex("p: \"(.*?)\"", html);
        String k = regex("k: \"(.*?)\"", html);

        Gson gson = new Gson();
        return gson.fromJson(decode(k, p), Manga.class);
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
        AlgorithmParameterSpec parameterSpec = new IvParameterSpec(Hex.decodeHex(ivHex.toCharArray()));

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

        return new String(cipher.doFinal(Hex.decodeHex(cipherTextHex.toCharArray())));
    }

    public static Document getDocument(String url) {

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
}

class Manga {

    private String[] content;

    public String[] getContent() {
        return content;
    }
}