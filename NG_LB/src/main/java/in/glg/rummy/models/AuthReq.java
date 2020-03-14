package in.glg.rummy.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import in.glg.rummy.api.builder.xml.XmlInterface;

@Root(name = "authreq", strict = false)
public class AuthReq implements XmlInterface {
    @Attribute(name = "msg_uuid")
    public String msg_uuid;
    @Attribute(name = "timestamp")
    public String timestamp;
}
