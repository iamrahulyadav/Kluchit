package com.cybussolutions.kluchit.Adapters;

import android.content.Context;
import android.widget.CheckBox;

/**
 * Created by Aaybee on 6/20/2016.
 */
public class Category_Job
{
    public String id;
    public CheckBox checkBox;
    public Category_Job(String title,Context c,String id) {
        checkBox = new CheckBox(c);
        checkBox.setText(title);
        checkBox.setChecked(false);
        this.id=id;
    }
}