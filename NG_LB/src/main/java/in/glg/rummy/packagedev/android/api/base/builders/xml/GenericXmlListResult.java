package in.glg.rummy.packagedev.android.api.base.builders.xml;

import java.util.List;

public class GenericXmlListResult<T> {
    public Result<T> result;

    public static class Result<T> {
        public List<T> value;
    }
}
