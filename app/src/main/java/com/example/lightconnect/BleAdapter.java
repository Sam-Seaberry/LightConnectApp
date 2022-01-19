package com.example.lightconnect;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class BleAdapter extends ArrayAdapter<String> {

    StringBuilder namestr[],MACstr[];

    //List<String> labels = db.getAllLabels();
    public BleAdapter(Context context, String[] A,String B[] ){
        super(context, 0, A);
        namestr = new StringBuilder[0];
        MACstr = new StringBuilder[0];


        namestr.toString().equals(A.toString());
        MACstr.toString().equals(A.toString());

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


        final ViewHolder holder;

        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ble_spinner, parent, false );
            holder = new ViewHolder();
            holder.mName = (TextView)convertView.findViewById(R.id.textview1);
            holder.mMAC = (TextView)convertView.findViewById(R.id.textView6);
            convertView.setTag(holder);

        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        holder.mName.setText(namestr[position]);
        holder.mMAC.setText(MACstr[position]);
        holder.mMAC.setVisibility(View.INVISIBLE);


        return convertView;
    }

    public void add(String name, String address) {
        namestr.toString().concat(name);
        MACstr.toString().concat(address);
    }

    private class ViewHolder {
        private TextView mName;
        private TextView mMAC;
    }
}
