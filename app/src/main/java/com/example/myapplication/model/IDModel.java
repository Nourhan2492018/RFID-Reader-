package com.example.myapplication.model;

import com.hprt.lib_rfid.utils.ByteUtils;

import java.util.Arrays;

/**
 * description：
 */
public class IDModel {
    private int ReadCount; // 标签被盘存到的次数
    private int RSSI;
    private int antennaID; // 盘存到标签的天线 ID
    private byte[] Frequency; // 盘存到标签时的频率
    private int Timestamp; // 执行该指令到标签首次被盘存到时的时间，单位毫秒
    private byte[] RFU; // 预留数据
    private byte ProtocolID; // 标签协议
    private byte[] TagDataLength; // 标签数据长度
    private String EPCID; // 标签 EPC 号
    private byte[] CRC; // 标签 CRC

    public IDModel(int ReadCount, int RSSI, int antennaID, byte[] Frequency, int Timestamp, byte[] RFU,
                   byte ProtocolID, byte[] TagDataLength, String EPCID, byte[] CRC) {
        this.ReadCount = ReadCount;
        this.RSSI = RSSI;
        this.antennaID = antennaID;
        this.Frequency = Frequency;
        this.Timestamp = Timestamp;
        this.RFU = RFU;
        this.ProtocolID = ProtocolID;
        this.TagDataLength = TagDataLength;
        this.EPCID = EPCID;
        this.CRC = CRC;
    }

    public int getReadCount() {
        return ReadCount;
    }

    public void setReadCount(int ReadCount) {
        this.ReadCount = ReadCount;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public int getAntennaID() {
        return antennaID;
    }

    public void setAntennaID(int antennaID) {
        this.antennaID = antennaID;
    }

    public byte[] getFrequency() {
        return Frequency;
    }

    public void setFrequency(byte[] Frequency) {
        this.Frequency = Frequency;
    }

    public int getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(int Timestamp) {
        this.Timestamp = Timestamp;
    }

    public byte[] getRFU() {
        return RFU;
    }

    public void setRFU(byte[] RFU) {
        this.RFU = RFU;
    }

    public byte getProtocolID() {
        return ProtocolID;
    }

    public void setProtocolID(byte ProtocolID) {
        this.ProtocolID = ProtocolID;
    }

    public byte[] getTagDataLength() {
        return TagDataLength;
    }

    public void setTagDataLength(byte[] TagDataLength) {
        this.TagDataLength = TagDataLength;
    }

    public String getEPCID() {
        return EPCID;
    }

    public void setEPCID(String EPCID) {
        this.EPCID = EPCID;
    }

    public byte[] getCRC() {
        return CRC;
    }

    public void setCRC(byte[] CRC) {
        this.CRC = CRC;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IDModel)) {
            return false;
        }
        IDModel otherModel = (IDModel) other;
        if (EPCID.equals(otherModel.getEPCID())) {
            return true;
        }
        return super.equals(other);
    }

    @Override
    public String toString() {
        return "IDModel(ReadCount=" + ReadCount +
                ", RSSI=" + RSSI +
                ", antennaID=" + antennaID +
                ", Frequency=" + ByteUtils.Companion.bytetohex(Frequency) +
                ", Timestamp=" + Timestamp +
                ", RFU=" + ByteUtils.Companion.bytetohex(RFU) +
                ", ProtocolID=" + ProtocolID +
                ", TagDataLength=" + ByteUtils.Companion.bytetohex(TagDataLength) +
                ", EPCID='" + EPCID + '\'' +
                ", CRC=" + ByteUtils.Companion.bytetohex(CRC) +
                ")";
    }
}