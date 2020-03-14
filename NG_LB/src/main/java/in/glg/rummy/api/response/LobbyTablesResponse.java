package in.glg.rummy.api.response;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.util.List;

import in.glg.rummy.models.Table;

public class LobbyTablesResponse extends BaseResponse {
    @Attribute(name = "data", required = false)
    private String data;
    @ElementList(name = "tables", required = false)
    private List<Table> tables;

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<Table> getTables() {
        return this.tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public int getErrorMessage() {
        return 0;
    }
}
