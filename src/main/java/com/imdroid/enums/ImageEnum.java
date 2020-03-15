package com.imdroid.enums;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2019-01-14 19:44
 * @Modified By:
 */
public enum ImageEnum implements CodeEnum {
    LEVELNESS(101, "水平度"),
    VERTICAL(102, "垂直度"),
    FLATNESS(103, "平整度"),
    ;

    private Integer code;
    private String meaning;

    ImageEnum(Integer code, String meaning) {
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
