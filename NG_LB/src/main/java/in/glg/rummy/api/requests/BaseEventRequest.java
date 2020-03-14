package in.glg.rummy.api.requests;

import org.simpleframework.xml.Attribute;

import in.glg.rummy.api.builder.xml.XmlInterface;

public class BaseEventRequest implements XmlInterface {
    @Attribute(name = "msg_uuid")
    private String uuid;

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
