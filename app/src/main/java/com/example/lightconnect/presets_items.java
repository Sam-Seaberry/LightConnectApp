package com.example.lightconnect;

public class presets_items {
    private final String mPresetname;
    private final int red;
    private final int blue;
    private final int green;

    public presets_items(String presetname, int r, int g, int b){
        mPresetname = presetname;
        red = r;
        blue =b;
        green =g;

    }
    public String getmPresetname(){
        return mPresetname;
    }
    public int getRed(){
        return red;
    }
    public int getGreen(){
        return green;
    }
    public int getBlue(){
        return blue;
    }
}
