package com.example.lightconnect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PresetsAdapter extends ArrayAdapter<String> {


    //List<String> labels = db.getAllLabels();
    public PresetsAdapter(Context context, List<String> lables){
        super(context, 0, lables);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }


    private View initView(int position, View convertView, ViewGroup parent){
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.presets_spinner, parent, false );

        }

        View Viewflag = convertView.findViewById(R.id.preset_colours);
        TextView TextViewflag = convertView.findViewById(R.id.textView6);



        int red =0;
        int blue =0;
        int green =0;
        String name = "";
        SQLiteOpenHelper database = new SQL_base(getContext());
        SQLiteDatabase db = database.getReadableDatabase();

        String queryString = "SELECT * FROM " + "presets" ;


        Cursor cursor = db.rawQuery(queryString, null);//selectQuery,selectedArguments

        cursor.moveToPosition(position);

        if (cursor != null && cursor.getCount() > 0) {

            red = cursor.getInt(1);
            green = cursor.getInt(2);
            blue = cursor.getInt(3);
            name = cursor.getString(0);
            Viewflag.setBackgroundColor(Color.rgb(red, green, blue));
            TextViewflag.setText(name);

        }

        cursor.close();

        return convertView;
    }
}
