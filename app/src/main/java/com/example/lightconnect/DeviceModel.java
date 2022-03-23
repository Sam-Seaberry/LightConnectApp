package com.example.lightconnect;

import java.util.ArrayList;
import java.util.List;

public class DeviceModel {
    private String name;
    private int viewColor, barValue;
    private boolean switchValue;

    public DeviceModel(String name, int viewColor, int barValue, boolean switchValue){
        this.name = name;
        this.viewColor= viewColor;
        this.barValue= barValue;
        this.switchValue= switchValue;
    }
    public DeviceModel(){}

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    public int getViewColor() {
        return viewColor;
    }

    public void setViewColor(int viewColor) {
        this.viewColor = viewColor;
    }

    public int getBarValue() {
        return barValue;
    }

    public void setBarValue(int barValue) {
        this.barValue = barValue;
    }

    public boolean getSwitchValue() {
        return switchValue;
    }

    public void setSwitchValue(boolean switchValue) {
        this.switchValue = switchValue;
    }


}
