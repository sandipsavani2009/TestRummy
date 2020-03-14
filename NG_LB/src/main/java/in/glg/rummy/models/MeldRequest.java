package in.glg.rummy.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

import in.glg.rummy.api.requests.Baserequest;

@Root(name = "request")
public class MeldRequest extends Baserequest {
    @Attribute(name = "command")
    private String command;
    @Attribute(name = "face")
    private String face;
    @ElementList(inline = true, name = "box", required = false)
    private List<MeldBox> meldBoxes;
    @Attribute(name = "suit")
    private String suit;
    @Attribute(name = "table_id")
    private String tableId;

    public String getTableId() {
        return this.tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getFace() {
        return this.face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public String getSuit() {
        return this.suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<MeldBox> getMeldBoxes() {
        return this.meldBoxes;
    }

    public void setMeldBoxes(List<MeldBox> meldBoxes) {
        this.meldBoxes = meldBoxes;
    }
}
