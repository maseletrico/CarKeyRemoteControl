package com.maseletrico.remotecontrolkey;

import android.widget.ImageView;

/**
 * Created by root on 14/02/18.
 */

public class BluetoothInfo {

    private String btName, btAddress;
    private ImageView btHardwareType;

    public BluetoothInfo(){

    }

    public BluetoothInfo(String btName, String btAddress, ImageView btHardwareType) {
        this.btName = btName;
        this.btAddress = btAddress;
        this.btHardwareType = btHardwareType;
    }

    public String getBtName() {
        return btName;
    }

    public void setBtName(String btName) {
        this.btName = btName;
    }

    public String getBtAddress() {
        return btAddress;
    }

    public void setBtAddress(String btAddress) {
        this.btAddress = btAddress;
    }

    public ImageView getBtHardwareType() {
        return btHardwareType;
    }

    public void setBtHardwareType(ImageView btHardwareType) {
        this.btHardwareType = btHardwareType;
    }
}
