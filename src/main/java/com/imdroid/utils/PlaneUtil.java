package com.imdroid.utils;

import com.imdroid.algorithm.leastSquare.LeastSquare;
import com.imdroid.algorithm.randomSampleConsensus.ParameterEstimator;
import com.imdroid.algorithm.randomSampleConsensus.PlaneParameterEstimator;
import com.imdroid.algorithm.randomSampleConsensus.Ransac;
import com.imdroid.pojo.bo.Plane;
import com.imdroid.pojo.bo.Point2D;
import com.imdroid.pojo.bo.Point3D;
import lombok.NonNull;

import java.util.List;

import static com.imdroid.pojo.bo.Const.Coordinate;

/**
 * @Description: 平面工具类
 * @Author: iceh
 * @Date: create in 2018-09-05 15:38
 * @Modified By:
 */
public class PlaneUtil {
    /**
     * 使用最小二乘法根据点集拟合出平面
     *
     * @param pointData
     */
    public static <T extends Point3D> Plane planeFitting(@NonNull List<T> pointData) {
        double[] coefficient = LeastSquare.planeCoefficient(pointData);
        return fromArray(coefficient).normalization();
    }

    /**
     * @param pointData
     * @param numSamples
     * @return
     */
    public static <T extends Point3D> Plane planeRansac(@NonNull List<Point3D> pointData, int numSamples) {
        //maximalOutlierPercentage这个参数的设定理论上可以取0-1中任意小数，但为了迭代次数少一些，通过输入参数设置。
        double maximalOutlierPercentage = 1.0 - (double) numSamples / (double) pointData.size();
        ParameterEstimator<Point3D, Double> ppEstimator = new PlaneParameterEstimator(0.001);
        Ransac<Point3D, Double> ransac = new Ransac(ppEstimator, 3, maximalOutlierPercentage);
        ransac.compute(pointData, 0.99999);
        double d = -(ransac.getParameters().get(0) * ransac.getParameters().get(3) + ransac.getParameters().get(1) * ransac.getParameters().get(4) + ransac.getParameters().get(2) * ransac.getParameters().get(5));
        double[] parameter = {ransac.getParameters().get(0), ransac.getParameters().get(1), ransac.getParameters().get(2), d};
        return fromArray(parameter).normalization();
    }

    /**
     * 将数组转换为平面
     *
     * @param coefficient
     * @return
     */
    public static Plane fromArray(@NonNull double[] coefficient) {
        return new Plane(coefficient[0], coefficient[1], coefficient[2], coefficient[3]);
    }


    public static <T extends Point3D> Plane optimalPlane(@NonNull List<T> pointData, String coordinate) {
        if (Coordinate.Z.equals(coordinate)) {
            return planeFitting(pointData);
        } else {
            List<Point2D> point2DS = PointUtil.reduceDimension(pointData, Coordinate.Z);
            double[] coefficient = LineUtil.lineFitting(point2DS);
            return new Plane(coefficient[0], coefficient[1], 0, coefficient[2]).normalization();
        }
    }

}
