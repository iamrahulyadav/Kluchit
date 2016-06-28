package com.cybussolutions.kluchit.Adapters;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.cybussolutions.kluchit.R;

import java.util.HashMap;

public class Category_adapter extends ArrayAdapter<Category_Job> {


    Category_Job title[];
    Activity context;
    CheckBox checkBox;
    public HashMap<Integer,String> values;


    public Category_adapter(Activity context, Category_Job[] title)
    {
        super(context, R.layout.category_row,title);
        this.title = title;
        this.context = context;
        values=new HashMap<Integer, String>();
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {

        View rowView;


        LayoutInflater inflater = context.getLayoutInflater();
        rowView = inflater.inflate(R.layout.category_row,null,true);

        checkBox = (CheckBox) rowView.findViewById(R.id.category_check);

        checkBox.setText(title[position].checkBox.getText());

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CheckBox temp=(CheckBox)(getItem(position).checkBox);
                if (!values.containsKey(position)) {
                    values.put(position, (String) temp.getText());
                    //Toast.makeText(getContext(),values.get(position),Toast.LENGTH_LONG).show();
                    //Toast.makeText(getContext(),"IN",Toast.LENGTH_LONG).show();
                }
                else
                {
                    values.remove(position);
                    //Toast.makeText(getContext(),"Out",Toast.LENGTH_LONG).show();
                }

            }
        });


        return rowView;
    }
}
