package com.imdroid.utils;

import com.imdroid.algorithm.leastSquare.LeastSquare;
import com.imdroid.algorithm.randomSampleConsensus.LineParameterEstimator;
import com.imdroid.algorithm.randomSampleConsensus.ParameterEstimator;
import com.imdroid.algorithm.randomSampleConsensus.Ransac;
import com.imdroid.pojo.bo.Point2D;
import lombok.NonNull;

import java.util.List;

/**
 * @Description:直线工具类
 * @Author: iceh
 * @Date: create in 2018-09-12 17:28
 * @Modified By:
 */
public class LineUtil {
    public static double[] lineFitting(@NonNull List<Point2D> pointData) {
        double[] coefficient = LeastSquare.lineCoefficient(pointData);

        return coefficient;
    }

    /**
     * @param pointData  点集
     * @param numSamples
     * @return
     */
    public static List<Double> lineRansac(@NonNull List<Point2D> pointData, int numSamples) {
        //maximalOutlierPercentage这个参数的设定理论上可以取0-1中任意小数，但为了迭代次数少一些，通过输入参数设置。
        double maximalOutlierPercentage = 1.0 - (double) numSamples * 0.9 / (double) pointData.size();
        ParameterEstimator<Point2D, Double> lpEstimator = new LineParameterEstimator(0.0001);
        Ransac<Point2D, Double> ransac = new Ransac(lpEstimator, 2, maximalOutlierPercentage);
        ransac.compute(pointData, 0.99999);

        return ransac.getParameters();
    }

}
