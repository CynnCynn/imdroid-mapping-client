package com.imdroid.enums;

/**
 * @Description: 墙面上点类型的枚举类
 * @Author: iceh
 * @Date: create in 2018-12-27 11:25
 * @Modified By:
 */
public enum PointTypeEnum implements CodeEnum {
    BASIS(0, "原始点"),
    QUALIFIED(1, "合格"),
    RAISE(2, "凸"),
    SAG(3, "凹"),
    DOOR_HOLE(4, "门洞"),
    WINDOW_HOLE(5, "窗洞"),
    OUT_POINT(6, "飘出点"),
    RAISE_VERTICAL(7, "垂直凸"),
    SAG_VERTICAL(8, "垂直凹"),
    RAISE_FLAT(9,"水平凸"),
    SAG_FLAT(10,"水平凹"),
    BOUND_POINT(11, "边界点");


    private Integer code;
    private String meaning;

    PointTypeEnum(Integer code, String meaning) {
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


