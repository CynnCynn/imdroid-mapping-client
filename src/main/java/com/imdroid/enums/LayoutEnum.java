package com.imdroid.enums;

/**
 * @Description: 户型布局枚举类
 * @Author: iceh
 * @Date: create in 2018-11-18 10:46
 * @Modified By:
 */
public enum LayoutEnum implements CodeEnum {
    THREE_L_D_K(311, "三房一厅一厨"),
    TWO_L_D_K(211, "两房一厅一厨"),
    FOUR_L_D_K(411, "四房一厅一厨");

    private Integer code;
    private String meaning;

    LayoutEnum(Integer code, String meaning) {
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
