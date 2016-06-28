package com.cybussolutions.kluchit.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybussolutions.kluchit.R;

/**
 * Created by Hamza Android on 5/11/2016.
 */
public class Drawer_Addapter extends ArrayAdapter<String>

{
    String title[];
    int imgs[];
    Activity context;



    public Drawer_Addapter(Activity context, String[] title , int[] imgs)
    {
        super(context, R.layout.singlerow,title);
        this.title = title;
        this.context = context;
        this.imgs = imgs;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        View rowView;


        LayoutInflater inflater = context.getLayoutInflater();
        rowView = inflater.inflate(R.layout.row_drawer,null,true);
        TextView name=(TextView) rowView.findViewById(R.id.heading);
        ImageView img = (ImageView) rowView.findViewById(R.id.img);


        name.setText(title[position]);
        img.setImageResource(imgs[position]);

        return rowView;
    }
}
