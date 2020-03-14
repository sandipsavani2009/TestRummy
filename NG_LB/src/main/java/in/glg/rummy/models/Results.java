package in.glg.rummy.models;

import org.simpleframework.xml.ElementList;

import java.io.Serializable;
import java.util.List;

public class Results implements Serializable {
    @ElementList(inline = true, name = "result", required = false)
    private List<CheckMeldResult> checkMeldResults;

    public List<CheckMeldResult> getCheckMeldResults() {
        return this.checkMeldResults;
    }

    public void setCheckMeldResults(List<CheckMeldResult> checkMeldResults) {
        this.checkMeldResults = checkMeldResults;
    }
}
