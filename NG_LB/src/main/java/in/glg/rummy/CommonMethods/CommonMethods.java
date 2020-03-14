package in.glg.rummy.CommonMethods;

/**
 * Created by GridLogic on 20/7/17.
 */

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import in.glg.rummy.R;


public class CommonMethods
{
    private static String TAG = "flow";

    public static void loadIconSet()
    {
        Iconify.with(new FontAwesomeModule());
    }

    public static void showSnackbar(View view, String message)
    {
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public static Document convertStringToDocument(String xmlStr)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try
        {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) );
            return doc;
        } catch (Exception e) {
            Log.e(TAG, "EXP: CommonMethods.convertStringToDocument -->> "+e.toString());
        }
        return null;
    }

    public static void showMessageDialog(Context context, String strHeading, String strMessage)
    {
        final Dialog dialog = new Dialog(context, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_message);
        dialog.setCanceledOnTouchOutside(false);

        TextView heading = (TextView) dialog.findViewById(R.id.heading);
        TextView message = (TextView) dialog.findViewById(R.id.message);

        // Button controls
        Button done = (Button) dialog.findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Drawable background = done.getBackground();
        if (background instanceof Drawable) {
            ((Drawable) background).setColorFilter(Color.parseColor("#2196F3"), PorterDuff.Mode.SRC_ATOP);
        }

        if(strMessage!=null && !strMessage.equalsIgnoreCase("") &&  !strMessage.equalsIgnoreCase("null"))
            message.setText(strMessage);

        if(strHeading!=null && !strHeading.equalsIgnoreCase("") &&  !strHeading.equalsIgnoreCase("null"))
            heading.setText(strHeading);
        else
            heading.setVisibility(View.GONE);

        dialog.show();
    }

    public static void showProgress(Context context, String loadingMessage, Dialog dialog)
    {
        if(dialog==null)
            dialog = new Dialog(context, R.style.DialogTheme);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.message);

        if(loadingMessage!=null)
            message.setText(loadingMessage);

        dialog.show();
    }

    public static void hideProgress(Dialog progressDialog)
    {
        //if(progressDialog!=null)
            progressDialog.dismiss();
    }

    public static String checkFaceNumber(String face)
    {
        if(face.equalsIgnoreCase("1"))
            return "A";
        else if(face.equalsIgnoreCase("11"))
            return "J";
        else if(face.equalsIgnoreCase("12"))
            return "Q";
        else if(face.equalsIgnoreCase("13"))
            return "K";
        else
            return face;
    }
}