package in.glg.rummy.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import in.glg.rummy.api.requests.Baserequest;

@Root(name = "request", strict = false)
public class SearchTableRequest extends Baserequest {
    @Attribute(name = "bet")
    private String bet;
    @Attribute(name = "command")
    private String command;
    @Attribute(name = "conversion")
    private String conversion;
    @Attribute(name = "gamesettings_id")
    private String gamesettingsId;
    @Attribute(name = "maxplayers")
    private String maxPlayers;
    @Attribute(name = "nick_name")
    private String nickName;
    @Attribute(name = "stream_id")
    private String streamId;
    @Attribute(name = "stream_name")
    private String streamName;
    @Attribute(name = "table_cost")
    private String tableCost;
    @Attribute(name = "table_id")
    private String tableId;
    @Attribute(name = "table_type")
    private String tableType;
    @Attribute(name = "user_id")
    private String userId;

    public String getStreamId() {
        return this.streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getStreamName() {
        return this.streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getGamesettingsId() {
        return this.gamesettingsId;
    }

    public void setGamesettingsId(String gamesettingsId) {
        this.gamesettingsId = gamesettingsId;
    }

    public String getBet() {
        return this.bet;
    }

    public void setBet(String bet) {
        this.bet = bet;
    }

    public String getMaxPlayers() {
        return this.maxPlayers;
    }

    public void setMaxPlayers(String maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getConversion() {
        return this.conversion;
    }

    public void setConversion(String conversion) {
        this.conversion = conversion;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getTableId() {
        return this.tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getTableType() {
        return this.tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getTableCost() {
        return this.tableCost;
    }

    public void setTableCost(String tableCost) {
        this.tableCost = tableCost;
    }
}
