package element;

public class ListElement {

    private String url;
    private String thumbnail;
    private String issueNumber;
    private String date;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "element.ListElement{" +
                "url='" + url + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", issueNumber='" + issueNumber + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
