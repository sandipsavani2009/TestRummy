package in.glg.rummy.api.response;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import in.glg.rummy.models.Results;

public class CheckMeldResponse extends BaseResponse {
    @Attribute(name = "data", required = false)
    private String data;
    @Attribute(name = "meldtimer", required = false)
    private String meldTimer;
    @Element(name = "results", required = false)
    private Results results;
    @Attribute(name = "table_id", required = false)
    private String tableId;

    public Results getResults() {
        return this.results;
    }

    public void setResults(Results results) {
        this.results = results;
    }

    public String getMeldTimer() {
        return this.meldTimer;
    }

    public void setMeldTimer(String meldTimer) {
        this.meldTimer = meldTimer;
    }

    public String getTableId() {
        return this.tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getErrorMessage() {
        return 0;
    }
}
