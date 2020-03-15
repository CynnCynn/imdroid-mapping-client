package com.imdroid.algorithm.kmeans;

import com.imdroid.pojo.bo.Point3D;
import com.imdroid.pojo.entity.BlkPoint;

import java.io.IOException;
import java.util.*;

public class Kmeans {

    /**
     * @param
     * @param helpCenterList
     * @throws IOException
     */

    public static List<ArrayList<Point3D>>
    initHelpCenterList(List<ArrayList<Point3D>> helpCenterList, int k) {
        for (int i = 0; i < k; i++) {
            helpCenterList.add(new ArrayList<Point3D>());
        }
        return helpCenterList;
    }

    /**
     * @param
     * @throws IOException
     */
    public static <T extends BlkPoint> List<T> filter(List<T> dataList) {

        List<Point3D> centers = new ArrayList<Point3D>();
        List<Point3D> newCenters = new ArrayList<Point3D>();
        List<ArrayList<Point3D>> helpCenterList = new ArrayList<ArrayList<Point3D>>();

        //随机确定K个初始聚类中心
        Random rd = new Random();
        int k = 10;
        HashSet<Integer> hashSet = new HashSet<>();
        randomSet(0, dataList.size(), k, hashSet);
        Iterator iterator = hashSet.iterator();
        while (iterator.hasNext()) {
            int index = (Integer) iterator.next();
            centers.add(dataList.get(index));
            helpCenterList.add(new ArrayList<Point3D>());
        }
        int count = 0;
        while (count < 1000) {//进行若干次迭代，直到聚类中心稳定
            count++;
            for (int i = 0; i < dataList.size(); i++) {//标注每一条记录所属于的中心
                double minDistance = 99999999;
                int centerIndex = -1;
                for (int j = 0; j < k; j++) {//离0~k之间哪个中心最近
                    double currentDistance = 0;
                    currentDistance += centers.get(j).getDistance(dataList.get(i));
                    if (minDistance > currentDistance) {
                        minDistance = currentDistance;
                        centerIndex = j;
                    }
                }
                helpCenterList.get(centerIndex).add(dataList.get(i));
            }

            //计算新的k个聚类中心
            for (int i = 0; i < k; i++) {
                double meanx = 0;
                double meany = 0;
                double meanz = 0;

                for (int j = 0; j < helpCenterList.get(i).size(); j++) {
                    meanx += helpCenterList.get(i).get(j).getX();
                    meany += helpCenterList.get(i).get(j).getY();
                    meanz += helpCenterList.get(i).get(j).getZ();
                }

                meanx /= helpCenterList.get(i).size();
                meany /= helpCenterList.get(i).size();
                meanz /= helpCenterList.get(i).size();

                Point3D tmp = new Point3D(meanx, meany, meanz);

                newCenters.add(tmp);

            }
            double distance = 0;

            for (int i = 0; i < k; i++) {
                distance += centers.get(i).getDistance(newCenters.get(i));
            }
            if (distance < 0.003)//小于阈值时，结束循环
                break;
            else//否则，新的中心来代替旧的中心，进行下一轮迭代
            {
                centers = new ArrayList<Point3D>(newCenters);
                newCenters = new ArrayList<Point3D>();
                helpCenterList = new ArrayList<ArrayList<Point3D>>();
                helpCenterList = initHelpCenterList(helpCenterList, k);
            }
        }
        int dataSetLength = dataList.size();
        double[][] distance = new double[k][dataSetLength];
        double[] distanceSum = new double[k];

        List<T> newList = new ArrayList<>();
        for (int j = 0; j < k; j++) {
            if (helpCenterList.get(j).size() < dataList.size() / 30) {
                for (int i = 0; i < helpCenterList.get(j).size(); i++) {
                    newList.add((T) helpCenterList.get(j).get(i));
                }
            } else {
                for (int i = 0; i < helpCenterList.get(j).size(); i++) {
                    distance[j][i] = helpCenterList.get(j).get(i).getDistance(centers.get(j));
                    distanceSum[j] += distance[j][i];
                }
                //每个簇的平均距离
                distanceSum[j] /= helpCenterList.get(j).size();
                double radius = distanceSum[j] + 1.5 * Standardlizerdistance(distance[j], distanceSum[j]);
                for (int i = 0; i < helpCenterList.get(j).size(); i++) {
                    if (distance[j][i] >= radius && (helpCenterList.get(j).get(i).getDistance(new Point3D(0.0, 0.0, 0.0)) < 1.8)) {
                        newList.add((T) helpCenterList.get(j).get(i));
                    }
                }

            }

        }

        return newList;

    }

    public static void randomSet(int min, int max, int n, HashSet<Integer> set) {
        if (n > (max - min + 1) || max < min) {
            return;
        }
        for (int i = 0; i < n; i++) {
            // 调用Math.random()方法
            int num = (int) (Math.random() * (max - min)) + min;
            set.add(num);// 将不同的数存入HashSet中
        }
        int setSize = set.size();
        // 如果存入的数小于指定生成的个数，则调用递归再生成剩余个数的随机数，如此循环，直到达到指定大小
        if (setSize < n) {
            randomSet(min, max, n - setSize, set);// 递归
        }
    }

    private static double Standardlizerdistance(double[] distance, double x) {
        double currentDistance = 0;

        for (int t = 1; t < distance.length; t++) {//计算两点之间的欧式距离
            currentDistance += (distance[t] - x) * (distance[t] - x);
        }

        return Math.sqrt(currentDistance / distance.length);
    }
}
