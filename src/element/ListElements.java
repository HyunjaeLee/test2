package element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class ListElements implements Iterable<ListElement> {

    private String thumbnail;
    private String name;
    private String author;
    private List<ListElement> listElements;

    public ListElements() {
        listElements = new ArrayList<>();
    }

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean add(ListElement listElement) {
        return this.listElements.add(listElement);
    }

    @Override
    public Iterator<ListElement> iterator() {
        return this.listElements.iterator();
    }

    @Override
    public void forEach(Consumer<? super ListElement> action) {
        this.listElements.forEach(action);
    }

    @Override
    public String toString() {
        return "element.ListElements{" +
                "thumbnail='" + thumbnail + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", listElements=" + listElements +
                '}';
    }
}
