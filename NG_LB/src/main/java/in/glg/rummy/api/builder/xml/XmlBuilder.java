package in.glg.rummy.api.builder.xml;

public interface XmlBuilder {
    <T> T getEntityForJson(String str, Class<T> cls);

    <T> String getJsonForEntity(XmlInterface<T> xmlInterface);
}
