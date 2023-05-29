package com.example.myapplication.listener;

import com.hprt.lib_rfid.listener.RfidDataListener;
import com.hprt.lib_rfid.model.RFIDEntity;

public interface ReadTagSingleListener  {
    void onData(RFIDEntity data);
}
