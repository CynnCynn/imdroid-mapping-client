package com.imdroid.algorithm.findHole;

import com.alibaba.fastjson.JSON;
import com.imdroid.enums.AssociateEnum;
import com.imdroid.enums.PointTypeEnum;
import com.imdroid.enums.QuotaEnum;
import com.imdroid.pojo.bo.*;
import com.imdroid.pojo.bo.Const.HoleDeviation;
import com.imdroid.pojo.bo.Const.Threshold;
import com.imdroid.pojo.bo.Point;
import com.imdroid.pojo.entity.BlkPoint;
import com.imdroid.pojo.entity.Quota;
import com.imdroid.pojo.entity.QuotaData;
import com.imdroid.utils.BigDecimalUtil;
import com.imdroid.utils.PlaneUtil;
import com.imdroid.utils.PointUtil;
import com.imdroid.utils.WallUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static com.imdroid.enums.QuotaEnum.HOLE_EDGE_FALTNESS;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FindHole {

    /**
     * @param allPoints     旋转后的坐标点
     * @param wallUpBound   墙面的上界
     * @param wallDownBound 墙面的下界
     * @return
     */
    public static List<BlkPoint> getWallPoints(List<BlkPoint> allPoints, double wallUpBound, double wallDownBound) {
//        System.out.println("上："+wallUpBound+"下：" + wallDownBound);
        List<BlkPoint> wallPoints = new ArrayList<>();
        for (BlkPoint b : allPoints) {
            if (b.getZ() < wallUpBound && b.getZ() > wallDownBound) {
                wallPoints.add(b);
            }
        }
        return wallPoints;
    }

    /**
     * 找出安全距离内的一圈闭环
     *
     * @param map              按0.1度分好类的点集
     * @param safeThresholdUp  安全距离上界
     * @param safeThresholdLow 安全距离下界
     * @return 闭环
     */
    public static List<BlkPoint> cycle(Map<Double, List<BlkPoint>> map, double safeThresholdUp, double safeThresholdLow) {
        List<BlkPoint> datumPoints = initDatumPoints();
        List<BlkPoint> nonZero = FindCycle.findRawEdge(map, safeThresholdUp, safeThresholdLow, datumPoints);
        pointsToFile(nonZero, "D:\\study\\python\\fof\\rawEdge.txt");

        Map<Double, List<BlkPoint>> x = groupBy(nonZero, Const.Coordinate.X, 2);
        Map<Double, List<BlkPoint>> y = groupBy(nonZero, Const.Coordinate.Y, 2);
//        System.out.println(x.keySet().toString());
//        System.out.println(y.keySet().toString());

        double xMax = 0.0;
        double xMin = 0.0;
        double yMax = 0.0;
        double yMin = 0.0;
        List<Double> xInner = new ArrayList<>();
        List<Double> yInner = new ArrayList<>();
//        int xMaxSize = 0, xMaxSize1=0;
//        int yMaxSize, yMaxSize1=0;
        for (double key : x.keySet()) {
            double m = computeAver(x.get(key), Const.Coordinate.X);
            xInner.add(m); // 需要保存值
//            if(m<0)
//                xMaxSize = ((x.get(key).size() > xMaxSize) ? x.get(key).size() : xMaxSize);
//            if(m>0)
//                xMaxSize1 = ((x.get(key).size() > xMaxSize1) ? x.get(key).size() : xMaxSize1);
            xMin = (m < xMin ? m : xMin);
            xMax = (m > xMax ? m : xMax);
        }



//
//        if (Math.abs(xMin) < 0.01) {
//            for (BlkPoint point : nonZero) {
//                xMin = (point.getX() < xMin ? point.getX() : xMin);
//            }
//        }
//
//        if (Math.abs(xMax) < 0.01) {
//            for (BlkPoint point : nonZero) {
//                xMax = (point.getX() > xMax ? point.getX() : xMax);
//            }
//        }

        for (double key : y.keySet()) {
            double m = computeAver(y.get(key), Const.Coordinate.Y);
            yInner.add(m);
            yMin = (m < yMin ? m : yMin);
            yMax = (m > yMax ? m : yMax);
        }

        xMin = (Math.abs(xMin) < 0.01 ? -xMax : xMin);
        xMax = (Math.abs(xMax) < 0.01 ? -xMin : xMax);
        yMin = (Math.abs(yMin) < 0.01 ? -yMax : yMin);
        yMax = (Math.abs(yMax) < 0.01 ? -yMin : yMax);

//        if (Math.abs(yMin) < 0.01) {
//            for (BlkPoint point : nonZero) {
//                yMin = (point.getY() < yMin ? point.getY() : yMin);
//            }
//        }
//
//        if (Math.abs(yMax) < 0.01) {
//            for (BlkPoint point : nonZero) {
//                yMax = (point.getY() > yMax ? point.getY() : yMax);
//            }
//        }

//      修正闭环(最外一圈)
        for (int i = 0; i < 3601; i++) {
            double theta = i / 10.0 * Math.PI / 180;
            if (i == 0)
                datumPoints.get(i).set_datumPoint(xMax, 0, safeThresholdUp, xMax);
            else if (i == 900)
                datumPoints.get(i).set_datumPoint(0, yMax, safeThresholdUp, yMax);
            else if (i == 1800)
                datumPoints.get(i).set_datumPoint(xMin, 0, safeThresholdUp, Math.abs(xMin));
            else if (i == 2700)
                datumPoints.get(i).set_datumPoint(0, yMin, safeThresholdUp, Math.abs(yMin));
            else if (i < 900) {
                setDatumPoint(datumPoints, i, theta, xMax, yMax, safeThresholdUp);
            } else if (i < 1800) {
                setDatumPoint(datumPoints, i, theta, xMin, yMax, safeThresholdUp);
            } else if (i < 2700) {
                setDatumPoint(datumPoints, i, theta - Math.PI, xMin, yMin, safeThresholdUp);
            } else
                setDatumPoint(datumPoints, i, theta - Math.PI, xMax, yMin, safeThresholdUp);
        }
//        FindHole.pointsToFile(datumPoints, "D:\\study\\python\\fof\\datumPoints1.txt");

//      修正凹进去的部分
        if (x.size() > 2) {
//            if(x.size() != y.size())
//            {
//                if(x.size()<y.size()){
//
//                }
//            }
            int i = 0;
            for (double key : x.keySet()) {
                i++;
                if (i > 1 && i < x.size()) {
                    double delta = xInner.get(i - 1) - FindHole.computeAver(x.get(key), Const.Coordinate.X);
                    setDatumPoint(datumPoints, x, Const.Coordinate.X, key, yMax, yMin, safeThresholdUp, delta);
                }
            }
        }
        if (y.size() > 2) {
            int i = 0;
            for (double key : y.keySet()) {
                i++;
                if (i > 1 && i < y.size()) {
                    double delta = yInner.get(i - 1) - FindHole.computeAver(y.get(key), Const.Coordinate.Y);
                    setDatumPoint(datumPoints, y, Const.Coordinate.Y, key, xMax, xMin, safeThresholdUp, delta);
                }
            }
        }

//        pointsToFile(datumPoints, "D:\\study\\python\\fof\\Edge.txt");
//        System.out.println("文件输出完成");

        return datumPoints;
    }

    /**
     * 标记外飘点
     *
     * @param allPoints
     * @param datumPoints
     * @param threshold
     */
    public static void setOuterPoints(List<BlkPoint> allPoints, List<BlkPoint> datumPoints, double threshold) {
        for (BlkPoint blkPoint : allPoints) {
//            double r = datumPoints.get((int)(blkPoint.getPhi()*10)).getR();
            if (isOuterPoint(blkPoint, datumPoints, threshold)) {
                blkPoint.setType(PointTypeEnum.OUT_POINT.getCode()); // 飘出点
            }
        }
    }

    public static void holeCycle(Map<Double, List<BlkPoint>> map, double safeThresholdUp,
                                 double safeThresholdLow, List<BlkPoint> safeCycle) {
        List<BlkPoint> holeCyclePoints = initDatumPoints();
        List<BlkPoint> nonZero = FindCycle.findRawEdge(map, safeThresholdUp, safeThresholdLow, holeCyclePoints);

        Map<Double, List<BlkPoint>> xD = groupBy(safeCycle, Const.Coordinate.X, 2);
        Map<Double, List<BlkPoint>> yD = groupBy(safeCycle, Const.Coordinate.Y, 2);
        Map<Double, List<BlkPoint>> x = groupBy(nonZero, Const.Coordinate.X, 2);
        Map<Double, List<BlkPoint>> y = groupBy(nonZero, Const.Coordinate.Y, 2);

        Double[] xDKey = xD.keySet().toArray(new Double[0]);
        Double[] xKey = x.keySet().toArray(new Double[0]);
        Double[] yDKey = yD.keySet().toArray(new Double[0]);
        Double[] yKey = y.keySet().toArray(new Double[0]);


        double xMax = computeAver(xD.get(xDKey[xDKey.length - 1]), Const.Coordinate.X);
        double xMin = computeAver(xD.get(xDKey[0]), Const.Coordinate.X);
        double yMax = computeAver(yD.get(yDKey[yDKey.length - 1]), Const.Coordinate.Y);
        double yMin = computeAver(yD.get(yDKey[0]), Const.Coordinate.Y);


        int i = 0;
        for (Double xDk : xDKey) {
            for (Double xk : xKey) {
                if (Math.abs(xDk - xk) > Threshold.OUT_POINT_DISTANCE_EXACT &&
                        Math.abs(xDk - xk) < Threshold.OUT_POINT_DISTANCE) {
                    double delta = computeAver(x.get(xk), Const.Coordinate.X) -
                            computeAver(xD.get(xDk), Const.Coordinate.X);
//                    在这面墙有梁, 外飘点边界往外扩一点
//                    if (xDk < 0) {
                    setDatumPoint(safeCycle, xD, Const.Coordinate.X, xDk, yMax, yMin, safeThresholdUp, delta);
                    if (i == 0)// 更新xMin
                        xMin += delta;
                    if (i == xDKey.length - 1)// 更新xMax
                        xMax += delta;
//                    } else {
//                        setDatumPoint(safeCycle, xD, Const.Coordinate.X, xDk, yMax, yMin, safeThresholdUp, 0.0);
//                    }
                }
            }
            i++;
        }

        yD = groupBy(safeCycle, Const.Coordinate.Y, 2);
        for (Double yDk : yDKey) {
            for (Double yk : yKey) {
                if (Math.abs(yDk - yk) > Threshold.OUT_POINT_DISTANCE_EXACT &&
                        Math.abs(yDk - yk) < Threshold.OUT_POINT_DISTANCE) {
                    double delta = computeAver(y.get(yk), Const.Coordinate.Y) -
                            computeAver(yD.get(yDk), Const.Coordinate.Y);
//                    在这面墙有梁, 外飘点边界往外扩一点
//                    if (xDk < 0) {
                    setDatumPoint(safeCycle, yD, Const.Coordinate.Y, yDk, xMax, xMin, safeThresholdUp, delta);
//                    } else {
//                        setDatumPoint(safeCycle, xD, Const.Coordinate.X, xDk, yMax, yMin, safeThresholdUp, 0.0);
//                    }
                }
            }
        }
    }

    private static List<BlkPoint> initDatumPoints() {
        List<BlkPoint> datumPoints = new ArrayList<>();
        for (int i = 0; i < 3601; i++) {
            BlkPoint point = new BlkPoint();
            point.setPhi(i / 10.0);
            point.setR(0.0);
            point.setX(0.0);
            point.setY(0.0);
            point.setZ(0.0);
            datumPoints.add(point);
        }
        return datumPoints;
    }

    private static boolean isOuterPoint(BlkPoint blkPoint, List<BlkPoint> datumPoints, double threshold) {
        double r = datumPoints.get((int) (blkPoint.getPhi() * 10)).getR();
        if (blkPoint.getR() - r > threshold) {
            double x = datumPoints.get((int) (blkPoint.getPhi() * 10)).getX();
            double y = datumPoints.get((int) (blkPoint.getPhi() * 10)).getY();
            double deltaX = blkPoint.getX() - x;
            double deltaY = blkPoint.getY() - y;
            BlkPoint newDatumP1 = new BlkPoint(blkPoint.getX() - deltaX, blkPoint.getY(), blkPoint.getZ());
            PointUtil.setPhi(newDatumP1);
            BlkPoint newDatumP2 = new BlkPoint(blkPoint.getX(), blkPoint.getY() - deltaY, blkPoint.getZ());
            PointUtil.setPhi(newDatumP2);
            double dis1 = PointUtil.getDistance(newDatumP1, datumPoints.get((int) (newDatumP1.getPhi() * 10)));
            double dis2 = PointUtil.getDistance(newDatumP2, datumPoints.get((int) (newDatumP2.getPhi() * 10)));
            double distance = Math.abs(dis1 < dis2 ? deltaX : deltaY);
            return (distance > threshold);
        }
        return false;
    }

    public static <T extends Point3D> double computeAver(@NonNull List<T> points, String coordinate) {
        double sum = 0.0;

        if (Const.Coordinate.X.equals(coordinate)) {
            for (T point : points) {
                sum += point.getX();
            }
        }
        if (Const.Coordinate.Y.equals(coordinate)) {
            for (T point : points) {
                sum += point.getY();
            }
        }
        if (Const.Coordinate.Z.equals(coordinate)) {
            for (T point : points) {
                sum += point.getZ();
            }
        }
        return sum / points.size();
    }

    public static <T extends BlkPoint> Map<Double, List<T>> groupBy(@NonNull List<T> points, String coordinate, int scale) {
        Map<Double, List<T>> map = new TreeMap<>();
        for (T point : points) {
            double coord = 0d;
            if (Const.Coordinate.X.equals(coordinate)) {
                coord = BigDecimalUtil.scale(point.getX(), scale);
            }
            if (Const.Coordinate.Y.equals(coordinate)) {
                coord = BigDecimalUtil.scale(point.getY(), scale);
            }
            if(Const.Coordinate.Z.equals(coordinate)){
                coord = BigDecimalUtil.scale(point.getZ(), scale);
            }
            List<T> subList = map.get(coord);
            if (null == subList) {
                subList = new ArrayList<>();
            }
            subList.add(point);
            map.put(coord, subList);
        }

        Map<Double, List<T>> map1 = new TreeMap<>();
        double lastKey = 0.0, map1Key = 0.0;
        int flag = 0;
        int continueFlag = 0;
        double normalSize = 0.0;
        if (points.size() > 0 && map.size() > 0) {
            normalSize = Math.floor((double) points.size() / (double) map.size()) * 4;
        }



        for (double key : map.keySet()) {
            if (flag == 0) {
                map1Key = key; // 第一个数
                flag = 1;
            }
            if (map.get(key).size() > normalSize) {
                List<T> subList = map.get(key);
                if (Math.abs(key - lastKey) < 0.02 && continueFlag == 1) {
                    subList.addAll(map1.get(map1Key));
                    map1.put(map1Key, subList);
                } else {
                    map1.put(key, subList);
                    if (continueFlag == 0)
                        map1Key = key;
                    continueFlag = 1;
                }
                lastKey = key;
            } else {
                continueFlag = 0;
            }
        }

        if(Const.Coordinate.Z.equals(coordinate)) {
//            for (double key : map.keySet()) {
//                if (map.get(key).size() > normalSize) {
//                    if (flag == 0) {
//                        map1Key = key; // 第一个数
//                        flag = 1;
//                    }
//
//                }
//            }
            return map1;
        }
//        找出最大的两个集合
        double positiveKey = 0.0, negativeKey = 0.0;
        int positiveSize = 0, negativeSize = 0;
        for (Double key : map1.keySet()) {
            if (key < 0) {
                if (map1.get(key).size() > negativeSize) {
                    negativeKey = key;
                    negativeSize = map1.get(key).size();
                }
            } else {
                if (map1.get(key).size() > positiveSize) {
                    positiveKey = key;
                    positiveSize = map1.get(key).size();
                }
            }
        }

//        先删除较为夸张的外飘点
        if (positiveKey > 0 && negativeKey < 0) {
            Iterator<Double> iterator = map1.keySet().iterator();
            double key = iterator.next();
            if (key > 0) {
                if (key / positiveKey > 5 && map1.get(key).size() * 2 < positiveSize) {
                    iterator.remove();
                }
            } else {
                if (key / negativeKey > 5 && map1.get(key).size() * 2 < negativeSize) {
                    iterator.remove();
                }
            }
        }


        if (map1.size() > 0) {// 如果不是边界，会有一个大的缺口
            int i = 0;
            Iterator<Double> iterator = map1.keySet().iterator();
            while (iterator.hasNext()) {
//            for(double key:map1.keySet()){
                i++;
                double key = iterator.next();
                Collections.sort(map1.get(key), new Comparator<BlkPoint>() {
                    @Override
                    public int compare(BlkPoint o1, BlkPoint o2) {
                        return o1.getPhi().compareTo(o2.getPhi());
                    }
                });
//                    处理跨一、四象限的情况
                if (map1.get(key).get(0).getPhi() < 90 &&
                        map1.get(key).get(map1.get(key).size() - 1).getPhi() > 270) {
                    int mark = 0;
                    for (T point : map1.get(key)) {
                        if (point.getPhi() > 270)
                            break;
                        mark++;
                    }
                    List<T> subList = map1.get(key).subList(mark, map1.get(key).size() - 1);
                    subList.addAll(map1.get(key).subList(0, mark));
                    map1.put(key, subList);
                }
//                if(i>1 && i<map1.size()){
//                    找出最大连续的区间
                List<BlkPoint> tmpContinue = new ArrayList<>();
                double start = 0.0;
                double end = 0.0;
                int size = 0;
                double continueLength = 0.0;
                for (BlkPoint blkPoint : map1.get(key)) {
//                        最大连续
                    if (tmpContinue.isEmpty()) {
                        tmpContinue.add(blkPoint);
                        continue;
                    }

                    if (Math.abs(tmpContinue.get(tmpContinue.size() - 1).getPhi() - blkPoint.getPhi()) < 5 ||
                            Math.abs(tmpContinue.get(tmpContinue.size() - 1).getPhi() - blkPoint.getPhi()) > 355) {
                        tmpContinue.add(blkPoint);
                    } else {
                        if (size < tmpContinue.size()) {
                            start = tmpContinue.get(0).getPhi();
                            end = tmpContinue.get(tmpContinue.size() - 1).getPhi();
                            size = tmpContinue.size();
                            if (Const.Coordinate.X.equals(coordinate))
                                continueLength = Math.abs(tmpContinue.get(tmpContinue.size() - 1).getY() - tmpContinue.get(0).getY());
                            else if (Const.Coordinate.Y.equals(coordinate))
                                continueLength = Math.abs(tmpContinue.get(tmpContinue.size() - 1).getX() - tmpContinue.get(0).getX());
                        }
                        tmpContinue.clear();
                        tmpContinue.add(blkPoint);
                    }

                }
                if (!tmpContinue.isEmpty() && size < tmpContinue.size()) {
                    start = tmpContinue.get(0).getPhi();
                    end = tmpContinue.get(tmpContinue.size() - 1).getPhi();
                    if (Const.Coordinate.X.equals(coordinate))
                        continueLength = Math.abs(tmpContinue.get(tmpContinue.size() - 1).getY() - tmpContinue.get(0).getY());
                    else if (Const.Coordinate.Y.equals(coordinate))
                        continueLength = Math.abs(tmpContinue.get(tmpContinue.size() - 1).getX() - tmpContinue.get(0).getX());
                }
//                    if(Const.Coordinate.X.equals(coordinate))
//                        continueLength = Math.abs(tmpContinue.get(tmpContinue.size()-1).getY()-tmpContinue.get(0).getY());
//                    else if(Const.Coordinate.Y.equals(coordinate))
//                        continueLength = key*(1/Math.tan(start/180*Math.PI)-1/Math.tan(end/180*Math.PI));

                if (continueLength < 0.5) {
                    i--;
                    iterator.remove();
                }
//                    删除不在start和end范围内的值
                else {
                    Iterator<T> iter = map1.get(key).iterator();
                    while (iter.hasNext()) {
                        BlkPoint blkPoint = iter.next();
                        if (start > end) {
                            if (blkPoint.getPhi() < start && blkPoint.getPhi() > end)
                                iter.remove();
                        } else {
                            if (blkPoint.getPhi() < start || blkPoint.getPhi() > end)
                                iter.remove();
                        }
                    }
//                        System.out.println(start+" "+end);
//                        if(map1.get(key).size()>0)
//                            System.out.println(map1.get(key).get(0).getPhi()+" "+map1.get(key).get(map1.get(key).size()-1).getPhi());

                }
            }
        }
//        }
//        Iterator<Double> iterator = map1.keySet().iterator();
//        while(iterator.hasNext()){
//            double key = iterator.next();
//            if(map1.get(key).size()<100){
//                iterator.remove();
//            }
//        }
        return map1;
    }

    private static void setDatumPoint(List<BlkPoint> datumPoints, int i, double theta, double x, double y, double z) {
        double r1 = Math.abs(x / Math.cos(theta));
        double r2 = Math.abs(y / Math.sin(theta));
        if (Math.abs(datumPoints.get(i).getR() - Math.min(r1, r2)) > 0.01) {
            if (r1 < r2) {
                datumPoints.get(i).set_datumPoint(x, x * Math.tan(theta), z, Math.min(r1, r2));
            } else {
                datumPoints.get(i).set_datumPoint(y / Math.tan(theta), y, z, Math.min(r1, r2));
            }
        }
    }

    /**
     * @param datumPoints
     * @param map
     * @param coordinate
     * @param key
     * @param max
     * @param min
     * @param safeThresholdUp
     */
    private static void setDatumPoint(List<BlkPoint> datumPoints, Map<Double, List<BlkPoint>> map,
                                      String coordinate, double key, double max, double min,
                                      double safeThresholdUp, double delta) {
//        int i=0;
//        for(double key:map.keySet()){
//            i++;
//            if(i>1 && i<map.size()){
        double value = computeAver(map.get(key), coordinate) + delta;
        double start = map.get(key).get(0).getPhi();
        double end = map.get(key).get(map.get(key).size() - 1).getPhi();
        if (start > end) {
            end += 360;
            datumPoints.get(3600).set_datumPoint(value, 0, safeThresholdUp, value);
        }
        if (Const.Coordinate.X.equals(coordinate)) {
            for (double theta = start; theta <= end; theta += 0.1) {
                double theta1 = BigDecimalUtil.scale(mod360(theta), 1);
                if (theta1 % 90 < 0.01) {
                    datumPoints.get((int) (theta1 * 10)).set_datumPoint(value, 0, safeThresholdUp, value);
                }
                if (theta1 < 180)
                    setDatumPoint(datumPoints, (int) (theta1 * 10),
                            theta1 * Math.PI / 180, value, max, safeThresholdUp);
                else
                    setDatumPoint(datumPoints, (int) (theta1 * 10),
                            theta1 * Math.PI / 180 - Math.PI, value, min, safeThresholdUp);
            }
        } else if (Const.Coordinate.Y.equals(coordinate)) {
            for (double theta = start; theta <= end; theta += 0.1) {
                double theta1 = BigDecimalUtil.scale(mod360(theta), 1);
                if (theta1 % 90 < 0.01) {
                    datumPoints.get((int) (theta1 * 10)).set_datumPoint(0, value, safeThresholdUp, value);
                }
                if (theta1 < 90 || theta1 > 270)
                    setDatumPoint(datumPoints, (int) (theta1 * 10),
                            theta1 * Math.PI / 180, max, value, safeThresholdUp);
                else
                    setDatumPoint(datumPoints, (int) (theta1 * 10),
                            theta1 * Math.PI / 180 - Math.PI, min, value, safeThresholdUp);
            }
        }
//            }
//
//        }
    }

    /**
     * @param map
     * @param datumPoints
     * @param ceiling
     * @param floor
     * @return
     */
    public static List<Double> findHoleMark(Map<Double, List<BlkPoint>> map, List<BlkPoint> datumPoints, double ceiling, double floor) {
        List<Double> hole = new ArrayList<>();

        int i;
        for (Double key : map.keySet()) {
            List<Double> holeZ = new ArrayList<>();
            int z;
            int countR = 0;
            for (BlkPoint blkPoint : map.get(key)) {
                if (!PointTypeEnum.OUT_POINT.getCode().equals(blkPoint.getType()))
                    holeZ.add(blkPoint.getZ());
                else
                    countR++;
            }
            Collections.sort(holeZ);
            double height = 0;//最小值
            for (z = 0; z < holeZ.size() - 1; z++) {
                if (holeZ.get(z + 1) - holeZ.get(z) > height) {
                    height = holeZ.get(z + 1) - holeZ.get(z);
                }
            }
            if (height > Threshold.HOLE_HEIGHT_MIN || countR > 0)
                hole.add(key);
//            System.out.println(height+" "+ normalHeight);
//            int size = map.get(key).size();
////            间隔
//            double interval = (ceiling-floor)/size*10;
//            double z = datumPoints.get((int)(key*10)).getZ();
////            System.out.println(key+" "+size+" " + interval+" "+Z+" "+R);
//            int countZ = 0;
//            int countR = 0;
//            for(BlkPoint blkPoint:map.get(key)){
////                有无缺失
//                if(z - blkPoint.getZ()>interval){
//                    z = z-interval;
//                    countZ ++;
//                }
//
//                else {
////                有无突变
//                    if(blkPoint.getZ()<z){
//                        z = blkPoint.getZ();
//                    }
//                    if (isOuterPoint(blkPoint, datumPoints)) {
//                        countR++;
//                    }
//                }
//            }
//            if((countZ>10 && size/countZ>15)||countR>0){
//                hole.add(key);
//            }
        }

//        去除误判点
//        判断hole集合中的key的连续程度
//        若连续的角度所对应的窗口长度超过40cm，则认为是洞口
        Double[] keyHole = new Double[hole.size()];
        keyHole = hole.toArray(keyHole);
        List<Double> holeMark = new ArrayList<>();
        i = 0;
        int j = 1;
        while (j < keyHole.length) {
            if (keyHole[j] - keyHole[j - 1] < 0.5) {
                j++;
//                如果到了最后一个数
                if (j == keyHole.length) {
//                  判定是否满足窗口的条件
                    if (keyHole[j - 1] - keyHole[i] < 0.5)
                        break;
                    if (Math.abs(datumPoints.get((int) (keyHole[j - 1] * 10)).getX() -
                            datumPoints.get((int) (keyHole[i] * 10)).getX()) > Threshold.HOLE_WIDTH_MIN ||
                            Math.abs(datumPoints.get((int) (keyHole[j - 1] * 10)).getY() -
                                    datumPoints.get((int) (keyHole[i] * 10)).getY()) > Threshold.HOLE_WIDTH_MIN ||
                            Math.abs(keyHole[j - 1] - 360.0) < 0.01) {
                        holeMark.add(keyHole[i]);
                        holeMark.add(keyHole[j - 1]);
                    }
                }
            }

//            有断开
            else if (keyHole[j] - keyHole[j - 1] >= 0.5) {
//                如果起点是0度，可能会和360度相接
                if (Math.abs(datumPoints.get((int) (keyHole[j - 1] * 10)).getX() -
                        datumPoints.get((int) (keyHole[i] * 10)).getX()) < Threshold.HOLE_WIDTH_MIN &&
                        Math.abs(datumPoints.get((int) (keyHole[j - 1] * 10)).getY() -
                                datumPoints.get((int) (keyHole[i] * 10)).getY()) < Threshold.HOLE_WIDTH_MIN && keyHole[i] > 0.01) {
                    i = j;
                } else {
                    holeMark.add(keyHole[i]);
                    holeMark.add(keyHole[j - 1]);
                    i = j + 1;
                }
                j++;
            }
        }
//~        完成holeMark：两个一组围城封闭图形
        return holeMark;
    }


    public static List<QuotaData> computeHole(Map<Double, List<BlkPoint>> map, List<Double> holeMark, List<BlkPoint> datumPoints, double floor) {
//        找到边缘的点集
//        先确定高，再确定宽
        List<QuotaData> quotaDataList = new ArrayList<>();
        List<Double> realHoleMark = new ArrayList<>();

        int i;
        int windowNo = 1, doorNo = 1;
        for (i = 0; i < holeMark.size() / 2; i++) {
            if (holeMark.get(2 * i) > holeMark.get(2 * i + 1) || holeMark.get(2 * i) < 0.01)
                continue;
//            开始是0，结尾是360，这两组会连城一个洞口
            if (Math.abs(holeMark.get(2 * i + 1) - 360.0) < 0.01 && holeMark.get(0) < 0.01) {
                holeMark.set(2 * i + 1, holeMark.get(1) + 360.0);
            }
            double holeMarkMiddle = BigDecimalUtil.scale((holeMark.get(2 * i) + holeMark.get(2 * i + 1)) / 2 - 0.5, 1);
            List<Double> holeZ = new ArrayList<>();
            int z;
            for (z = 0; z < 10; z++) {
                if (map.get(mod360(holeMarkMiddle + z * 0.1)) == null)
                    continue;
                for (BlkPoint holemarkmiddle : map.get(mod360(holeMarkMiddle + z * 0.1))) {
                    if (!isOuterPoint(holemarkmiddle, datumPoints, Threshold.OUT_POINT_DISTANCE_EXACT))
//                        Math.abs(holemarkmiddle.getR() - datumPoints.get((int) (mod360(holeMarkMiddle + z * 0.1) * 10)).getR()) < Threshold.OUT_POINT_DISTANCE
                        holeZ.add(holemarkmiddle.getZ());
                }
            }
            Collections.sort(holeZ);
            double height = 0;//最小值
            int heightIndex = 0;
            for (z = 0; z < holeZ.size() - 1; z++) {
                if (holeZ.get(z + 1) - holeZ.get(z) > height) {
                    height = holeZ.get(z + 1) - holeZ.get(z);
                    heightIndex = z;
                }
            }

            if (holeZ.size() == 0) {
                continue;
            }
            double holeTop, holeBottom;
//            若是门，不存在下限
            if (height < 0.1) {
                holeBottom = floor + 0.01;
                holeTop = holeZ.get(0);
            } else {
                holeTop = holeZ.get(heightIndex + 1);
                holeBottom = holeZ.get(heightIndex);
            }

            height = holeTop - holeBottom + HoleDeviation.HEIGHT_DEVIATION * 2; // 假设上下各有1cm的误差
//            取门洞两边的边缘点集
//            计算窗宽
//            取窗体中间部分的点集
            double midHeight = (holeTop + holeBottom) / 2;
            List<BlkPoint> holeXY = new ArrayList<>();
            z = 20;
            for (double start = holeMark.get(2 * i) - z * 0.1; start <= holeMark.get(2 * i + 1) + z * 0.1; start += 0.1) {
                double start1 = BigDecimalUtil.scale(mod360(start), 1);
                if (map.get(start1) == null)
                    continue;
                for (BlkPoint holeEdge : map.get(start1)) {
//                    holeEdge.getR() - datumPoints.get((int) (start1 * 10)).getR() < Threshold.OUT_POINT_DISTANCE
                    if (!isOuterPoint(holeEdge, datumPoints, Threshold.OUT_POINT_DISTANCE_EXACT) &&
                            holeEdge.getZ() < midHeight + 0.05 && holeEdge.getZ() > midHeight - 0.05)
                        holeXY.add(holeEdge);
                }
            }


//            System.out.println(holeX.size());
//            pointsToFile(holeX, "D:\\study\\python\\fof\\test1.txt");


            double holeWidthX = 0.0;
            double holeWidthY = 0.0;
            int widthIndexX = 0;
            int widthIndexY = 0;
            for (z = 0; z < holeXY.size() - 1; z++) {
                if (Math.abs(holeXY.get(z + 1).getX() - holeXY.get(z).getX()) > holeWidthX) {
                    holeWidthX = Math.abs(holeXY.get(z + 1).getX() - holeXY.get(z).getX());
                    widthIndexX = z;
                }
                if (Math.abs(holeXY.get(z + 1).getY() - holeXY.get(z).getY()) > holeWidthY) {
                    holeWidthY = Math.abs(holeXY.get(z + 1).getY() - holeXY.get(z).getY());
                    widthIndexY = z;
                }
            }
            if (holeXY.size() < 2) {
                continue;
            }

            double holeWidth = (holeWidthX > holeWidthY ? holeWidthX : holeWidthY);
            int widthIndex = (holeWidthX > holeWidthY ? widthIndexX : widthIndexY);
            String coordinate = (holeWidthX > holeWidthY ? Const.Coordinate.Y : Const.Coordinate.X);
            double startW = (holeWidthX > holeWidthY ?
                    holeXY.get(widthIndex).getX() :
                    holeXY.get(widthIndex).getY());
            double endW = (holeWidthX > holeWidthY ?
                    holeXY.get(widthIndex + 1).getX() :
                    holeXY.get(widthIndex + 1).getY());
            double coordinateValue = (holeWidthX > holeWidthY ?
                    holeXY.get(widthIndex + 1).getY() :
                    holeXY.get(widthIndex + 1).getX());


            if (startW > endW) {
                double tmp = startW;
                startW = endW;
                endW = tmp;
            }

            if (holeWidth < Threshold.HOLE_WIDTH_MIN || height < Threshold.HOLE_HEIGHT_MIN)
                continue;
            if (height / holeWidth < 0.58 || height / holeWidth > 3.2)
                continue;


//            double startW = (holeWidthX > holeWidthY ?
//                    datumPoints.get((int)(holeXY.get(widthIndex).getPhi()*10)).getX() :
//                    datumPoints.get((int)(holeXY.get(widthIndex).getPhi()*10)).getY());
//            double endW = (holeWidthX > holeWidthY ?
//                    datumPoints.get((int)(holeXY.get(widthIndex+1).getPhi()*10)).getX() :
//                    datumPoints.get((int)(holeXY.get(widthIndex+1).getPhi()*10)).getY());
//            double coordinateValue = (holeWidthX > holeWidthY ?
//                    datumPoints.get((int)(holeXY.get(widthIndex+1).getPhi()*10)).getY() :
//                    datumPoints.get((int)(holeXY.get(widthIndex+1).getPhi()*10)).getX());


            holeMark.set(2 * i, holeXY.get(widthIndex).getPhi());//洞口开始角度
            holeMark.set(2 * i + 1, holeXY.get(widthIndex + 1).getPhi());//洞口结束角度

            realHoleMark.add(holeXY.get(widthIndex).getPhi());
            realHoleMark.add(holeXY.get(widthIndex + 1).getPhi());


            height = BigDecimalUtil.scale(height, 4);
            holeWidth = BigDecimalUtil.scale(holeWidth, 4);

            Hole hole = new Hole();
            hole.setHeight(height);
            hole.setWidth(holeWidth);
            hole.setNo("c");
            hole.setCoordinate(coordinate);
            hole.setStartW(startW);
            hole.setEndW(endW);
            hole.setEndH(holeBottom);
            hole.setStartH(holeTop);
            hole.setStartAngle(holeXY.get(widthIndex).getPhi());
            hole.setEndAngle(holeXY.get(widthIndex + 1).getPhi());
            hole.setCoordinateValue(coordinateValue);
//            门洞
            if (Math.abs(holeBottom - floor) < 0.1) {
                // 考虑精度需要多加0.1度
                setHoleType(PointTypeEnum.DOOR_HOLE.getCode(), holeMark, i, datumPoints, map, holeTop, holeBottom, coordinate, coordinateValue);
                //TODO 之后要封装好
//                if (height * holeWidth >= 0.5) {
                hole.setType("门");
                hole.setName("" + (doorNo++));
//                }
            }
//            窗洞
            else {
                setHoleType(PointTypeEnum.WINDOW_HOLE.getCode(), holeMark, i, datumPoints, map, holeTop, holeBottom, coordinate, coordinateValue);
//                if (height * holeWidth >= 0.5) {
                hole.setType("窗");
                hole.setName("" + (windowNo++));
//                }
            }
            String manuals = JSON.toJSONString(hole);
            QuotaData quotaData = new QuotaData(0D, QuotaEnum.DOOR_HOLE_HEIGHT_SIZE.getCode(), 2L, 2L, AssociateEnum.STATION_DATA.getCode());
            quotaData.setManuals(manuals);
            if (quotaDataList.size() < 4) {
                quotaDataList.add(quotaData);
            }


//            System.out.println("窗" + (i + 1) + ":" + holeMark.get(2 * i) + " " + holeMark.get(2 * i + 1));
//            System.out.println("高:" + height);
//            System.out.println("宽：" + holeWidth);


        }

//        更新holeMark
        holeMark.clear();
        holeMark.addAll(realHoleMark);

        return quotaDataList;
    }

    //    计算洞口平整度
    public static List<QuotaData> computeHoleFlatness(List<QuotaData> quotaDataList, Map<Double, List<BlkPoint>> map, List<BlkPoint> datumPoints, double quota) {
        List<QuotaData> holeQuotaDataList = new ArrayList<>();
        int i;
        for (QuotaData quotaData : quotaDataList) {
            String manuals = quotaData.getManuals();
            Hole hole = JSON.parseObject(manuals, Hole.class);
            List<BlkPoint> holeSet = new ArrayList<>();
            double start = hole.getStartAngle();
            double end = hole.getEndAngle();
            if (start > end) {
                end += 360;
            }
            for (double angle = start; angle <= end; angle += 0.1) {
                double realAngle = BigDecimalUtil.scale(angle, 1);

                if (map.get(realAngle) != null) {
                    for (BlkPoint blkPoint : map.get(realAngle)) {
                        if (!isOuterPoint(blkPoint, datumPoints, 0.45)) {
                            holeSet.add(blkPoint);
                        }
                    }
                }
            }
            Map<Double, List<BlkPoint>> group;
            if (hole.getCoordinate().equals(Const.Coordinate.X))
                group = PointUtil.groupByCoordinate(holeSet, Const.Coordinate.Y_2);
            else
                group = PointUtil.groupByCoordinate(holeSet, Const.Coordinate.X_2);
            Map<Double, List<BlkPoint>> groupZ = PointUtil.groupByCoordinate(holeSet, Const.Coordinate.Z);

            List<BlkPoint> holeSetStart = new ArrayList<>();
            List<BlkPoint> holeSetEnd = new ArrayList<>();
            double normalSize = (double) holeSet.size() / group.size();
            for (Double key : group.keySet()) {
                if (Math.abs(key - hole.getStartW()) < 0.01 && group.get(key).size() > normalSize) {
                    for (BlkPoint blkPoint : group.get(key)) {
                        if (blkPoint.getZ() < hole.getStartH() && blkPoint.getZ() > hole.getEndH()) {
                            holeSetStart.add(blkPoint);
                        }
                    }
                }
                if (Math.abs(key - hole.getEndW()) < 0.01 && group.get(key).size() > normalSize) {
                    for (BlkPoint blkPoint : group.get(key)) {
                        if (blkPoint.getZ() < hole.getStartH() && blkPoint.getZ() > hole.getEndH()) {
                            holeSetEnd.add(blkPoint);
                        }
                    }
                }
            }

            addQuotaData(holeSetStart, holeQuotaDataList, hole, "width", quota);
            addQuotaData(holeSetEnd, holeQuotaDataList, hole, "width", quota);

            holeSetStart.clear();
            holeSetEnd.clear();

//            Z方向上的
            normalSize = (double) holeSet.size() / groupZ.size();

            for (Double key : groupZ.keySet()) {
                if (Math.abs(key - hole.getStartH()) < 0.01 && groupZ.get(key).size() > normalSize) {
                    holeSetStart.addAll(groupZ.get(key));
                }
                if (Math.abs(key - hole.getEndH()) < 0.01 && groupZ.get(key).size() > normalSize) {
                    holeSetEnd.addAll(groupZ.get(key));
                }
            }
            addQuotaData(holeSetStart, holeQuotaDataList, hole, "height", quota);
            addQuotaData(holeSetEnd, holeQuotaDataList, hole, "height", quota);

        }
//        for(QuotaData quotaData:holeQuotaDataList){
//            System.out.println(quotaData.toString());
//        }
        return holeQuotaDataList;
    }

    private static void addQuotaData(List<BlkPoint> blkPoints, List<QuotaData> holeQuotaDataList, Hole hole, String direction,double quota) {
        if (blkPoints.size() < 100)
            return;
        List<BlkPoint> qualified = new ArrayList<>();
        Wall wall = new Wall();
        wall.setPoints(blkPoints);
        WallUtil.testQualified(blkPoints, wall, qualified, quota);
        double flatness = (double) qualified.size() / blkPoints.size();
        String other = "";
        if (direction.equals("width")) {
//            区分左右
            if (Math.abs(blkPoints.get(0).getPhi() - hole.getStartAngle()) < Math.abs(blkPoints.get(0).getPhi() - hole.getEndAngle()))
                other = "右";
            else
                other = "左";
        }
        if (direction.equals("height")) {
//            区分上下
            if (Math.abs(blkPoints.get(0).getZ() - hole.getStartH()) < Math.abs(blkPoints.get(0).getZ() - hole.getEndH()))
                other = "上";
            else
                other = "下";
        }
        holeQuotaDataList.add(new QuotaData(flatness, QuotaEnum.HOLE_EDGE_FALTNESS.getCode(), (long) blkPoints.size(), (long) qualified.size(), AssociateEnum.STATION_DATA.getCode(), hole.getType() + hole.getName() + other));

    }

    private static double mod360(double a) {
        return BigDecimalUtil.scale((a + 360.0) % 360, 1);
    }

    /**
     * @param type        点的类型
     * @param holeMark    标记洞口开始和结束角度数组
     * @param i           holeMark的索引
     * @param datumPoints 闭环
     * @param map         按角度分的点集
     * @param holeTop
     * @param holeBottom
     */
    private static void setHoleType(int type, List<Double> holeMark, int i, List<BlkPoint> datumPoints, Map<Double, List<BlkPoint>> map,
                                    double holeTop, double holeBottom, String coordinate, double coordinateValue) {
//        往外扩2cm
//        double angle = datumPoints.get((int)(holeMark.get(2*i+1)*10)).getR();

        double end = holeMark.get(2 * i + 1) + 2;
        if (holeMark.get(2 * i) > holeMark.get(2 * i + 1)) {
            end += 360.0;
        }
//        进一步去除外飘点
        Map<Double, List<BlkPoint>> doorEdge = new TreeMap<>();
        for (double start = holeMark.get(2 * i) - 2; start <= end; start += 0.1) {
            double start1 = BigDecimalUtil.scale(mod360(start), 1);
            double r = datumPoints.get((int) (start1 * 10)).getR();
            if (map.get(start1) == null)
                continue;
            for (BlkPoint blkPoint : map.get(start1)) {
                if (blkPoint.getZ() > holeTop + HoleDeviation.HEIGHT_DEVIATION ||
                        blkPoint.getZ() < holeBottom - HoleDeviation.HEIGHT_DEVIATION ||
                        Math.abs(blkPoint.getR() - r) > Threshold.OUT_POINT_DISTANCE ||
                        blkPoint.getType().equals(PointTypeEnum.OUT_POINT.getCode())) {
                    continue;
                }
                double coord;
                if (Const.Coordinate.X.equals(coordinate)) {
                    coord = Math.abs(BigDecimalUtil.scale(blkPoint.getX(), 2));
                } else
                    coord = Math.abs(BigDecimalUtil.scale(blkPoint.getY(), 2));
                List<BlkPoint> subList = doorEdge.get(coord);
                if (null == subList) {
                    subList = new ArrayList<>();
                }
                subList.add(blkPoint);
                doorEdge.put(coord, subList);
            }
        }
        double maxKey = 0;
        int maxSize = 0;
        for (double key : doorEdge.keySet()) {
            maxKey = (maxSize < doorEdge.get(key).size() ? key : maxKey);
            maxSize = (maxSize < doorEdge.get(key).size() ? doorEdge.get(key).size() : maxSize);
        }
        maxKey = (maxKey < Math.abs(coordinateValue) ? maxKey : Math.abs(coordinateValue));

        for (double key : doorEdge.keySet()) {
            if (key <= maxKey) {
                for (BlkPoint blkPoint : doorEdge.get(key)) {
//                    if (blkPoint.getZ() < holeTop + HoleDeviation.HEIGHT_DEVIATION &&
//                            blkPoint.getZ() > holeBottom - HoleDeviation.HEIGHT_DEVIATION &&
//                            !PointTypeEnum.OUT_POINT.getCode().equals(blkPoint.getType()))
                    blkPoint.setType(type);
                }
            } else {
                for (BlkPoint blkPoint : doorEdge.get(key)) {
                    blkPoint.setType(PointTypeEnum.OUT_POINT.getCode());
                }
            }
        }
//        for(double key:map.keySet()){
//            if(coordinateValue>0){
//                for(BlkPoint blkPoint:map.get(key)){
//                    if((coordinate.equals(Const.Coordinate.X) && blkPoint.getX()>maxKey)||
//                            coordinate.equals(Const.Coordinate.Y) && blkPoint.getY()>maxKey){
//                        blkPoint.setType(PointTypeEnum.OUT_POINT.getCode());
//                    }
//                }
//            }
//            else{
//                for(BlkPoint blkPoint:map.get(key)){
//                    if((coordinate.equals(Const.Coordinate.X) && blkPoint.getX()<-maxKey)||
//                            coordinate.equals(Const.Coordinate.Y)&&blkPoint.getY()<-maxKey){
//                        blkPoint.setType(PointTypeEnum.OUT_POINT.getCode());
//                    }
//                }
//            }
//        }
    }

    /**
     * @param datumPoints 安全区域的闭环
     * @param fileName    文件名
     * @param holeMark    洞口标识
     */
    public static void pointsToFile(List<BlkPoint> datumPoints, String fileName, List<Double> holeMark) {
//        写入文件
        FileWriter fw = null;
        try {
            fw = new FileWriter(fileName);
            int i = 0;
            for (i = 0; i < holeMark.size() / 2; i++) {
                for (double j = holeMark.get(2 * i); j < holeMark.get(2 * i + 1); j += 0.1) {
                    fw.write(datumPoints.get((int) (j * 10)).getX() + " " + datumPoints.get((int) (j * 10)).getY() + "\r\n");
                }
            }
            fw.flush();


        } catch (FileNotFoundException e) {
//            e.printStackTrace();
            log.error("文件未找到");
        } catch (IOException e) {
            log.error("写文件出错");
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    log.error("写文件出错");
                }
            }
        }
    }

    /**
     * @param Points   输出点集
     * @param fileName 文件名
     */
    public static void pointsToFile(List<BlkPoint> Points, String fileName) {
        //        写入文件
        FileWriter fw = null;
        try {
            fw = new FileWriter(fileName);
            int i = 0;
            for (BlkPoint blkPoint : Points) {
                fw.write(blkPoint.getX() + " " + blkPoint.getY() + " " + blkPoint.getZ() + "\r\n");
            }
            fw.flush();
        } catch (FileNotFoundException e) {
//            e.printStackTrace();
            log.error("文件未找到");
        } catch (IOException e) {
            log.error("写文件出错");
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    log.error("写文件出错");
                }
            }
        }
    }

}
