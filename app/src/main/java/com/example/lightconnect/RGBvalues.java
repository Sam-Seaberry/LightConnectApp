package com.example.lightconnect;

import androidx.lifecycle.MutableLiveData;

public class RGBvalues {
    private static int RED=0;
    private static int GREEN=0;
    private static int BLUE=0;
    private static byte[] conversion= new byte[1];

    public int getRED(){
        return RED;
    }
    public int getGREEN(){
        return GREEN;
    }
    public int getBLUE(){
        return BLUE;
    }

    public void setRED(int red){
        RED = red;
    }
    public void setGREEN(int green){
        GREEN = green;
    }
    public void setBLUE(int blue){
        BLUE = blue;
    }
    public void setALL(int red, int green, int blue){
        RED = red;
        BLUE = blue;
        GREEN = green;
    }

    public static byte[] tobyte(int a){
        conversion[0] = (byte)(a & 0xFF);
        return conversion;
    }
}
