package in.glg.rummy.packagedev.android.api.base.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Button;

import in.glg.rummy.packagedev.android.api.base.builders.BaseBuilder;
import in.glg.rummy.packagedev.android.api.base.requests.AbstractDataRequest;
import in.glg.rummy.packagedev.cmdpatters.Resource;

public class DataApiAsyncTask extends AbstractDataApiAsyncTask {
    private ProgressDialog progressDialog;
    private Button mSubmit;

    public DataApiAsyncTask(boolean showToasts, Context context, ProgressDialog progressDialog, Button mSubmit) {
        super(context, showToasts);
        this.progressDialog = progressDialog;
        this.mSubmit = mSubmit;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        if (this.progressDialog != null) {
            this.progressDialog.show();
        }
    }

    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (this.progressDialog != null) {
            this.progressDialog.cancel();
        }
        this.mSubmit.setEnabled(true);
    }

    protected Void doInBackground(AbstractDataRequest... params) {
        super.doInBackground(params);
        if (params != null && params[0] != null && this.isConnected) {
            params[0].requestDelegate.execute(params[0]);
        } else if (this.context != null) {
            ((BaseBuilder) params[0].requestDelegate).sendOnFailMessage(this.context.getString(Resource.string.no_internet_connection));
        }
        return null;
    }
}
