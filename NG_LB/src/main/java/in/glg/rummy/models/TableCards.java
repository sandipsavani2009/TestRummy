package in.glg.rummy.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.io.Serializable;
import java.util.List;

public class TableCards implements Serializable {
    @ElementList(inline = true, name = "card")
    private List<PlayingCard> cards;
    @Attribute(name = "table_id", required = false)
    private String tableId;

    public List<PlayingCard> getCards() {
        return this.cards;
    }

    public void setCards(List<PlayingCard> cards) {
        this.cards = cards;
    }

    public String getTableId() {
        return this.tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }
}
