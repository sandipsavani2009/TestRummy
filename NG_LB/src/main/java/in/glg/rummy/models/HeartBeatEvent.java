package in.glg.rummy.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import in.glg.rummy.api.response.BaseResponse;

@Root(name = "event")
public class HeartBeatEvent extends BaseResponse {
    @Attribute(name = "event_name", required = false)
    private String eventName;
    @Attribute(name = "nick_name", required = false)
    private String nickName;
    @Attribute(name = "player_in", required = false)
    private String playerIn;
    @Element(name = "table", required = false)
    private TableCards tableCards;

    public TableCards getTable() {
        return this.tableCards;
    }

    public void setTable(TableCards table) {
        this.tableCards = table;
    }

    public String getEventName() {
        return this.eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPlayerIn() {
        return this.playerIn;
    }

    public void setPlayerIn(String playerIn) {
        this.playerIn = playerIn;
    }

    public int getErrorMessage() {
        return 0;
    }
}
