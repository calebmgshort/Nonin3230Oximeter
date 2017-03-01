package com.example.android.nonin3230oximeter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by Caleb on 3/1/17. We are going to ignore this class for the time being.
 */

public class BTLE_Adapter extends SimpleAdapter{

    Activity activity;
    int layoutResourceID;

    public BTLE_Adapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }
/*
    public BTLE_Adapter(Activity activity, int resource) {
        super(activity.getApplicationContext(), resource);
    }
    */
}
