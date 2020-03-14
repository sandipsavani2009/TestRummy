package in.glg.rummy.packagedev.android.api.base.builders.json;

public interface JsonBuilder {
    <T> T getEntityForJson(String str, Class<T> cls);

    <T> String getJsonForEntity(JsonInterface<T> jsonInterface);
}
