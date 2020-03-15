package com.imdroid.pojo.bo;

import lombok.Data;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2019-01-14 21:22
 * @Modified By:
 */
@Data
public class Hole {
    private String name;
    private String type;
    private String No;
    private double width;
    private double height;

    private String coordinate; // X或者Y方向
    private double startW; // 宽度开始刻度
    private double endW; // 宽度结束刻度
    private double startH; // 高度开始刻度
    private double endH; // 高度结束刻度
    private double coordinateValue; // 坐标方向上的刻度

    private double startAngle; // 开始角度
    private double endAngle; // 结束角度

    private double thickness;
    private double verticality;
    private double standardSize;
    private double measureSize;
}
