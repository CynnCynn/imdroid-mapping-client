package com.imdroid.pojo.entity;

import com.imdroid.pojo.bo.Point3D;
import com.imdroid.utils.BigDecimalUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description:blk360扫描仪的三维点
 * @Author: iceh
 * @Date: create in 2018-09-04 11:58
 * @Modified By:
 */
@Data
public class BlkPoint extends Point3D implements Serializable {
    private Long pk;

    private Long intensity;

    private Long wallDataPk;
    private Integer type;

    private Double r;
    private Double theta;
    private Double phi;

    public BlkPoint() {
    }

    public BlkPoint(double x, double y, double z) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
    }

//    @Override
//    public int compareTo(BlkPoint m) {
//        // 对theta排序，以后可以改为对z排序。
//        return this.getTheta().compareTo(m.getTheta());
//    }

    /**
     * 对theta值设置小数点后位数
     *
     * @param scale 精度
     */
    public void phi_format(int scale) {
        this.phi = BigDecimalUtil.scale(phi, scale);
    }

    public void set_datumPoint(double x, double y, double z, double r){
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        this.setR(r);
    }
}
