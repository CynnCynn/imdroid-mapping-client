package com.imdroid.enums;


/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2019-01-03 12:02
 * @Modified By:
 */
public enum BluetoothEnum {
    //指令
    SCAN_COMPLETE("sc", "扫描完成"),
    RECEIVE_FILE("rf", "拿到可处理点云文件"),
    //回复
    I_AM_HERE("iah", "i am here"),
    TEAM_VIEW("tv", "远程调试用"),
    IMPORT_COMPLETE("103", "导入完成"),
    CALCULATION_COMPLETE("105", "计算完成"),
    //展示用
    TASK_DATA_QUOTA("tdq", "taskData/quota.do"),
    STATION_DATA_QUOTA("sdq", "stationData/quota.do"),
    WALL_DATA_QUOTA("wdq", "wallData/quota.do"),

    //错误码
    ERROR_NOT_FIND_DATA("201", "找不到扫描文件，请重新扫描"),
    ERROR_LOW_BATTERY_LEVEL("202", "扫描仪电量过低，请更换电池"),
    ERROR_CONNNECT("203", "扫描仪连接断开，请重新连接"),
    ERROR_WIFI("204", "连接扫描仪失败"),
    ERROR_DATA("205", "扫描数据异常，请重新扫描");

    private String type;
    private String meaning;

    BluetoothEnum(String type, String meaning) {
        this.type = type;
        this.meaning = meaning;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }}
