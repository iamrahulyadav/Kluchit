package com.cybussolutions.kluchit.Activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.cybussolutions.kluchit.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abdul on 05/09/2016.
 */
public class SocialSharingCusDef extends Activity {

    Spinner color_spinner;
    Spinner position_spinner;
    RadioGroup category;
    RadioButton default_;
    RadioButton custom_;
    ImageButton submit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.social_sharing_asker);


        color_spinner= (Spinner) findViewById(R.id.color);
        position_spinner = (Spinner) findViewById(R.id.position);
        submit=(ImageButton)findViewById(R.id.button);

        List<String> colors = new ArrayList<String>();
        colors.add("select logo color");
        colors.add("yellow");
        colors.add("red");
        colors.add("purple");
        colors.add("blue");
        colors.add("green");
        colors.add("pink");


        List<String> position = new ArrayList<String>();
        position.add("select logo position");
        position.add("top left");
        position.add("top right");
        position.add("bottom left");
        position.add("bottom right");
        position.add("middle");



        //top left, top right, bottom left, bottom right, middle

        // Creating adapter for spinner
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, colors);

        // Drop down layout style - list view with radio button
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        color_spinner.setAdapter(colorAdapter);



        //top left, top right, bottom left, bottom right, middle

        // Creating adapter for spinner
        ArrayAdapter<String> positionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, position);

        // Drop down layout style - list view with radio button
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        position_spinner.setAdapter(positionAdapter);


        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


       color_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               if (position==0)
               {
                   SharedPreferences preferences = getApplicationContext().getSharedPreferences("CameraPref", Context.MODE_PRIVATE);
                   SharedPreferences.Editor editor = preferences.edit();
                   editor.remove("color");
                   editor.commit();
               }
               else
               {
                   SharedPreferences preferences = getApplicationContext().getSharedPreferences("CameraPref", Context.MODE_PRIVATE);
                   SharedPreferences.Editor editor = preferences.edit();
                   editor.putString("color", String.valueOf(position));
                   editor.commit();
               }
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {

           }
       });

        position_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position==0)
                {
                    SharedPreferences preferences = getApplicationContext().getSharedPreferences("CameraPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("position");
                    editor.commit();
                }
                else
                {
                    SharedPreferences preferences = getApplicationContext().getSharedPreferences("CameraPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("position", String.valueOf(position));
                    editor.commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        category=(RadioGroup) findViewById(R.id.radioGroup);
        default_=(RadioButton) findViewById(R.id.def);
        custom_=(RadioButton)findViewById(R.id.cus);



        default_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide_custom_settings();
            }
        });


        custom_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_custom_settings();
            }
        });

        hide_custom_settings();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getApplicationContext().getSharedPreferences("CameraPref", Context.MODE_PRIVATE);
                if (category.getCheckedRadioButtonId()==custom_.getId()) {
                    if (preferences.contains("color") && preferences.contains("position")) {
                        //Toast.makeText(SocialSharingCusDef.this,"Call intent for custom",Toast.LENGTH_LONG).show();
                        finish();
                        Intent intent=new Intent(SocialSharingCusDef.this,CustomSocialSharing.class);
                        startActivity(intent);
                    }
                    else
                    {
                        custom_.setError("Please Select color and position to continue");
                    }
                }

                else
                {
                    finish();
                    Intent intent=new Intent(SocialSharingCusDef.this,SocialSharing.class);
                    startActivity(intent);
                }
            }
        });

    }

    void hide_custom_settings()
    {
        color_spinner.setVisibility(View.INVISIBLE);
        position_spinner.setVisibility(View.INVISIBLE);
    }

    void show_custom_settings()
    {
        color_spinner.setVisibility(View.VISIBLE);
        position_spinner.setVisibility(View.VISIBLE);
    }


}
