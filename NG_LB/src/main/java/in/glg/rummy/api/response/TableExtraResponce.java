package in.glg.rummy.api.response;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.List;

import in.glg.rummy.models.Event;
import in.glg.rummy.models.PickDiscard;
import in.glg.rummy.models.ScoreBoard;
import in.glg.rummy.models.TableDetails;

public class TableExtraResponce extends BaseResponse {
    @Attribute(name = "data", required = false)
    private String data;
    @Element(name = "event", required = false)
    private Event event;
    @Attribute(name = "fun_chips", required = false)
    private String funChips;
    @Attribute(name = "message", required = false)
    private String message;
    @Attribute(name = "nick_name", required = false)
    private String nickName;
    @ElementList(name = "pickdiscards", required = false)
    private List<PickDiscard> pickDiscardsList;
    @Element(name = "scoreboard", required = false)
    private ScoreBoard scoreBoard;
    @Attribute(name = "table_id", required = false)
    private String tableId;
    @Element(name = "table_details", required = false)
    private TableDetails table_details;
    @Attribute(name = "tournament_table", required = false)
    private String tournamentTable;

    public String getTournamentTable() {
        return this.tournamentTable;
    }

    public void setTournamentTable(String tournamentTable) {
        this.tournamentTable = tournamentTable;
    }

    public List<PickDiscard> getPickDiscardsList() {
        return this.pickDiscardsList;
    }

    public void setPickDiscardsList(List<PickDiscard> pickDiscardsList) {
        this.pickDiscardsList = pickDiscardsList;
    }

    public ScoreBoard getScoreBoard() {
        return this.scoreBoard;
    }

    public void setScoreBoard(ScoreBoard scoreBoard) {
        this.scoreBoard = scoreBoard;
    }

    public Event getEvent() {
        return this.event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getErrorMessage() {
        return 0;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTableId() {
        return this.tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public TableDetails getTableDeatils() {
        return this.table_details;
    }

    public void setTableDetails(TableDetails tables) {
        this.table_details = tables;
    }

    public TableDetails getTable_details() {
        return this.table_details;
    }

    public void setTable_details(TableDetails table_details) {
        this.table_details = table_details;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getFunChips() {
        return this.funChips;
    }

    public void setFunChips(String funchips) {
        this.funChips = funchips;
    }
}
