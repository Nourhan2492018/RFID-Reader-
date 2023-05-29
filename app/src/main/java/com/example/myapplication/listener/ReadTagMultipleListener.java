package com.example.myapplication.listener;

import com.hprt.lib_rfid.model.RFIDEntity;

public interface ReadTagMultipleListener {
    void onMultiple(RFIDEntity data);
    void onTagEPC(RFIDEntity data);
}
