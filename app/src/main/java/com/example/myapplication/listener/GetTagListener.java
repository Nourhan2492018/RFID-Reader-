package com.example.myapplication.listener;

import com.hprt.lib_rfid.model.RFIDEntity;

public interface GetTagListener {
    void onData( RFIDEntity rfidEntity);
}
