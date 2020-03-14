package in.glg.rummy.packagedev.android.api.base.builders.json;

import java.util.List;

public class GenericGsonListResult<T> {
    public Result<T> result;

    public static class Result<T> {
        public List<T> value;
    }
}
