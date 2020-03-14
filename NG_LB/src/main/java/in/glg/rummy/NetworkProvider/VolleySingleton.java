package in.glg.rummy.NetworkProvider;

/**
 * Created by GridLogic on 20/7/17.
 */

import android.content.Context;
import android.graphics.Bitmap;

import androidx.collection.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


public class VolleySingleton {
    private static VolleySingleton ourInstance;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private VolleySingleton() {
    }

    private VolleySingleton(Context cntx) {
        requestQueue = Volley.newRequestQueue(cntx);

        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    public static VolleySingleton getInstance(Context cntx) {
        if(ourInstance == null)
        {
            ourInstance = new VolleySingleton(cntx);
        }
        return ourInstance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }


}