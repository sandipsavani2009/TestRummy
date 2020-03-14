package in.glg.rummy.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import in.glg.rummy.api.response.BaseResponse;

@Root(name = "request")
public class LogoutRequest extends BaseResponse {
    @Attribute(name = "command", required = false)
    public String command;

    public int getErrorMessage() {
        return 0;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
