package com.imdroid.algorithm.randomSampleConsensus;

import java.util.List;

/**
 * 模型估计器接口
 *
 * @param <T> 样本的类型
 * @param <S> 参数的类型
 * @author tigerlihao
 */
public interface ParameterEstimator<T, S> {
    /**
     * 执行准确参数估计的方法
     *
     * @param data 用于估计的样本集合
     * @return 模型参数列表
     */
    List<S> estimate(List<T> data);

    /**
     * 执行最小二乘法估计的方法
     *
     * @param data 用于估计的样本集合
     * @return 模型参数列表
     */
    List<S> leastSquaresEstimate(List<T> data);

    /**
     * 测试样本是否符合模型参数的方法
     *
     * @param parameters 模型参数
     * @param data       待测样本
     */
    boolean agree(List<S> parameters, T data);
}