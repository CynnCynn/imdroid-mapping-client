package com.imdroid.pojo.entity;

public class DisplayData {
    public static final byte WAITING = 0x01;//等待中
    public static final byte START_IMPORTING = 0x02; //开始导入扫描数据
    public static final byte IMPORT_COMPLETE = 0x03;//导入扫描数据完成
    public static final byte EXPORT_COMPLETE = 0x04;//导出扫描数据完成
    public static final byte CALCULATION_COMPLETE = 0x05;//计算完成
    public static final byte ERROR_WIFI = 0x06;//连接扫描仪失败
    public static final byte ERROR_DATA = 0x07;//扫描数据异常，请重新扫描

    private byte roomNum;//当前房数

    private byte currentProgress;//当前站数

    private byte total;//总站数

    private byte state;//工作站状态

    private byte batteryLevel;//电量

    public DisplayData(int roomNum, int currentProgress, int total, int state, int batteryLevel) {
        this.roomNum = (byte) roomNum;
        this.currentProgress = (byte) currentProgress;
        this.total = (byte) total;
        this.state = (byte)(state);
        this.batteryLevel = (byte) batteryLevel;
    }


    public byte[] getDataBytes() {
        byte[] bytes = new byte[7];
        bytes[0] = bytes[6] = 0x7E;
        bytes[1] = this.roomNum;
        bytes[2] = this.currentProgress;
        bytes[3] = this.total;
        bytes[4] = this.state;
        bytes[5] = this.batteryLevel;
        return bytes;
    }

    public byte getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(byte roomNum) {
        this.roomNum = roomNum;
    }

    public byte getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(byte currentProgress) {
        this.currentProgress = currentProgress;
    }

    public byte getTotal() {
        return total;
    }

    public void setTotal(byte total) {
        this.total = total;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public byte getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = (byte) batteryLevel;
    }
}
