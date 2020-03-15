package com.imdroid.pojo.bo;

import com.imdroid.utils.ArrayUtil;
import com.imdroid.utils.VectorUtil;
import lombok.Data;
import lombok.NonNull;

/**
 * @Description:空间三维向量
 * @Author: iceh
 * @Date: create in 2018-09-07 18:31
 * @Modified By:
 */
@Data
public class Vector3D {
    private double x;
    private double y;
    private double z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * 加向量
     *
     * @param v
     */
    public Vector3D plus(@NonNull Vector3D v) {
        return new Vector3D(x + v.x, y + v.y, z + v.z);
    }

    /**
     * 减向量
     *
     * @param v
     */
    public Vector3D minus(@NonNull Vector3D v) {
        return new Vector3D(x - v.x, y - v.y, z - v.z);
    }

    /**
     * 数乘向量
     *
     * @param n
     */
    public Vector3D scalarMultiply(@NonNull double n) {
        return new Vector3D(x * n, y * n, z * n);
    }

    /**
     * 点乘向量
     *
     * @param v
     * @return
     */
    public double dotProduct(@NonNull Vector3D v) {
        return x * v.x + y * v.y + z * v.z;
    }

    /**
     * 叉乘向量
     *
     * @param v
     * @return
     */
    public Vector3D crossProduct(Vector3D v) {
        return new Vector3D(y * v.z - v.y * z, z * v.x - v.z * x, x * v.y - v.x * y);
    }

    /**
     * 向量的模
     *
     * @return
     */
    public double getNorm() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * 转换为数组
     *
     * @return
     */
    public double[] toArray() {
        return new double[]{x, y, z};
    }

    /**
     * 向量标准化(将向量的模变为1),默认取小数点后4位
     *
     * @return
     */
    public Vector3D normalization() {
        return normalization(4);
    }

    /**
     * 向量标准化(将向量的模变为1)
     *
     * @param scale 小数点位数
     * @return
     */
    public Vector3D normalization(int scale) {
        double norm = getNorm();
        double[] normal = ArrayUtil.multiply(toArray(), 1 / norm);
        double[] scaleNormal = ArrayUtil.setScale(normal, scale);
        return VectorUtil.fromArray(scaleNormal);
    }

    /**
     * 向量之间夹角（弧度）
     *
     * @param v
     * @return
     */
    public double getAngleArc(@NonNull Vector3D v) {
        return Math.acos(dotProduct(v) / (getNorm() * v.getNorm()));
    }

    /**
     * 向量之间夹角 （角度）
     *
     * @param v
     * @return
     */
    public double getAngle(@NonNull Vector3D v) {
        return Math.toDegrees(getAngleArc(v));
    }

}
