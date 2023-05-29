package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hprt.lib_rfid.RFHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button connectBtn,disConnectedBtn, scanSingleBtn,scanMultiBtn;
    TextView checkConnected, tagText,tagDataText;
    ArrayList<String> list =  new ArrayList<String>();
    Rfid rfid;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        disConnectedBtn=findViewById(R.id.btn_disconnected);
        connectBtn= findViewById(R.id.btn_connect);
        checkConnected=findViewById(R.id.txt_check);
        scanSingleBtn =findViewById(R.id.btn_scann);
        scanMultiBtn =findViewById(R.id.multi_btn);
        tagText=findViewById(R.id.txt_tag);
        tagDataText=findViewById(R.id.txt_tag_data);
        Rfid.Init(this);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Rfid.INSTANCE.connect();
               checkConnected.setText(Rfid.INSTANCE.connectedRFID());
            }
        });
        disConnectedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Rfid.INSTANCE.disconnect();
                checkConnected.setText(Rfid.INSTANCE.disconnectedRFID());
            }
        });
        //RFHelper.INSTANCE.multiInventory();
        scanSingleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //	todo nour 0x06
                 //Rfid.INSTANCE.singleInventory();
                byte[]option={0x10};
                byte[]metadataflags={0x00, (byte)0xFF};
                byte[]tagSingulationFields={};
                //Rfid.INSTANCE.singleInventory();
                // Rfid.INSTANCE.singleRead(2000, option, metadataflags,tagSingulationFields);
               String d=Rfid.INSTANCE.singleRead(5000, option,
                      metadataflags,tagSingulationFields);
                tagText.setText(d);
                tagDataText.setText(d);
            }


        });


        scanMultiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Rfid.INSTANCE.ReadCycle();
                //Rfid.INSTANCE.MultiRead();
               // System.out.println("ReadCycle size : "+ Rfid.INSTANCE.idModels.size());
            }
        });

    }

}