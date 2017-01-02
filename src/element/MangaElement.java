package element;

import java.util.Arrays;

public class MangaElement {

    private String thumbnail;
    private String name;
    private String issueNumber;
    private String datePublished;
    private String seriesName;
    private String author;
    private String[] images;

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }

    public String getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(String datePublished) {
        this.datePublished = datePublished;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "element.MangaElement{" +
                "thumbnail='" + thumbnail + '\'' +
                ", name='" + name + '\'' +
                ", issueNumber='" + issueNumber + '\'' +
                ", datePublished='" + datePublished + '\'' +
                ", seriesName='" + seriesName + '\'' +
                ", author='" + author + '\'' +
                ", images=" + Arrays.toString(images) +
                '}';
    }
}
