package com.cybussolutions.kluchit.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cybussolutions.kluchit.R;

/**
 * Created by Abdullah Manzoor Dar on 7/18/2016.
 */
public class GettingStarted extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_getting_started, container, false);
    }
}
