package com.imdroid.algorithm.leastSquare;

import Jama.Matrix;
import com.imdroid.pojo.bo.Point2D;
import com.imdroid.pojo.bo.Point3D;

import java.util.List;

/**
 * @Description: 最小二乘法 利用矩阵运算
 * @Author: iceh
 * @Date: create in 2018-10-16 00:02
 * @Modified By:
 */
public class LeastSquare {

    /**
     * 算出二维点数据 最小二乘 对应直线参数
     *
     * @param pointData
     * @return
     */
    public static <T extends Point2D> double[] lineCoefficient(List<T> pointData) {
        //准备矩阵中需要的值
        double xSum = 0, ySum = 0, xySum = 0, x2Sum = 0, y2Sum = 0;
        for (T point2D : pointData) {
            xSum += point2D.getX();
            ySum += point2D.getY();
            xySum += point2D.getX() * point2D.getY();
            x2Sum += point2D.getX() * point2D.getX();
            y2Sum += point2D.getY() * point2D.getY();
        }
        //三阶参数矩阵
        double[][] parameterArray = {
                {x2Sum, xySum, xSum},
                {xySum, y2Sum, ySum},
                {xSum, ySum, pointData.size()}
        };
        Matrix parameter = new Matrix(parameterArray);
        return getCoefficient(parameter, 3);
    }

    /**
     * 算出三维点数据 最小二乘 对应平面参数
     *
     * @param pointData
     */
    public static <T extends Point3D> double[] planeCoefficient(List<T> pointData) {
        //准备矩阵中需要的值
        double xSum = 0, ySum = 0, zSum = 0, xySum = 0, xzSum = 0, yzSum = 0;
        double x2Sum = 0, y2Sum = 0, z2Sum = 0;
        for (T point3D : pointData) {
            xSum += point3D.getX();
            ySum += point3D.getY();
            zSum += point3D.getZ();
            xySum += point3D.getX() * point3D.getY();
            xzSum += point3D.getX() * point3D.getZ();
            yzSum += point3D.getY() * point3D.getZ();
            x2Sum += point3D.getX() * point3D.getX();
            y2Sum += point3D.getY() * point3D.getY();
            z2Sum += point3D.getZ() * point3D.getZ();
        }
        //四阶参数矩阵
        double[][] parameterArray = {
                {x2Sum, xySum, xzSum, xSum},
                {xySum, y2Sum, yzSum, ySum},
                {xzSum, yzSum, z2Sum, zSum},
                {xSum, ySum, zSum, pointData.size()}
        };
        Matrix parameter = new Matrix(parameterArray);
        return getCoefficient(parameter, 4);
    }

    /**
     * 最小二乘结果的double数组
     *
     * @param parameter 参数矩阵
     * @param order     矩阵对应阶数
     * @return
     */
    private static double[] getCoefficient(Matrix parameter, Integer order) {
        //对矩阵做特征分解,并取其特征向量构成的矩阵
        Matrix feature = parameter.eig().getV();
        //得到矩阵特征值最小对应特征向量（即矩阵的第一列），并转为数组
        double[] coefficient = feature.getMatrix(0, order - 1, 0, 0).getRowPackedCopy();
//        if ((parameter.get(3, 0) > 0 || parameter.get(3, 1) > 0 || parameter.get(3, 2) > 0)&&parameter.get(3,3) > 0)
//            for (int i = 0; i < coefficient.length; i++) {
//                coefficient[i] = -coefficient[i];
//            }
        return coefficient;
    }
}
