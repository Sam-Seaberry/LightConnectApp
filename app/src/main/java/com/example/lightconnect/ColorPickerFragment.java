package com.example.lightconnect;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import carbon.widget.FrameLayout;

public class ColorPickerFragment extends Fragment implements SeekBar.OnSeekBarChangeListener{

    private ImageView mColourWheel;
    private View mColorView;
    Bitmap bitmap;
    private int mColorVal;
    private int red,blue,green,brightness;
    private SeekBar mRed, mGreen, mBlue, mBrightness;

    CallBackFragment callBackFragment;

    //for presets
    private Spinner mPresets;
    private ArrayList<presets_items> mPresetList;
    private PresetsAdapter mAdapter;

    Context mContext;

    private RGBvalues mRGB;
    //new instances
    private ImageView mSaveButton;
    private ImageView mApplyButton;

    //sql instances
    private SQL_base mSQL;
    private FrameLayout mFrame;

    Bluetooth_Fragment mFragment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.colorpicker_frag,container,false);

        mColourWheel = view.findViewById(R.id.imageView);
        mColorView = view.findViewById(R.id.display_colors);
        mColourWheel.setDrawingCacheEnabled(true);
        mColourWheel.buildDrawingCache(true);

        mFrame = view.findViewById(R.id.framelay);

        mRGB = new RGBvalues();

        mSaveButton = view.findViewById(R.id.button4);
        mApplyButton = view.findViewById(R.id.button3);

        mRed = view.findViewById(R.id.seekBar);
        mGreen = view.findViewById(R.id.seekBar2);
        mBlue = view.findViewById(R.id.seekBar3);
        mBrightness = view.findViewById(R.id.seekBar4);

        mContext = container.getContext();

        setchangelister();

        mSQL = new SQL_base(mContext);

        mPresets= view.findViewById(R.id.spinner2);
        mPresetList = new ArrayList<>();

        loadSpinnerData();
        mPresets.setAdapter(mAdapter);

        mPresets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String label="";

                try {
                    label = parent.getItemAtPosition(position).toString();
                } catch (Exception e) {
                    System.out.println("Error " + e.getMessage());

                }
                Toast.makeText(parent.getContext(), "You selected: " + label, Toast.LENGTH_LONG).show();

                getSQLdata(label);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mColourWheel.setOnTouchListener((v, event) -> {
            if(event.getAction()== MotionEvent.ACTION_DOWN || event.getAction()== MotionEvent.ACTION_MOVE){
                bitmap = mColourWheel.getDrawingCache();

                if((int)event.getX()<bitmap.getWidth() && (int)event.getY()<bitmap.getHeight()){
                    int pixels = bitmap.getPixel((int)event.getX(), (int)event.getY());

                    mRGB.setALL(Color.red(pixels), Color.green(pixels), Color.blue(pixels));
                }

                setBarValue();

                mColorView.setBackgroundColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));
                mFrame.setElevationShadowColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));

                callBackFragment.notifyupdate();


            }
            return true;
        });
        mApplyButton.setOnTouchListener((v,event) ->{
            if(event.getAction()== MotionEvent.ACTION_DOWN || event.getAction()== MotionEvent.ACTION_BUTTON_PRESS) {
                mColorView.setBackgroundColor(Color.rgb(mRGB.getRED(), mRGB.getGREEN(), mRGB.getBLUE()));
                mFrame.setElevationShadowColor(Color.rgb(mRGB.getRED(), mRGB.getGREEN(), mRGB.getBLUE()));

                callBackFragment.notifyupdate();
            }
            return true;
        });

        mSaveButton.setOnTouchListener((v,event) ->{
            if(event.getAction()== MotionEvent.ACTION_DOWN || event.getAction()== MotionEvent.ACTION_BUTTON_PRESS) {
                newpreset();
            }
            return true;
        });


        return view;
    }




    private void setchangelister(){
        mRed.setOnSeekBarChangeListener(this);
        mGreen.setOnSeekBarChangeListener(this);
        mBlue.setOnSeekBarChangeListener(this);
        mBrightness.setOnSeekBarChangeListener(this);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setRGB(){
        //setting the colour preview
        mColorView.setBackgroundColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));
        mFrame.setElevationShadowColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));

        callBackFragment.notifyupdate();


    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch(seekBar.getId()){
            case R.id.seekBar:
                mRGB.setRED(progress);
                break;
            case R.id.seekBar2:
                mRGB.setGREEN(progress);
                break;
            case R.id.seekBar3:
                mRGB.setBLUE(progress);
                break;
            case R.id.seekBar4:
                brightness = progress;
                break;


        }
        mColorView.setBackgroundColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));
        mFrame.setElevationShadowColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));
        callBackFragment.notifyupdate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    private void addpresettolist(String name, int r, int g, int b){ //saves colour preset to SQLite
        mPresetList.add(new presets_items(name, r, g, b));

    }

    //for opening save preset dialog
    private void newpreset(){
        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.preset_dialog);



        EditText mInputName = dialog.findViewById(R.id.textinput);
        String presetname = mInputName.getText().toString();


        Button savebtn = dialog.findViewById(R.id.savebtn);
        Log.v("EditText", mInputName.getText().toString());
        savebtn.setOnClickListener(v -> {
            String presetname1 = mInputName.getText().toString();
            addpresettolist(presetname1,mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE());

            mSQL.addNewPreset(presetname1,mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE(),brightness);

            dialog.dismiss();
        });

        ImageView btnclose = dialog.findViewById(R.id.close_btn);
        btnclose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        loadSpinnerData();

    }

    //sets the seek bar values
    private void setBarValue(){
        mRed.setProgress(mRGB.getRED());
        mGreen.setProgress(mRGB.getGREEN());
        mBlue.setProgress(mRGB.getBLUE());
        mBrightness.setProgress(brightness);
    }


    //gets selected preset and extracts rgb values
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getSQLdata(String selected){
        SQL_base mSQL = new SQL_base(mContext);

        //query PRESET_COL for presets matching "selected"
        String queryString = "SELECT * FROM " + "presets" + " WHERE " + "preset" + " = ?";
        Cursor cursor = mSQL.getData(selected);
        cursor.moveToFirst();
        if(cursor.getCount()>0){
            mRGB.setRED(cursor.getInt(1));
            mRGB.setGREEN(cursor.getInt(2));
            mRGB.setBLUE(cursor.getInt(3));
            brightness = cursor.getInt(4);
        }
        setRGB();
        setBarValue();


    }


    // updating and loading spinner data
    private void loadSpinnerData() {
        SQL_base db = new SQL_base(mContext);
        List<String> labels = db.getPresetLabels();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, labels);

        mAdapter = new PresetsAdapter(mContext, labels);

        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data to spinner
        mPresets.setAdapter(dataAdapter);
    }
    public void setCallBackFragment(CallBackFragment callBackFragment){
        this.callBackFragment = callBackFragment;
    }


}
