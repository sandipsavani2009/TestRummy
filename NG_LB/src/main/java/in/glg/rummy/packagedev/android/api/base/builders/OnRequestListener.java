package in.glg.rummy.packagedev.android.api.base.builders;

import in.glg.rummy.packagedev.android.api.base.result.DataResult;

public interface OnRequestListener {
    <T> void onRequestFail(DataResult<T> dataResult);

    <T> void onRequestFail(String str);

    <T> void onRequestResult(DataResult<T> dataResult);

    <T> void onRequestStart();
}
