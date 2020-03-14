package in.glg.rummy.api.builder.xml;

import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.StringWriter;
import java.io.Writer;

public class CommonXmlBuilder {
    public static final String TAG = "flow";

    public <T> T getEntityForXml(String xmlDoc, Class<? extends T> entity) {
        try {
            return (T) new Persister().read((Class) entity, xmlDoc);
        } catch (Exception e) {
            Log.e(TAG, String.format("%s  ", new Object[]{getClass().getName()}), e);
            return null;
        }
    }

    public <T> String getXmlForEntity(XmlInterface<T> entity)
    {
        Serializer xmlSerializer = new Persister();
        Writer stringWriter = new StringWriter();
        try
        {
            xmlSerializer.write((Object) entity, stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            Log.e(TAG, String.format("%s  ", new Object[]{getClass().getName()}), e);
            return null;
        }
    }
}
