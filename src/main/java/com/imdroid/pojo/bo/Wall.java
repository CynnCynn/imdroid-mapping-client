package com.imdroid.pojo.bo;

import com.imdroid.pojo.entity.BlkPoint;
import com.imdroid.pojo.entity.QuotaData;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Description: 墙面类
 * @Author: iceh
 * @Date: create in 2018-10-29 19:50
 * @Modified By:
 */
@Data
@ToString(exclude = "points")
public class Wall {
    private String name;
    private String imagePath;
    private String imagePath2;
    private String coordinate;
    //分别存x，y，z的最小值与最大值
    private double[][] bound;
    private String axis;

    private Plane plane;
    private Plane targetPlane;

    private List<Double> pointsKeySet = new ArrayList<>();
    private List<BlkPoint> points = new ArrayList<>();
    private Map<Double, List<BlkPoint>> PlanePoints = new TreeMap<>();
    private List<QuotaData> quotaDataList = new ArrayList<>();

    public List<BlkPoint> getWallPoint(Map<Double, List<BlkPoint>> points) {
        List<BlkPoint> wallPoints = new ArrayList<>();
        for(double key:points.keySet()){
            wallPoints.addAll(points.get(key));
        }
        return wallPoints;
    }

    public void addQuotaData(QuotaData quotaData) {
        this.quotaDataList.add(quotaData);
    }
}


