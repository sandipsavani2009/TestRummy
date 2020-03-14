package in.glg.rummy.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.io.Serializable;
import java.util.List;

public class Stack implements Serializable {
    @Attribute(name = "face", required = false)
    private String face;
    @ElementList(name = "face_down_stack", required = false)
    private List<PlayingCard> faceDownStack;
    @ElementList(name = "face_up_stack", required = false)
    private List<PlayingCard> faceUpStack;
    @Attribute(name = "jocker", required = false)
    private String jocker;
    @Attribute(name = "suit", required = false)
    private String suit;
    @Attribute(name = "table_id", required = false)
    private String tableId;

    public List<PlayingCard> getFaceUpStack() {
        return this.faceUpStack;
    }

    public void setFaceUpStack(List<PlayingCard> faceUpStack) {
        this.faceUpStack = faceUpStack;
    }

    public String getTableId() {
        return this.tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getJocker() {
        return this.jocker;
    }

    public void setJocker(String jocker) {
        this.jocker = jocker;
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

    public List<PlayingCard> getFaceDownStack() {
        return this.faceDownStack;
    }

    public void setFaceDownStack(List<PlayingCard> faceDownStack) {
        this.faceDownStack = faceDownStack;
    }
}
