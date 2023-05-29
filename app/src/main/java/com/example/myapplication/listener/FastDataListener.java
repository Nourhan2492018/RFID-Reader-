package com.example.myapplication.listener;

import com.hprt.lib_rfid.model.RFIDEntity;

public interface FastDataListener {
    void onStart( Boolean result);
    void onData(RFIDEntity data);
    void onStop(Boolean result);
}