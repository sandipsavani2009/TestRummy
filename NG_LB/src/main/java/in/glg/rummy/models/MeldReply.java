package in.glg.rummy.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

import in.glg.rummy.api.requests.Baserequest;

@Root(name = "reply")
public class MeldReply extends Baserequest {
    @ElementList(inline = true, name = "box", required = false)
    private List<MeldBox> meldBoxes;
    @Attribute(name = "table_id")
    private String tableId;
    @Attribute(name = "text")
    private String text;
    @Attribute(name = "timestamp")
    private String timeStamp;
    @Attribute(name = "type")
    private String type;

    public String getTableId() {
        return this.tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public List<MeldBox> getMeldBoxes() {
        return this.meldBoxes;
    }

    public void setMeldBoxes(List<MeldBox> meldBoxes) {
        this.meldBoxes = meldBoxes;
    }
}
