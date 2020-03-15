package com.imdroid.pojo.entity;


import lombok.Data;

import java.io.Serializable;

/**
 * @Description:墙面数据表
 * @Author: iceh
 * @Date: create in 2018-11-12 16:02
 * @Modified By:
 */
@Data
public class WallData implements Serializable {
    private Long pk;
    /*
     * 总点数
     * */
    private Long totalPoints;
    /**
     * 坐标轴
     */
    private String axis;
    /**
     * 坐标轴正负
     */
    private String coordinate;
    /**
     * 墙面公式
     */
    private String formula;

    /**
     * 墙面名
     */
    private String name;
    /**
     * 墙面路径
     */
    private String imagePath;
    /**
     * 墙面路径
     */
    private String imagePath2;

    /**
     * 测站数据pk
     */
    private Long stationDataPk;


}
