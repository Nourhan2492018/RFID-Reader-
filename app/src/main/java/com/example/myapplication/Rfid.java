package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.myapplication.listener.ReadTagMultipleListener;
import com.example.myapplication.listener.ReadTagSingleListener;
import com.example.myapplication.model.IDModel;
import com.hprt.lib_rfid.RFHelper;
import com.hprt.lib_rfid.listener.ConnectListener;
import com.hprt.lib_rfid.listener.RfidDataListener;
import com.hprt.lib_rfid.model.RFIDEntity;
import com.hprt.lib_rfid.utils.ByteUtils;
import com.hprt.lib_rfid.utils.RfidHelper;
import com.hprt.lib_rfid.utils.RxTimerUtil;
import com.hprt.lib_rfid.utils.SLR5100ProtocolUtil;
import com.hprt.lib_rfid.utils.ThreadExecutors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class Rfid {
     int all_nums = 0;
     public static Rfid INSTANCE;
    public static RFService.RFBinder binder;
     private String connectMassage=" aready connect";
    private long startTime = 0;
    private final long periodTime = 15;

    private volatile boolean inventorying = false; // false: not inventorying, true: inventorying
    private Timer timer = new Timer();
    private boolean keyDown = false;
     //private ReadTagSingleListener mReadTagSingleListener;
    //private ReadTagMultipleListener mReadTagMultipleListener;
     //public RFService.RFBinder binder;
     private  String Data;
     private ArrayList<String>list=new ArrayList<String>();
    ArrayList<IDModel> idModels= new ArrayList<>();
    ArrayList<IDModel> dataList= new ArrayList<>();
    private Rfid() {
        binder=new RFService.RFBinder();
    }

    public static Rfid getINSTANCE() {
        if(INSTANCE!=null)
        return INSTANCE;
        else
        {

            INSTANCE= new Rfid();
        return  INSTANCE;
        }
    }

    public static void Init(Context context)
    {
        getINSTANCE();
        RFHelper.INSTANCE.init(context);
        RFHelper.INSTANCE.enableRFID();
    }
    String connectedRFID()
    {
        if(!(RFHelper.INSTANCE.isConnect()))
        {
            //RFHelper.INSTANCE.

            ConnectListener connectListener=
                    new ConnectListener() {
                        @Override
                        public void onSuccess() {
                            ThreadExecutors.INSTANCE.getMainThread().execute(() -> {

                                connectMassage= "onSuccess Connected";
                                System.out.println("onSuccess Connected");
                            });
                        }

                        @Override
                        public void onFail(@NonNull Exception e) {
                            ThreadExecutors.INSTANCE.getMainThread().execute(() -> {
                                connectMassage= "onFail Connected";
                                System.out.println("onFail Connected");
                            });
                        }
                    };
            RFHelper.INSTANCE.connect("/dev/ttyS1", 115200,connectListener);
        }

        return connectMassage;
    }

    public String disconnectedRFID(){
        RFHelper.INSTANCE.disconnect();
        System.out.println("Disconnected");
        //RFHelper.INSTANCE.disableRFID();
        return "Disconnected";
    }


    public  String singleRead(int timeout, byte[] option, byte[] metadataflags,
            byte[] tagSingulationFields)
    {
         idModels.clear();
        dataList.clear();
        if(!RFHelper.INSTANCE.isConnect())
            return "is Connect False";
          RfidDataListener mReadTagSingleListener1= new RfidDataListener() {
              @SuppressLint("CheckResult")
              @Override
              public void onRfid(RFIDEntity rfidEntity) {
                  //System.out.println("rfidEntity: "+rfidEntity.toString());
                  resolveSingleData(rfidEntity)
                          .map(idmodel -> {
                              RFHelper.INSTANCE.controlTwinkle();
                               //re.set(idmodel.getEPCID());
                              System.out.println("getEPCID: "+idmodel.getEPCID());
                              System.out.println("getRSSI: "+idmodel.getRSSI());
                              System.out.println("getCRC:"+idmodel.getCRC());
                              System.out.println("getRFU:"+idmodel.getRFU());
                              System.out.println("getAntennaID:"+idmodel.getAntennaID());

                              return idmodel;
                          })
                          .subscribeOn(Schedulers.computation())
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribe(
                                  idmodel  -> {
                                      if (idModels.contains(idmodel)) {
                                          for (int index = 0; index < idModels.size(); index++) {
                                              IDModel id = idModels.get(index);
                                              if (id.getEPCID().equals(idmodel.getEPCID())) {
                                                  id.setRSSI(idmodel.getRSSI());
                                                  id.setReadCount(id.getReadCount() + idmodel.getReadCount());
                                                  dataList.add(id);
                                                  System.out.println("getEPCID: "+idmodel.getEPCID());
                                                  System.out.println("getRSSI: "+idmodel.getRSSI());
                                                  System.out.println("getCRC:"+idmodel.getCRC());
                                                  System.out.println("getRFU:"+idmodel.getRFU());
                                                  System.out.println("getAntennaID:"+idmodel.getAntennaID());
                                              }
                                          }
                                      } else {
                                          idModels.add(idmodel);
                                          System.out.println("idModels:"+idModels.size());

                                      }

                                  }
                          );

              }
          };

         RFHelper.INSTANCE.singleInventory(
                    timeout,
                    option,
                    metadataflags,
                    tagSingulationFields);
            RFHelper.INSTANCE.getRFIDData(mReadTagSingleListener1);
            RFHelper.INSTANCE.getTagInfo();

        System.out.println("Single size : "+ dataList.size());
      return String.valueOf(idModels.size());
    }

public  void reading ()
{
    if(!RFHelper.INSTANCE.isConnect())
        return ;
    RfidDataListener mReadTagSingleListener1= new RfidDataListener() {
        @SuppressLint("CheckResult")
        @Override
        public void onRfid(RFIDEntity rfidEntity) {
            System.out.println("rfidEntity: "+rfidEntity.toString());
            resolveSingleData(rfidEntity)
                    .map(idmodel -> {
                        RFHelper.INSTANCE.controlTwinkle();
                        //re.set(idmodel.getEPCID());
                        System.out.println("getEPCID: "+idmodel.getEPCID());
                        System.out.println("getRSSI: "+idmodel.getRSSI());
                        System.out.println("getCRC:"+idmodel.getCRC());
                        System.out.println("getRFU:"+idmodel.getRFU());
                        System.out.println("getAntennaID:"+idmodel.getAntennaID());

                        return idmodel;
                    })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            idmodel  -> {
                                if (idModels.contains(idmodel)) {
                                    for (int index = 0; index < idModels.size(); index++) {
                                        IDModel id = idModels.get(index);
                                        if (id.getEPCID().equals(idmodel.getEPCID())) {
                                            id.setRSSI(idmodel.getRSSI());
                                            id.setReadCount(id.getReadCount() + idmodel.getReadCount());
                                            //
                                            System.out.println("getEPCID: "+idmodel.getEPCID());
                                            System.out.println("getRSSI: "+idmodel.getRSSI());
                                            System.out.println("getCRC:"+idmodel.getCRC());
                                            System.out.println("getRFU:"+idmodel.getRFU());
                                            System.out.println("getAntennaID:"+idmodel.getAntennaID());
                                        }
                                    }
                                } else {
                                    idModels.add(idmodel);
                                    System.out.println("idModels:"+idModels.size());

                                }

                            }
                    );

        }
    };
    RFHelper.INSTANCE.multiInventory();
    RFHelper.INSTANCE.getRFIDData(mReadTagSingleListener1);
    RFHelper.INSTANCE.getTagInfo();

}
    public  String ReadCycle()
    {
        idModels.clear();
        dataList.clear();
        if(!RFHelper.INSTANCE.isConnect()) {
            System.out.println("is Connect False");
            return "is Connect False";
        }
        RfidDataListener mReadTagMultipleListener1 = new RfidDataListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onRfid(RFIDEntity rfidEntity) {
                resolveEpcData(rfidEntity)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(list -> {
                            for (IDModel idmodel : list) {
                                if (idModels.contains(idmodel)) {
                                    for (int index = 0; index < idModels.size(); index++) {
                                        IDModel id = idModels.get(index);
                                        if (id.getEPCID().equals(idmodel.getEPCID())) {
                                            id.setRSSI(idmodel.getRSSI());
                                            id.setReadCount(id.getReadCount() + idmodel.getReadCount());
                                            System.out.println("=========================");
                                            System.out.println(" Item "+index);
                                            System.out.println("-------------------");
                                            System.out.println("getEPCID : "+id.getEPCID());
                                            System.out.println("getCRC : "+id.getCRC());
                                            System.out.println("getRSSI : "+id.getRSSI());
                                            System.out.println("=========================");
                                        }
                                    }
                                } else {
                                    RFHelper.INSTANCE.controlTwinkle();
                                    idModels.add(idmodel);
                                    System.out.println("=========================");
                                    System.out.println("getEPCID : "+idmodel.getEPCID());
                                    System.out.println("getCRC : "+idmodel.getCRC());
                                    System.out.println("getRSSI : "+idmodel.getRSSI());
                                    System.out.println("=========================");
                                }
                                if (inventorying) {
                                    RFHelper.INSTANCE.controlTwinkle();
                                }
                            }
                            //总计标签数
                            //总次数
                            int nums = 0;
                            for (IDModel model : idModels) {
                                nums += model.getReadCount();
                            }
                        }, throwable -> {
                        }, () -> {
                        });
            }
        };

        RFHelper.INSTANCE.multiInventory();
        RFHelper.INSTANCE.getRFIDData(mReadTagMultipleListener1);
        RFHelper.INSTANCE.getTagInfo();

        System.out.println("ReadCycle size : "+ Rfid.INSTANCE.idModels.size());
        return String.valueOf(idModels.size());
    }

void singleInventory() {

        timer = new Timer();
        timer.schedule(new MyTimeTask(), 20, periodTime);
        binder.singleInventory(
                6000,
                new byte[]{0x10},
                new byte[]{0x00, (byte) 0xFF},
                new byte[]{},
                mReadTagSingleListener
        );
    }
public  String MultiRead()
    {
        if(!RFHelper.INSTANCE.isConnect())
            return "isConnect False";
        inventory();
    return "mulitu ";
    }
private void inventory() {
//        if (!checkRFIDReady()) {
//            return;
//        }
        idModels.clear();
        if (!inventorying) {
            inventorying = true;
            inventory5100();
        }
    }
private void inventory5100() {
        inventorying = true;
        Timer timer = new Timer();
        timer.schedule(new MyTimeTask(), 0,periodTime);
        binder.multiInventory(mReadTagMultipleListener);
        System.out.println("size : "+idModels.size());
    }


    public void connect() {

        binder.connect("/dev/ttyS1", 115200, new ConnectListener() {
            @Override
            public void onSuccess() {

                ThreadExecutors.INSTANCE.getMainThread().execute(() -> {
                    System.out.println("Connected");

                });
            }

            @Override
            public void onFail(Exception e) {
                RxTimerUtil.INSTANCE.cancel();
                ThreadExecutors.INSTANCE.getMainThread().execute(() -> {
                    System.out.println("Connection Failed");
                });
            }
        });
    }

    public void disconnect() {
        binder.disconnect();
        System.out.println("Disconnect");
    }



    public void handleLost(String str) {
        binder.stopFastInventory();
        binder.disconnect();


    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
    public void stopInventory() {
        ThreadExecutors.INSTANCE.getMainThread().execute(new Runnable() {
            @Override
            public void run() {
                // print
            }
        });

        inventorying = false;
        if (timer != null) {
            timer.cancel();
        }
    }
    public boolean checkRFIDReady() {
        if ((RFHelper.INSTANCE.getConnectType()!=1)) {
            ToastUtils.showShort("Not connected");
            return false;
        }
        if (RFHelper.INSTANCE.getConnectType() == 0) {
            String result = FileIOUtils.readFile2String("/sys/devices/platform/10010000.kp/TSTBASE");
            if (TextUtils.isEmpty(result)) {
                ToastUtils.showShort("Device not detected");
                return false;
            }
            if (result.equals("1") || result.equals("1\n")) {
                return true;
            }
            return false;
        } else {
            return true;
        }
    }


    /**定时任务*/
    class MyTimeTask extends TimerTask {
        @Override
        public void run() {
            ThreadExecutors.INSTANCE.getMainThread().execute(() -> {
                startTime += periodTime;
            });
        }
    }

    public void showErrorStatus(String str) {
        if (timer != null) {
            timer.cancel();
        }
    }
    public void showTransError(String str) {
        if (timer != null) {
            timer.cancel();
        }
    }

//
Observable<IDModel> resolveSingleData(RFIDEntity rfidEntity) {
    return Observable.create(new ObservableOnSubscribe<IDModel>() {
        @Override
        public void subscribe(ObservableEmitter<IDModel> emitter) throws Exception {
            if (ByteUtils.Companion.bytes2ToInt_h(rfidEntity.Status, 0) == 0) {
                if(rfidEntity.getData().length>=32){
                    IDModel idmodel = singleToIDModel(rfidEntity);
                    emitter.onNext(idmodel);
                    emitter.onComplete();
                }
            } else {
                emitter.onError(new Throwable(SLR5100ProtocolUtil.Companion.getStatusInfo(ByteUtils.Companion.bytes2ToInt_h(rfidEntity.Status, 0))));
            }
        }
    });
}

private IDModel singleToIDModel(RFIDEntity rfidEntity) {
    System.out.println("singleToIDModel:"+rfidEntity.getData().length);
        int readcount = rfidEntity.getData()[3] & 0xFF;
        int rssi = rfidEntity.getData()[4];//4
        int antennaId = rfidEntity.getData()[5] & 0xFF;
        byte[] frequency = new byte[] {
                rfidEntity.getData()[6],
                rfidEntity.getData()[7],
                rfidEntity.getData()[8]
        };
        int timestamp = ByteUtils.Companion.bytes4ToInt_h(rfidEntity.getData(), 9);
        byte[] rfu = new byte[] {
                rfidEntity.getData()[13],
                rfidEntity.getData()[14]
        };
        byte protocolId = rfidEntity.getData()[15];
        byte[] datalength = new byte[] {
                rfidEntity.getData()[16],
                rfidEntity.getData()[17]
        };
        byte[] tagcrc = new byte[] {
                rfidEntity.getData()[rfidEntity.getData().length - 2],
                rfidEntity.getData()[rfidEntity.getData().length - 1]
        };
    byte[] epcid_arr = new byte[0];
    //todo
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
        epcid_arr = Arrays.copyOfRange(rfidEntity.getData(), 18, rfidEntity.getData().length - 2);
    }
    String epcid = ByteUtils.Companion.bytetohex(epcid_arr).replace(" ", "");
        IDModel idModel = new IDModel(
                readcount,
                rssi,
                antennaId,
                frequency,
                timestamp,
                rfu,
                protocolId,
                datalength,
                epcid,
                new byte[] {}
        );
        return idModel;
    }
private IDModel convertMultiLabelBytesToIDModel(byte[] it) {
        int readcount = it[0] & 0xFF;
        int rssi = it[1];
        int antennaId = it[2] & 0xFF;
        byte[] frequency = new byte[3];
        System.arraycopy(it, 3, frequency, 0, frequency.length);
        int timestamp = ByteUtils.Companion.bytes4ToInt_h(it, 6);
        byte[] rfu = new byte[2];
        System.arraycopy(it, 10, rfu, 0, rfu.length);
        byte protocolId = it[12];
        byte[] datalength = new byte[2];
        System.arraycopy(it, 15, datalength, 0, datalength.length);
        int epcLength = ByteUtils.Companion.bytes2ToInt_h(datalength, 0) / 8;
        byte[] pcword = new byte[2];
        System.arraycopy(it, 17, pcword, 0, pcword.length);
        byte[] epcid_arr = new byte[epcLength - 4];
        System.arraycopy(it, 19, epcid_arr, 0, epcid_arr.length);
        String epcid = ByteUtils.Companion.bytetohex(epcid_arr).replace(" ", "");
        byte[] tagcrc = new byte[2];
        System.arraycopy(it, it.length - 2, tagcrc, 0, 2);
        IDModel idModel = new IDModel(readcount, rssi, antennaId, frequency, timestamp, rfu, protocolId, datalength, epcid, new byte[]{});
        return idModel;
    }
public Observable<List<IDModel>> resolveEpcData(RFIDEntity rfidEntity) {
        return Observable.create(emitter -> {
            if (ByteUtils.Companion.bytes2ToInt_h(rfidEntity.getStatus(), 0) == 0) {
                int labelCount = rfidEntity.getData()[3] & 0xFF; //标签数量
                if (labelCount > 0) {
                    ArrayList<byte[]> labelArray = new ArrayList<>(labelCount);
                    byte[] labelData = new byte[0];
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
                        labelData = Arrays.copyOfRange(rfidEntity.getData(), 4, rfidEntity.getData().length);
                    }
                    int lenFlag = 0;
                    for (int i = 0; i < labelCount; i++) {
                        int epcLength = ByteUtils.Companion.bytes2ToInt_h(labelData, lenFlag + 15) / 8;
                        byte[] tempLabel = new byte[17 + epcLength]; //读取到单个标签数据长度
                        if (labelData.length < 17 + epcLength) {
                            emitter.onError(new Throwable("err epc length=== " + epcLength + " labelDataSize = " + labelData.length));
                        }
                        System.arraycopy(labelData, lenFlag, tempLabel, 0, tempLabel.length);
                        labelArray.add(tempLabel);
                        lenFlag += tempLabel.length;
                    }
                    ArrayList<IDModel> list = new ArrayList<>();
                    for (byte[] arr : labelArray) {
                        IDModel idmodel = convertMultiLabelBytesToIDModel(arr);
                        list.add(idmodel);
                    }
                    if (labelCount < 7) { //一次最多可以读7个标签
                        if (inventorying) { //读取完数据继续发盘点
                            binder.multiInventory(mReadTagMultipleListener);
                        }
                    } else {
                         binder.getTagInfo();
                    }
                    emitter.onNext(list);
                    emitter.onComplete();
                } else {
                    if (inventorying) {
                        binder.multiInventory(mReadTagMultipleListener);
                    }
                }
            } else {
                emitter.onError(new Throwable(SLR5100ProtocolUtil.Companion.getStatusInfo(ByteUtils.Companion.bytes2ToInt_h(rfidEntity.getStatus(), 0))));
            }
        });
    }
private ReadTagMultipleListener mReadTagMultipleListener = new ReadTagMultipleListener() {
        @Override
        public void onMultiple(RFIDEntity data) {
            resolveMultipleData(data)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(nums -> {
                        //本次标签数
                        RFHelper.INSTANCE.getTagInfo();

                    }, throwable -> {
                        ToastUtils.showShort(throwable.getMessage());
                        LogUtils.file(throwable.getMessage());
                    }, () -> {

                    });
        }

        @Override
        public void onTagEPC(RFIDEntity data) {
            resolveEpcData(data)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(list -> {
                        for (IDModel idmodel : list) {
                            if (idModels.contains(idmodel)) {
                                for (int index = 0; index < idModels.size(); index++) {
                                    IDModel id = idModels.get(index);
                                    if (id.getEPCID().equals(idmodel.getEPCID())) {
                                        id.setRSSI(idmodel.getRSSI());
                                        id.setReadCount(id.getReadCount() + idmodel.getReadCount());
                                        System.out.println("item "+index+" : getEPCID"+id.getEPCID());
                                        System.out.println("item "+index+" : getCRC"+id.getCRC());
                                        System.out.println("item "+index+" : getRSSI"+id.getRSSI());
                                    }
                                }
                            } else {
                                idModels.add(idmodel);
                                System.out.println("item  : getEPCID"+idmodel.getEPCID());
                                System.out.println("item  getCRC"+idmodel.getCRC());
                                System.out.println("item  : getRSSI"+idmodel.getRSSI());
                            }
                            if (inventorying) {
                                RFHelper.INSTANCE.controlTwinkle();
                            }
                        }
                        //总计标签数
                        //总次数
                        int nums = 0;
                        for (IDModel model : idModels) {
                            nums += model.getReadCount();
                        }
                    }, throwable -> {
                    }, () -> {
                    });
        }
    };
public Observable<Integer> resolveMultipleData(RFIDEntity rfidEntity) {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) {
                if (ByteUtils.Companion.bytes2ToInt_h(rfidEntity.getStatus(), 0) == 0) {
                    int found = rfidEntity.getData()[3] & 0xFF;
                    if (found > 0) {
                        emitter.onNext(found);
                    } else {
                        if (inventorying) {
                            RFHelper.INSTANCE.multiInventory();
                        }
                    }
                    emitter.onComplete();
                } else {
                    if (inventorying) {
                        binder.multiInventory(mReadTagMultipleListener);
                    }
                }
            }
        });
    }

    ReadTagSingleListener mReadTagSingleListener = new ReadTagSingleListener() {
        @SuppressLint("CheckResult")
        @Override
        public void onData(RFIDEntity data) {
            resolveSingleData(data)
                    .map(idmodel -> {
                        RFHelper.INSTANCE.controlTwinkle();
                        return idmodel;
                    })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            idmodel -> {
                                if (idModels.contains(idmodel)) {
                                    for (int index = 0; index < idModels.size(); index++) {
                                        IDModel id = idModels.get(index);
                                        if (id.getEPCID().equals(idmodel.getEPCID())) {
                                            id.setRSSI(idmodel.getRSSI());
                                            id.setReadCount(id.getReadCount() + idmodel.getReadCount());
                                        }
                                    }
                                } else {
                                    idModels.add(idmodel);
                                }

                            }


                    );
        }


    };


}


