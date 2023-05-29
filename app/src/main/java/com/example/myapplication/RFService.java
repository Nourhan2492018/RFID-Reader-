package com.example.myapplication;
import static com.hprt.lib_rfid.ext.LogExtKt.logFile;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.myapplication.listener.ReadTagMultipleListener;
import com.example.myapplication.listener.ReadTagSingleListener;
import com.hprt.lib_rfid.RFHelper;
import com.hprt.lib_rfid.listener.ConnectListener;
import com.hprt.lib_rfid.listener.RfidDataListener;
import com.hprt.lib_rfid.listener.TransStatusListener;
import com.hprt.lib_rfid.model.RFIDEntity;
import com.hprt.lib_rfid.utils.ByteUtils;
import com.hprt.lib_rfid.utils.SLR5100ProtocolUtil;
import com.hprt.lib_rfid.utils.ThreadExecutors;

public class RFService extends Service {
    private static final RFBinder binder = new RFBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
        super.onDestroy();
    }

static class RFBinder extends Binder {
        private ReadTagSingleListener mReadTagSingleListener;
        private ReadTagMultipleListener mReadTagMultipleListener;

private volatile boolean inventoryStatus = false;
public void singleInventory(int timeout, byte[] option, byte[] metadataflags, byte[] tagSingulationFields, ReadTagSingleListener listener) {
            this.mReadTagSingleListener = listener;
            RFHelper.INSTANCE.singleInventory(timeout, option, metadataflags, tagSingulationFields);
}
public void multiInventory(ReadTagMultipleListener listener) {
            this.mReadTagMultipleListener = listener;
            RFHelper.INSTANCE.multiInventory();
        }
public void getTagInfo() {
            RFHelper.INSTANCE.getTagInfo();
        }
public void controlTwinkle() {
            RFHelper.INSTANCE.controlTwinkle();
        }
void disconnect() {
            RFHelper.INSTANCE.disconnect();
    }
public void connect(String mac, int i, ConnectListener listener) {

            ThreadExecutors.INSTANCE.getTransThread().execute(new Runnable() {
                @Override
                public void run() {
                    RFHelper.INSTANCE.connect("/dev/ttyS1", 115200,listener);
                    //RFHelper.INSTANCE.getRFIDData(mRfidDataListener);
                    //listenTransStatus();
                }
            });
    }
void stopFastInventory() {
            ThreadExecutors.INSTANCE.getTransThread().execute(() -> {
                RFHelper.INSTANCE.stopFastInventory();
                inventoryStatus = false;
            });
        }}
private static RfidDataListener mRfidDataListener = new RfidDataListener() {
        @Override
        public void onRfid(RFIDEntity it) {
            switch (it.getCommand()) {
                case 0x21:
                    logFile("单标签读取0x21--->" + it);
                    if (binder.mReadTagSingleListener != null) {
                        binder.mReadTagSingleListener.onData(it);
                    }
                    break;
                case 0x22:
                    logFile("多标签读取0x22--->" + it);
                    if (binder.mReadTagMultipleListener != null) {
                        binder.mReadTagMultipleListener.onMultiple(it);
                    }
                    break;
                case 0x23:
                case 0x24:
                    logFile("写标签--->" + it);
                    if (ByteUtils.Companion.bytes2ToInt_h(it.getStatus(), 0) == 0) {
                        showToast("写入成功");
                    } else {
                        showToast(SLR5100ProtocolUtil.Companion.getStatusInfo(ByteUtils.Companion.bytes2ToInt_h(it.getStatus(), 0))
                                + ByteUtils.Companion.bytetohex(it.getStatus()));
                        }
                    break;
                case 0x25:
                    logFile("LOCK_TAG 0x25--->" + it);
                    if (ByteUtils.Companion.bytes2ToInt_h(it.getStatus(), 0) == 0) {
                        showToast("锁定成功");
                    } else {
                        showToast(SLR5100ProtocolUtil.Companion.getStatusInfo(ByteUtils.Companion.bytes2ToInt_h(it.getStatus(), 0)) + ByteUtils.Companion.bytetohex(it.getStatus()));
                    }
                    break;
                case 0x26:
                    logFile("KILL_TAG 0x26--->" + it);
                    if (ByteUtils.Companion.bytes2ToInt_h(it.getStatus(), 0) == 0) {
                        showToast("销毁成功");
                    } else {
                        showToast(SLR5100ProtocolUtil.Companion.getStatusInfo(ByteUtils.Companion.bytes2ToInt_h(it.getStatus(), 0)) + ByteUtils.Companion.bytetohex(it.getStatus()));
                    }
                    break;


            }
        }
    };
private static void showToast(String msg) {
        ThreadExecutors.INSTANCE.getMainThread().execute(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showShort(msg);
            }
        });
    }
private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                RF2Lost("BluetoothDevice ACTION_ACL_DISCONNECTED");
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                    RF2Lost("STATE_TURNING_OFF");
                }
            }
        }
    };
private void RF2Lost(String reason) {
        LogUtils.file("device lost---------------------" + reason);
    }

}