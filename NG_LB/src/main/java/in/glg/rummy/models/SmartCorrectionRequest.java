package in.glg.rummy.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import in.glg.rummy.api.requests.Baserequest;

@Root(name = "reply")
public class SmartCorrectionRequest extends Baserequest {
    @Attribute(name = "agree", required = false)
    private String agree;
    @Attribute(name = "command", required = false)
    private String command;
    @Attribute(name = "game_id", required = false)
    private String gameId;
    @Attribute(name = "nick_name", required = false)
    private String nickName;
    @Attribute(name = "table_id", required = false)
    private String tableId;
    @Attribute(name = "text", required = false)
    private String text;
    @Attribute(name = "timestamp", required = false)
    private String timeStamp;
    @Attribute(name = "type", required = false)
    private String type;
    @Attribute(name = "user_id", required = false)
    private String userId;

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

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGameId() {
        return this.gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getAgree() {
        return this.agree;
    }

    public void setAgree(String agree) {
        this.agree = agree;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
