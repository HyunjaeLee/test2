
public class Main {

    public static void main(String[] args) throws Exception {

        Manga manga = ManaaSpace.getManga("https://manaa.space/post/uploader/8Z4zVqRMYnldyevb");

        System.out.println(manga.getName());
        System.out.println(manga.getIssueNumber());
        System.out.println(manga.getSeriesName());
        System.out.println(manga.getAuthor());
        System.out.println(manga.getDatePublished());
        System.out.println(manga.getThumbnail());

        for(String url : manga.getImages()) {
            System.out.println(url);
        }
    }
}