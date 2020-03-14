package in.glg.rummy.models;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

@Root(name = "box")
public class MeldBox implements Serializable {
    @ElementList(inline = true, name = "card", required = false)
    private List<PlayingCard> meldBoxes;

    public List<PlayingCard> getMeldBoxes() {
        return this.meldBoxes;
    }

    public void setMeldBoxes(List<PlayingCard> meldBoxes) {
        this.meldBoxes = meldBoxes;
    }
}
