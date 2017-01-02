import com.google.gson.*;
import element.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManaaSpace {

    private static String regex(String regex, String input) {

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        String result = "";
        if(matcher.find()) {
            result = matcher.group(1);
        }
        return result;
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

    public static MainElements getAll() {

        Document doc = getDocument("https://manaa.space/comics/?mode=1&status=2&order=3");
        MainElements mainElements = new MainElements();
        doc.select("#shelf a[href]").forEach(element -> {

            String name = element.text();
            String url = element.attr("abs:href");
            MainElement mainElement = new MainElement();
            mainElement.setUrl(url);
            mainElement.setName(name);
            mainElements.add(mainElement);
        });

        return mainElements;
    }

    public static ListElements getList(String url) {

        Document doc = getDocument(url);

        String json = doc.select("script[type=\"application/ld+json\"]").first().data();

        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(json);
        JsonObject object = jsonElement.getAsJsonObject();
        String thumbnail_main = object.get("thumbnailUrl").getAsString();
        String name = object.get("name").getAsString();
        JsonObject authorObject = object.getAsJsonObject("author");
        String author = authorObject.get("name").getAsString();

        ListElements listElements = new ListElements();
        listElements.setName(name);
        listElements.setAuthor(author);
        listElements.setThumbnail(thumbnail_main);

        doc.select(".item").forEach(item -> {

            String href = item.select("a[href]").first().attr("abs:href");
            String issueNumber = item.select(".classification").first().text();
            String date = item.select(".date").first().text();
            String thumbnail = item.select("img[data-src]").first().attr("data-src");

            ListElement listElement = new ListElement();
            listElement.setThumbnail(thumbnail);
            listElement.setDate(date);
            listElement.setIssueNumber(issueNumber);
            listElement.setUrl(href);

            listElements.add(listElement);
        });

        return listElements;
    }

    private static String[] getImageUrls(String k, String p) {

        String json = null;
        try {
            json = Crypto.decrypt(k, p);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        JsonObject object = element.getAsJsonObject();
        JsonArray array = object.getAsJsonArray("content");

        Gson gson = new Gson();
        return gson.fromJson(array, String[].class);
    }

    public static MangaElement getManga(String url) {

        Document doc = getDocument(url);

        String json = doc.select("script[type=\"application/ld+json\"]").first().data();
        // fix json syntax error
        json = json.replaceFirst("},", "}");

        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(json);
        JsonObject object = jsonElement.getAsJsonObject();
        String thumbnail = object.get("thumbnailUrl").getAsString();
        String name = object.get("name").getAsString();
        String issueNumber = object.get("issueNumber").getAsString();
        String datePublished = object.get("datePublished").getAsString();
        JsonObject isPartOf = object.getAsJsonObject("isPartOf");
        String seriesName = isPartOf.get("name").getAsString();
        JsonObject authorObject = isPartOf.getAsJsonObject("author");
        String author = authorObject.get("name").getAsString();

        String html = doc.outerHtml();

        String p = regex("p: \"(.*?)\"", html);
        String k = regex("k: \"(.*?)\"", html);

        String[] images = getImageUrls(k, p);

        MangaElement manga = new MangaElement();

        manga.setThumbnail(thumbnail);
        manga.setName(name);
        manga.setIssueNumber(issueNumber);
        manga.setDatePublished(datePublished);
        manga.setSeriesName(seriesName);
        manga.setAuthor(author);
        manga.setImages(images);

        return manga;
    }
}
