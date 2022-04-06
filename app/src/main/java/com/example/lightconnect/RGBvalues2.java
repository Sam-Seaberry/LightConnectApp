package com.example.lightconnect;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class RGBvalues2 {
    private static MutableLiveData<Integer> RED= new MutableLiveData<Integer>();
    private static MutableLiveData<Integer> GREEN=new MutableLiveData<Integer>();
    private static MutableLiveData<Integer> BLUE=new MutableLiveData<Integer>();
    private static MutableLiveData<Integer> BRIGHT=new MutableLiveData<Integer>();

    private static MutableLiveData<Boolean> connected=new MutableLiveData<Boolean>();

    private static byte[] conversion= new byte[1];

    public LiveData<Integer> getRED(){
        return RED;
    }


    public LiveData<Integer> getGREEN(){
        return GREEN;
    }
    public LiveData<Integer> getBLUE(){
        return BLUE;
    }
    public LiveData<Integer> getBRIGHT(){
        return BRIGHT;
    }

    public LiveData<Boolean> getConnec(){
        return connected;
    }


    public void setRED(int red){
        RED.setValue(red);
    }

    public void setGREEN(int green){
        GREEN.setValue(green);
    }
    public void setBLUE(int blue){
        BLUE.setValue(blue);
    }
    public void setBRIGHT(int bright){
        BRIGHT.setValue(bright);
    }
    public void setConnect(boolean connect){
        connected.setValue(connect);
    }
    public void setALL(int red, int green, int blue){
        RED.setValue(red);
        BLUE.setValue(green);
        GREEN.setValue(blue);
    }

}
