package com.imdroid.pojo.bo;

import com.imdroid.utils.ArrayUtil;
import com.imdroid.utils.PlaneUtil;
import com.imdroid.utils.VectorUtil;
import lombok.Data;
import lombok.NonNull;

/**
 * @Description:空间平面类 公式为 Ax+By+Cz+D=0 或 np+D=0 n为法向量，p为点
 * @Author: iceh
 * @Date: create in 2018-09-05 15:10
 * @Modified By:
 */
@Data
public class Plane {
    private Vector3D normalVector;
    private double intercept;

    public Plane(Vector3D normalVector, double intercept) {
        this.normalVector = normalVector;
        this.intercept = intercept;
    }

    public Plane(double a, double b, double c, double d) {
        this.normalVector = new Vector3D(a, b, c);
        this.intercept = d;
    }

    /**
     * 获取点到平面的距离
     *
     * @param point3D
     * @return
     */
    public double getDistance(@NonNull Point3D point3D) {
        Vector3D pointVector = VectorUtil.fromBlkPoint(point3D);
        return Math.abs(normalVector.dotProduct(pointVector) + intercept) / normalVector.getNorm();
    }

    /**
     * 平面之间夹角（弧度）
     *
     * @param plane
     * @return
     */
    public double getAngleArc(@NonNull Plane plane) {
        return normalVector.getAngleArc(plane.normalVector);
    }

    /**
     * 平面之间夹角（角度）
     *
     * @param plane
     * @return
     */
    public double getAngle(@NonNull Plane plane) {
        return Math.toDegrees(getAngleArc(plane));
    }

    /**
     * 转换为数组
     *
     * @return
     */
    public double[] toArray() {
        return new double[]{normalVector.getX(), normalVector.getY(), normalVector.getZ(), intercept};
    }

    /**
     * 向量标准化(将向量的模变为1)
     *
     * @return
     */
    public Plane normalization() {
        double norm = normalVector.getNorm();
        double[] normal = ArrayUtil.multiply(toArray(), 1 / norm);
        return PlaneUtil.fromArray(normal);
    }

    /**
     * 获得平面的通用公式 Ax+By+Cz+D=0
     *
     * @return
     */
    public String getFormula() {
        String formula = "";
        double[] coefficient = ArrayUtil.setScale(toArray(), 4);
        if (coefficient[0] != 0) {
            formula += coefficient[0] + "x";
        }
        if (coefficient[1] > 0) {
            formula += "+" + coefficient[1] + "y";
        } else if (coefficient[1] < 0) {
            formula += coefficient[1] + "y";
        }
        if (coefficient[2] > 0) {
            formula += "+" + coefficient[2] + "z";
        } else if (coefficient[2] < 0) {
            formula += coefficient[2] + "z";
        }
        if (coefficient[3] > 0) {
            formula += "+" + coefficient[3] + "=0";
        } else if (coefficient[3] < 0) {
            formula += coefficient[3] + "=0";
        }
        return formula;
    }


}
