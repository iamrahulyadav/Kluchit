package com.cybussolutions.kluchit.Network;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Queue;

/**
 * Created by Abdul on 25/08/2016.
 */
public class UploadPreferenceManager  {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    int total=0;
    int current=-1;

    UploadPreferenceManager(Context c)
    {
        preferences=c.getSharedPreferences("UploaderService",Context.MODE_PRIVATE);
        editor=preferences.edit();
        if (!preferences.contains("total") && !preferences.contains("current"))
        {
            editor.putInt("total",total);
            editor.putInt("current",current);
            editor.commit();
        }
        else
        {
            total=preferences.getInt("total",0);
            current=preferences.getInt("current",-1);
        }

        add_upload("/storage/emulated/0/Prisma/1ebc3192bfe5c884c7ca6738f046599fresNetFinal_final1.jpg");
        add_upload("/storage/emulated/0/Prisma/4b926967c19bbd5256e5023f329d8cd2resNetFinal_4.jpg");
        add_upload("/storage/emulated/0/Prisma/336103988d7a77ba5fc2bfb927a4fec41507_k15.jpg");
        add_upload("/storage/emulated/0/Prisma/dc18f3772a0554c0f0230c933e8e9035resNet7_5.jpg");

    }
    boolean is_empty()
    {
        if (total==0)
            return true;
        return false;
    }
    boolean add_upload(String url)
    {
        String url_index="url"+String.valueOf(total++);//url0
        editor.putString(url_index,url);
        editor.putInt("total",total);
        editor.commit();
        return true;
    }
    boolean is_uploaded()
    {
        editor.putInt("current",current);
        editor.commit();

        if (is_finished())
        {
            editor.clear();
            editor.commit();
        }
        return true;

    }
    boolean is_finished()
    {
        if (current==total)
        {
            editor.clear();
            editor.commit();
            return true;
        }
        return false;
    }
    String get_current_url()
    {
        String url_index="url"+String.valueOf(++current);//url0
        return preferences.getString(url_index,null);
    }
}
