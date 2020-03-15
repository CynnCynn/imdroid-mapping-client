package com.imdroid.enums;

/**
 * @Description:指标类型枚举类
 * @Author: iceh
 * @Date: create in 2018-11-16 17:07
 * @Modified By:
 */
public enum QuotaEnum implements CodeEnum {
    LEVELNESS(101, "水平度"),
    VERTICAL(102, "内墙垂直度"),
    VERTICAL_BALCONY(108,"阳台花园墙垂直度"),
    FLATNESS(103, "内墙墙面平整度"),
    CEIL_FLATNESS(107,"天花平整度"),
    FLOOR_FLATNESS(106,"高精度结构地面平整度"),
    SQUARE(104, "方正度"),
    SECTION_SIZE(105, "截面尺寸"),

    DOOR_HOLE_WIDTH_SIZE(200, "门洞尺寸(宽)"),
    DOOR_HOLE_HEIGHT_SIZE(201, "门洞尺寸(高)"),
    WINDOW_HOLE_HEIGHT_SIZE(202, "窗口尺寸(高)"),
    WINDOW_HOLE_WIDTH_SIZE(203, "窗口尺寸(宽)"),

    INDOOR_HOLE_SIZE(204,"户内门洞尺寸偏差"),

    EXTERNAL_CORNER(301, "阳角"),
    INTERNAL_CORNER(302, "阴角"),
    CONCENTRIC(303, "窗边墙大小头"),
    BASEBOARD(304, "踢脚线平整度"),
    HOLE_EDGE_FALTNESS(305, "洞边平整度");

    private Integer code;
    private String meaning;

    QuotaEnum(Integer code, String meaning) {
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
