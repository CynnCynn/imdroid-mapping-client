package com.imdroid.pojo.bo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 来源:https://blog.csdn.net/freedomboy319/article/details/48828449
 *
 * @Description:
 * @Author: zouzhongfan
 * @Date: create in 2018-09-26 19:06
 * @Modified By:
 */
public @Data
class DataNode {
    private String nodeName; // 样本点名
    private double[] dimension; // 样本点的维度
    private double kDistance; // k-距离
    private List<DataNode> kNeighbor = new ArrayList<DataNode>();// k-领域
    private double distance; // 到给定点的欧几里得距离
    private double reachDensity;// 可达密度
    private double reachDis;// 可达距离

    private double lof;// 局部离群因子


    public DataNode(String nodeName, double[] dimension) {
        this.nodeName = nodeName;
        this.dimension = dimension;
    }
}
