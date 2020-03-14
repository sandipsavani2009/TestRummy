package in.glg.rummy.packagedev.android.api.base.builders.json;

import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CommonJsonBuilder {
    public static final String TAG = CommonJsonBuilder.class.getSimpleName();

    public <T> T getEntityForJson(String json, Class<T> entity) {
        try {
            return new Gson().fromJson(json, entity);
        } catch (Exception e) {
            Log.e(TAG, String.format("%s  ", new Object[]{getClass().getName()}), e);
            return null;
        }
    }

    public <T> String getJsonForEntity(JsonInterface<T> entity) {
        try {
            return new Gson().toJson(entity);
        } catch (Exception e) {
            Log.e(TAG, String.format("%s  ", new Object[]{getClass().getName()}), e);
            return null;
        }
    }

    public <T> Type getEntityForJson(String json, Type type) {
        try {
            return (Type) new Gson().fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, String.format("%s  ", new Object[]{getClass().getName()}), e);
            return null;
        }
    }

    public <T> GenericGsonListResult<T> getListOfTypeForJson(String json, Type type) {
        try {
            return (GenericGsonListResult) new Gson().fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, String.format("%s  ", new Object[]{getClass().getName()}), e);
            return null;
        }
    }

    public <T> GenericGsonResult<T> getTypeForJson(String json, Type type) {
        try {
            return (GenericGsonResult) new Gson().fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, String.format("%s  ", new Object[]{getClass().getName()}), e);
            return null;
        }
    }

    public <T> List<T> getListForJson(String json, Type type) {
        try {
            return (ArrayList) new Gson().fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, String.format("%s  ", new Object[]{getClass().getName()}), e);
            return null;
        }
    }
}
