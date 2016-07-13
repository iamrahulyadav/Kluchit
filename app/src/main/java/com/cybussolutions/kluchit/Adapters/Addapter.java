package com.cybussolutions.kluchit.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cybussolutions.kluchit.DataModels.Main_screen_pojo;
import com.cybussolutions.kluchit.R;

import java.util.ArrayList;


public class Addapter extends ArrayAdapter<String> {




    String heading[];
    String title[];


    public ArrayList<Main_screen_pojo> list ;


    Activity activity;

    public Addapter(Context context, int resource, ArrayList<Main_screen_pojo> list, Activity activity) {
        super(context, resource);
        this.list = list;
        this.activity = activity;
    }




    @Override
    public int getCount() {
        return list.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        View rowView;


        LayoutInflater inflater = activity.getLayoutInflater();
        rowView = inflater.inflate(R.layout.row_profile,null,true);
        TextView name=(TextView) rowView.findViewById(R.id.userid);
        TextView job_id = (TextView) rowView.findViewById(R.id.job_id);
        TextView specializtion=(TextView) rowView.findViewById(R.id.specialization);
        TextView department=(TextView) rowView.findViewById(R.id.department);


        Main_screen_pojo screen_pojo = list.get(position);


        name.setText(screen_pojo.getMaintxt());
        specializtion.setText(screen_pojo.getDiscription());
        department.setText(screen_pojo.getCatagory());
        job_id.setText(screen_pojo.getJob_id());






        return rowView;
    }


}
