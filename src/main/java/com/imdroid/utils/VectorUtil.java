package com.imdroid.utils;

import com.imdroid.pojo.bo.Point3D;
import com.imdroid.pojo.bo.Vector3D;

/**
 * @Description:向量工具类（数学方面的）
 * @Author: iceh
 * @Date: create in 2018-09-08 10:46
 * @Modified By:
 */
public class VectorUtil {
    /**
     * 将点中x,y,z提前出来
     *
     * @param point3D
     * @return
     */
    public static Vector3D fromBlkPoint(Point3D point3D) {
        return new Vector3D(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    /**
     * 将数组转换为向量
     *
     * @param array
     * @return
     */
    public static Vector3D fromArray(double[] array) {
        return new Vector3D(array[0], array[1], array[2]);
    }
}
