package in.glg.rummy.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import in.glg.rummy.api.requests.Baserequest;

@Root(name = "request", strict = false)
public class BaseTrRequest extends Baserequest {
    @Attribute(name = "command")
    private String command;

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
