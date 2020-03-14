package in.glg.rummy.api.requests;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "request")
public class LobbyDataRequest extends Baserequest {
    @Attribute(name = "command")
    private String command;

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
