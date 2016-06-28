package com.cybussolutions.kluchit.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cybussolutions.kluchit.R;


public class Addapter extends ArrayAdapter<String> {




    String heading[];
    String title[];
    String txt[];

    Activity context;



    public Addapter(Activity context, String[] heading , String[] title, String[] txt)
    {
        super(context, R.layout.singlerow,heading);
        this.heading = heading;
        this.context = context;
        this.title = title;
        this.txt = txt;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        View rowView;


        LayoutInflater inflater = context.getLayoutInflater();
        rowView = inflater.inflate(R.layout.singlerow,null,true);
        TextView name=(TextView) rowView.findViewById(R.id.userid);
        TextView specializtion=(TextView) rowView.findViewById(R.id.specialization);
        TextView department=(TextView) rowView.findViewById(R.id.department);





        name.setText(heading[position]);
        specializtion.setText(title[position]);
        department.setText(txt[position]);






        return rowView;
    }
}
