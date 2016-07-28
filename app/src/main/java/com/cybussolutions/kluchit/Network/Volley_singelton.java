package com.cybussolutions.kluchit.Network;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


public class Volley_singelton {

    public static Volley_singelton mInstance =null;
    private RequestQueue requestQueue;
    private ImageLoader loader;
    private Volley_singelton() {
        requestQueue = Volley.newRequestQueue(Application.getContext());
        loader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {

            private LruCache<String,Bitmap> lruCache = new LruCache<>((int)(Runtime.getRuntime().maxMemory()/1024/8));
            @Override
            public Bitmap getBitmap(String url) {


                return lruCache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {

                lruCache.put(url,bitmap);

            }
        });
    }






    public static Volley_singelton getInstance()
    {
        if(mInstance==null){
            mInstance=new Volley_singelton();
        }

        return mInstance;
    }

    public RequestQueue getResquest(){
        return requestQueue;
    }

    public ImageLoader getLoader()
    {
        return loader;
    }
}


