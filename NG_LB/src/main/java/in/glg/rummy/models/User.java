package in.glg.rummy.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

@Root(name = "user")
public class User implements Serializable {
    @Attribute(name = "DEVICE_ID", required = false)
    private String DEVICE_ID;
    @Attribute(name = "aienable", required = false)
    private String aiEnable;
    @Attribute(name = "autoplay", required = false)
    private String autoplay;
    @Attribute(name = "autoplay_count", required = false)
    private String autoplay_count;
    @Attribute(name = "buyinammount", required = false)
    private String buyinammount;
    @Attribute(name = "char_no", required = false)
    private String char_no;
    @Attribute(name = "device_info_id", required = false)
    private String deviceInfoId;
    @Attribute(name = "face", required = false)
    private String face;
    @Attribute(name = "gender", required = false)
    private String gender;
    @ElementList(name = "meld", required = false)
    private List<MeldBox> meldList;
    @Attribute(name = "minimumbuyin", required = false)
    private String minimumBuyin;
    @Attribute(name = "nick_name", required = false)
    private String nick_name;
    @Attribute(name = "place", required = false)
    private String place;
    @Attribute(name = "player_type", required = false)
    private String playerType;
    @Attribute(name = "playerlevel", required = false)
    private String playerlevel;
    @Attribute(name = "points", required = false)
    private String points;
    @Attribute(name = "rejoin", required = false)
    public String reJoin;
    @Attribute(name = "result", required = false)
    private String result;
    @Attribute(name = "sc_use", required = false)
    private String scUse;
    @Attribute(name = "score", required = false)
    private String score;
    @Attribute(name = "seat", required = false)
    private String seat;
    @Attribute(name = "suit", required = false)
    private String suit;
    @Attribute(name = "table_score", required = false)
    public String tableScore;
    @Attribute(name = "total_score", required = false)
    private String totalScore;
    @Attribute(name = "type", required = false)
    private String type;
    @Attribute(name = "user_id", required = false)
    private String user_id;

    public String getAutoplay() {
        return this.autoplay;
    }

    public void setAutoplay(String autoplay) {
        this.autoplay = autoplay;
    }

    public String getAutoplay_count() {
        return this.autoplay_count;
    }

    public void setAutoplay_count(String autoplay_count) {
        this.autoplay_count = autoplay_count;
    }

    public String getBuyinammount() {
        return this.buyinammount;
    }

    public void setBuyinammount(String buyinammount) {
        this.buyinammount = buyinammount;
    }

    public String getChar_no() {
        return this.char_no;
    }

    public void setChar_no(String char_no) {
        this.char_no = char_no;
    }

    public String getNick_name() {
        return this.nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPlace() {
        return this.place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getPlayerlevel() {
        return this.playerlevel;
    }

    public void setPlayerlevel(String playerlevel) {
        this.playerlevel = playerlevel;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser_id() {
        return this.user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDEVICE_ID() {
        return this.DEVICE_ID;
    }

    public void setDEVICE_ID(String DEVICE_ID) {
        this.DEVICE_ID = DEVICE_ID;
    }

    public String getAiEnable() {
        return this.aiEnable;
    }

    public void setAiEnable(String aiEnable) {
        this.aiEnable = aiEnable;
    }

    public String getDeviceInfoId() {
        return this.deviceInfoId;
    }

    public void setDeviceInfoId(String deviceInfoId) {
        this.deviceInfoId = deviceInfoId;
    }

    public String getSeat() {
        return this.seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public String getScore() {
        return this.score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getPoints() {
        return this.points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getScUse() {
        return this.scUse;
    }

    public void setScUse(String scUse) {
        this.scUse = scUse;
    }

    public String getTotalScore() {
        return this.totalScore;
    }

    public void setTotalScore(String totalScore) {
        this.totalScore = totalScore;
    }

    public List<MeldBox> getMeldList() {
        return this.meldList;
    }

    public void setMeldList(List<MeldBox> meldList) {
        this.meldList = meldList;
    }

    public String getSuit() {
        return this.suit;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public String getFace() {
        return this.face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public String getPlayerType() {
        return this.playerType;
    }

    public void setPlayerType(String playerType) {
        this.playerType = playerType;
    }

    public String getMinimumBuyin() {
        return this.minimumBuyin;
    }

    public void setMinimumBuyin(String minimumBuyin) {
        this.minimumBuyin = minimumBuyin;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTableScore() {
        return this.tableScore;
    }

    public void setTableScore(String tableScore) {
        this.tableScore = tableScore;
    }

    public String getReJoin() {
        return this.reJoin;
    }

    public void setReJoin(String reJoin) {
        this.reJoin = reJoin;
    }
}
