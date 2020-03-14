package in.glg.rummy.packagedev.android.api.base.builders.xml;

public interface XmlBuilder {
    <T> T getEntityForJson(String str, Class<T> cls);

    <T> String getJsonForEntity(XmlInterface<T> xmlInterface);
}
