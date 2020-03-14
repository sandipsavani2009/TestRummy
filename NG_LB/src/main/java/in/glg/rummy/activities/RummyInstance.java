package in.glg.rummy.activities;

import android.content.Context;
import android.content.Intent;

public class RummyInstance {

    Context context;

    public RummyInstance(Context context)
    {
        this.context = context;
    }

    public void inIt(String userId,String merchantId)
    {
        Intent i = new Intent(context, NostraJsonActivity.class);
        i.putExtra("merchant_id",merchantId);
        i.putExtra("userid",userId);
        context.startActivity(i);
    }
}
