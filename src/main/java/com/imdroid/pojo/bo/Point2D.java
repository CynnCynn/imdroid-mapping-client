package com.imdroid.pojo.bo;


import lombok.Data;


/**
 * @Description:平面上的点
 * @Author: iceh
 * @Date: create in 2018-09-22 16:26
 * @Modified By:
 */
@Data
public class Point2D implements Point {
    private double x;
    private double y;
    public Point2D() {

    }

    public Point2D(Double x, Double y) {
        this.x = x;
        this.y = y;
    }
}

