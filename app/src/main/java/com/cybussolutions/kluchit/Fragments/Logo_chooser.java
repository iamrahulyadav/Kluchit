package com.cybussolutions.kluchit.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.cybussolutions.kluchit.R;


/**
 * Created by Abdullah Manzoor Dar on 8/9/2016.
 */
public class Logo_chooser extends Fragment {


    ListView grid;


    String[] web = {
            "Top Left",
            "Top Right",
            "Bottom Left",
            "Bottom Right",
            "Middle",
            "kjklk"
    } ;


    int[] imageId = {
            R.drawable.log_one_a,
            R.drawable.log_two_a,
            R.drawable.log_three_a,
            R.drawable.log_four_a,
            R.drawable.log_five_a,
            R.drawable.log_six_a
    };


    public class CustomGrid extends BaseAdapter {
        private Context mContext;
        private final String[] web;
        private final int[] Imageid;

        public CustomGrid(Context c,String[] web,int[] Imageid ) {
            mContext = c;
            this.Imageid = Imageid;
            this.web = web;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return imageId.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View grid;
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {

                grid = new View(mContext);
                grid = inflater.inflate(R.layout.icon, null);
                ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image);
                imageView.setImageResource(Imageid[position]);
            } else {
                grid = (View) convertView;
            }

            return grid;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.logos, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        grid=(ListView) view.findViewById(R.id.list);
        CustomGrid adapter = new CustomGrid(getActivity().getApplicationContext(), web, imageId);
        grid.setAdapter(adapter);


        view.findViewById(R.id.linerar).setBackgroundResource(R.color.colorAccent);

    }

}
