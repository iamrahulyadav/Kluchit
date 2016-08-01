package com.cybussolutions.kluchit.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cybussolutions.kluchit.DataModels.History_model;
import com.cybussolutions.kluchit.DataModels.Questions;
import com.cybussolutions.kluchit.R;

import java.util.ArrayList;

/**
 * Created by Hamza Android on 7/29/2016.
 */
public class history_adapter  extends ArrayAdapter<Questions> {

    public ArrayList<History_model> list;
    Activity activity;

    public history_adapter(Context context, int resource, ArrayList<History_model> list, Activity activity) {
        super(context, resource);

        this.list = list;
        this.activity = activity;
    }
    public int getCount() {
        return list.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View rowView, final ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        rowView = inflater.inflate(R.layout.ques_single_row, parent, false);
        TextView txt = (TextView) rowView.findViewById(R.id.q_txt);
        final ImageView positiveimage = (ImageView) rowView.findViewById(R.id.ans_img);
        final ImageView crossimage = (ImageView) rowView.findViewById(R.id.cross_image);

        final History_model ques = list.get(position);

        txt.setText(ques.getQuestiontxt());



        if(ques.getDescription()=="null" && ques.getQuestionanswer().equals("1"))
        {
            positiveimage.setBackgroundResource(R.drawable.thumbsupgreen);
        }

        else
        {
            crossimage.setBackgroundResource(R.drawable.thumbdownred);

            crossimage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(),ques.getDescription().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }





        return rowView;

    }
    }
