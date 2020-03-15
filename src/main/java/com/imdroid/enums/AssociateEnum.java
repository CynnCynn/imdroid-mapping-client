package com.imdroid.enums;

/**
 * @Description: 指标关联类型枚举类
 * @Author: iceh
 * @Date: create in 2018-11-16 18:21
 * @Modified By:
 */
public enum AssociateEnum implements CodeEnum {
    TASK_DATA(1, "任务数据"),
    STATION_DATA(2, "测站数据"),
    WALL_DATA(3, "墙面数据");

    private Integer code;
    private String meaning;

    AssociateEnum(Integer code, String meaning) {
        this.code = code;
        this.meaning = meaning;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }
}
