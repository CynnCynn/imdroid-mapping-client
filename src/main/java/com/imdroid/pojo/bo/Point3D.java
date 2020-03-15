package com.imdroid.pojo.bo;

import com.imdroid.utils.BigDecimalUtil;
import lombok.Data;

/**
 * @Description:空间三维点
 * @Author: iceh
 * @Date: create in 2018-09-22 16:27
 * @Modified By:
 */
@Data
public class Point3D implements Point {
    private Double x;
    private Double y;
    private Double z;

    private boolean visited;
//    private int cid;

    private int cluster;
    private boolean Noised;

    public Point3D() {

    }

    public Point3D(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getDistance(Point3D point3D) {
        return Math.sqrt(Math.pow((this.x - point3D.getX()), 2) + Math.pow((this.y - point3D.getY()), 2) + Math.pow((this.z - point3D.getZ()), 2));
    }

    public double getDistance2D(Point3D point3D) {
        if (point3D.getX() == 0) {
            return Math.sqrt(Math.pow((this.y - point3D.getY()), 2) + Math.pow((this.z - point3D.getZ()), 2));
        } else if (point3D.getY() == 0) {
            return Math.sqrt(Math.pow((this.x - point3D.getX()), 2) + Math.pow((this.z - point3D.getZ()), 2));
        } else
            return Math.sqrt(Math.pow((this.x - point3D.getX()), 2) + Math.pow((this.y - point3D.getY()), 2));
    }

    /**
     * 默认设置为4位
     */
    public void format() {
        format(4);
    }

    /**
     * 对point的xyz值设置小数点后位数
     *
     * @param scale
     */
    public void format(int scale) {
        x = BigDecimalUtil.scale(x, scale);
        y = BigDecimalUtil.scale(y, scale);
        z = BigDecimalUtil.scale(z, scale);
    }

}
