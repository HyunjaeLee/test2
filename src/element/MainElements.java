package element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class MainElements implements Iterable<MainElement> {

    private List<MainElement> mainElements;

    public MainElements() {
        this.mainElements = new ArrayList<>();
    }

    public boolean add(MainElement mainElement) {
        return this.mainElements.add(mainElement);
    }

    public Iterator<MainElement> iterator() {
        return this.mainElements.iterator();
    }

    public void forEach(Consumer<? super MainElement> action) {
        this.mainElements.forEach(action);
    }

    @Override
    public String toString() {
        return "MainElements{" +
                "mainElements=" + mainElements +
                '}';
    }
}
