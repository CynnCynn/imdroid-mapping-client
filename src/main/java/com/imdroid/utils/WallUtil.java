package com.imdroid.utils;

import com.imdroid.algorithm.kmeans.Kmeans;
import com.imdroid.algorithm.findHole.FindHole;
import com.imdroid.algorithm.simpleFilter.SimpleFilter;
import com.imdroid.dao.mapper.QuotaMapper;
import com.imdroid.enums.AssociateEnum;
import com.imdroid.enums.PointTypeEnum;
import com.imdroid.enums.QuotaEnum;
import com.imdroid.pojo.bo.*;
import com.imdroid.pojo.bo.Const.Axis;
import com.imdroid.pojo.bo.Const.Coordinate;
import com.imdroid.pojo.bo.Const.PermitDeviation;
import com.imdroid.pojo.bo.Const.PlaneName;
import com.imdroid.pojo.dto.WallDataDTO;
import com.imdroid.pojo.entity.BlkPoint;
import com.imdroid.pojo.entity.Quota;
import com.imdroid.pojo.entity.QuotaData;
import com.imdroid.utils.WiFiUtil.cmd.Ssid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * @Description: 墙面工具类
 * @Author: iceh
 * @Date: create in 2018-10-31 11:57
 * @Modified By:
 */
@Slf4j
public class WallUtil {

    /**
     * 获取对应的高度宽度
     *
     * @param wall
     */
    public static void findBound(@NonNull Wall wall) {
        List<BlkPoint> points = wall.getPoints();
        //初始赋值
        double xMax = points.get(0).getX(), xMin = xMax;
        double yMax = points.get(0).getY(), yMin = yMax;
        double zMax = points.get(0).getZ(), zMin = zMax;
        //TODO 后续可根据坐标轴向优化
        for (BlkPoint point : points) {
            double currentX = point.getX();
            double currentY = point.getY();
            double currentZ = point.getZ();
            if (xMax < currentX) {
                xMax = currentX;
            }
            if (xMin > currentX) {
                xMin = currentX;
            }
            if (yMax < currentY) {
                yMax = currentY;
            }
            if (yMin > currentY) {
                yMin = currentY;
            }
            if (zMax < currentZ) {
                zMax = currentZ;
            }
            if (zMin > currentZ) {
                zMin = currentZ;
            }
        }
        double[][] bound = {
                {xMin, xMax},
                {yMin, yMax},
                {zMin, zMax}};
        wall.setBound(bound);

//        标记边界点
        if (wall.getCoordinate().equals(Coordinate.X)) {
            Map<Double, List<BlkPoint>> mapY = PointUtil.groupByCoordinate(wall.getPoints(), Coordinate.Y_2);
            setBoundPoints(mapY, Coordinate.Y, Coordinate.Z);
            Map<Double, List<BlkPoint>> mapZ = PointUtil.groupByCoordinate(wall.getPoints(), Coordinate.Z_2);
            setBoundPoints(mapZ, Coordinate.Z, Coordinate.Y);
        }
        if (wall.getCoordinate().equals(Coordinate.Y)) {
            Map<Double, List<BlkPoint>> mapX = PointUtil.groupByCoordinate(wall.getPoints(), Coordinate.X_2);
            setBoundPoints(mapX, Coordinate.X, Coordinate.Z);
            Map<Double, List<BlkPoint>> mapZ = PointUtil.groupByCoordinate(wall.getPoints(), Coordinate.Z_2);
            setBoundPoints(mapZ, Coordinate.Z, Coordinate.X);
        }
        if (wall.getCoordinate().equals(Coordinate.Z)) {
            Map<Double, List<BlkPoint>> mapX = PointUtil.groupByCoordinate(wall.getPoints(), Coordinate.X_2);
            setBoundPoints(mapX, Coordinate.Z, Coordinate.Y);
            Map<Double, List<BlkPoint>> mapY = PointUtil.groupByCoordinate(wall.getPoints(), Coordinate.Y_2);
            setBoundPoints(mapY, Coordinate.Z, Coordinate.X);
        }
    }

    private static void setBoundPoints(Map<Double, List<BlkPoint>> map, String coordinate1, String coordinate2) {

        for (Double key : map.keySet()) {
            if (map.get(key).size() > 0) {
                if (!Coordinate.Z.equals(coordinate1)) {
                    map.get(key).sort(new Comparator<BlkPoint>() {
                        @Override
                        public int compare(BlkPoint o1, BlkPoint o2) {
                            return o1.getZ().compareTo(o2.getZ());
                        }
                    });
                } else {
                    if (Coordinate.X.equals(coordinate2)) {
                        map.get(key).sort(new Comparator<BlkPoint>() {
                            @Override
                            public int compare(BlkPoint o1, BlkPoint o2) {
                                return o1.getX().compareTo(o2.getX());
                            }
                        });
                    } else {
                        map.get(key).sort(new Comparator<BlkPoint>() {
                            @Override
                            public int compare(BlkPoint o1, BlkPoint o2) {
                                return o1.getY().compareTo(o2.getY());
                            }
                        });
                    }
                }
                map.get(key).get(0).setType(PointTypeEnum.BOUND_POINT.getCode());
                map.get(key).get(map.get(key).size() - 1).setType(PointTypeEnum.BOUND_POINT.getCode());
            }

        }


    }

    /**
     * 转换wall到wallData
     *
     * @param wall
     * @return
     */
    public static WallDataDTO formatWall(@NonNull Wall wall, double floor, double quota) {
        log.info("开始计算墙面" + wall.getName());
        //对图片进行视觉上的校正
//        WallUtil.symmetry(wall);
        //填充墙面所需数据
        WallUtil.fillWallQuota(wall);
        //算出墙中点的边界
        WallUtil.findBound(wall);
        //需要先设好平面，因为后续的计算需要
        WallUtil.formatPointType(wall, floor, quota);


        if (wall.getName().equals(PlaneName.CEILING)) {
            FindHole.pointsToFile(wall.getPoints(), "D:\\study\\python\\fof\\ceiling.txt");
        }
        WallDataDTO wallDataDTO = new WallDataDTO();
        BeanUtils.copyProperties(wall, wallDataDTO);
        Long totalPoints = (long) wall.getPoints().size();
        wallDataDTO.setTotalPoints(totalPoints);
        wallDataDTO.setFormula(wall.getPlane().getFormula());
        return wallDataDTO;
    }

    /**
     * 根据wall里的point，填充wall的指标与展示所需数据集
     *
     * @param wall
     */
    public static void fillWallQuota(@NonNull Wall wall) {
        String coordinate = wall.getCoordinate();
        //根据墙中的点，做拟合得到点对应平面函数
        List<BlkPoint> points = wall.getPoints();
//        List<BlkPoint> filterPoints = Kmeans.filter(points);
//        List<DataNode> nodeList = PointUtil.lofNodeList(filterPoints);
//        for (int i = 0; i < nodeList.size(); i++) {
//            if (nodeList.get(i).getLof() > 0.8) {
//                BlkPoint temp = new BlkPoint(nodeList.get(i).getDimension()[0],nodeList.get(i).getDimension()[1],nodeList.get(i).getDimension()[2]);
//                points.remove(temp);
//            }
//        }
//        List<Point3D> point3DS = new ArrayList<>();
//        for (int i = 0; i < points.size(); i++){
//            Point3D point3D = points.get(i);
//            point3DS.add(point3D);
//        }

//        Plane plane = PlaneUtil.planeRansac(point3DS, point3DS.size() / 10);

        Plane plane = PlaneUtil.planeFitting(points);
//        Plane targetPlane;
        Plane targetPlane = PlaneUtil.optimalPlane(points, coordinate);
        wall.setPlane(plane);
        wall.setTargetPlane(targetPlane);
//        wall.addQuotaData(quotaData);

    }


    private static void formatPointType(@NonNull Wall wall, double floor, double quota) {
        //根据墙面函数对点分类
        List<BlkPoint> qualified_5 = new ArrayList<>();
        List<BlkPoint> raise_5 = new ArrayList<>();
        List<BlkPoint> sag_5 = new ArrayList<>();
        List<BlkPoint> points = wall.getPoints();

        List<BlkPoint> raise_9 = new ArrayList<>();
        List<BlkPoint> sag_9 = new ArrayList<>();

        List<BlkPoint> baseboard_qualified_5 = new ArrayList<>();
        List<BlkPoint> baseboard_points = new ArrayList<>();

//        List<BlkPoint> qualified_5_2 = new ArrayList<>();
////        二次拟合测试
//        testQualified(points, wall, qualified_5);
//        testQualified(qualified_5, wall, qualified_5_2);
//        log.warn(wall.getName()+" 合格点占比：" + (double)qualified_5.size()/points.size());
//                " 合格点占比(二次)：" + (double)qualified_5_2.size()/points.size()));
//        qualified_5.clear();

        String coordinate = wall.getCoordinate();
        Map<Double, List<BlkPoint>> map = new TreeMap<>();
//        double disToPlane = wall.getPlane().getDistance(new Point3D(0.0,0.0,0.0));
//        double keyMin = 0;
//        if(disToPlane< Const.Threshold.BLK_MINDISTANCE){
//            keyMin = Math.sqrt(Math.pow(Const.Threshold.BLK_MINDISTANCE, 2) - Math.pow(disToPlane, 2));
//        }
        switch (coordinate) {
            case Coordinate.X:
                map = PointUtil.groupByCoordinate(points, Coordinate.DIS_TO_X_AXIS);
                break;
            case Coordinate.Y:
                map = PointUtil.groupByCoordinate(points, Coordinate.DIS_TO_Y_AXIS);
                break;
            case Coordinate.Z:
                map = PointUtil.groupByCoordinate(points, Coordinate.DIS_TO_Z_AXIS);
                break;
        }
        List<BlkPoint> subPoints = new ArrayList<>();
        int sumSize = 0;
        int totalSize = points.size();
//        for(int i=1; i<30; i++) {
//            sumSize=0;
//        System.out.println(wall.getName()+map.size()+totalSize);
        for (double key : map.keySet()) {
            sumSize += map.get(key).size();
//              里面的一圈不要
//                if(key<keyMin){
//                    qualified_5.addAll(map.get(key));
//                    continue;
//                }
            subPoints.addAll(map.get(key));

//                for(BlkPoint blkPoint:map.get(key)){
//                    if(Coordinate.Y.equals(coordinate) && Math.sqrt(Math.pow(blkPoint.getX(),2)+Math.pow(blkPoint.getZ(),2))<keyMin){
//                        qualified_5.add(blkPoint);
//                    }
//                    else if(Coordinate.X.equals(coordinate) && Math.sqrt(Math.pow(blkPoint.getY(),2)+Math.pow(blkPoint.getZ(),2))<keyMin){
//                        qualified_5.add(blkPoint);
//                    }
//                    else
//                        subPoints.add(blkPoint);
//                }

            if (totalSize - sumSize < Const.SubSize.subPointsSize)
                continue;
            if (subPoints.size() > Const.SubSize.subPointsSize) {
                chooseNotQualified(subPoints, wall, qualified_5, sag_5, raise_5, sag_9, raise_9, baseboard_qualified_5, baseboard_points, floor, quota);
//                log.info(key+" "+subPoints.size());
                subPoints.clear();
            }

        }
        if (subPoints.size() > 0) {
            chooseNotQualified(subPoints, wall, qualified_5, sag_5, raise_5, sag_9, raise_9,
                    baseboard_qualified_5, baseboard_points, floor, quota);
            subPoints.clear();
        }
//            log.info(i*1000+" "+(double) qualified_5.size() / points.size());
//            qualified_5.clear(); sag_5.clear(); raise_5.clear(); sag_9.clear(); raise_9.clear();
//        }
//

//
//        Plane plane = wall.getPlane();
//
//        for (BlkPoint blkPoint : points) {
//            if (PointTypeEnum.BASIS.getCode().equals(blkPoint.getType())) {
//                //天花的指标做特殊处理
//                double quota = PermitDeviation.SMALL;
//                if (null != wall.getName() && wall.getName().equals(PlaneName.CEILING)) {
//                    quota = PermitDeviation.LARGE;
//                }
//
//                double distance1 = plane.getDistance(blkPoint);
//
//                if (distance1 <= quota) {
//                    blkPoint.setType(PointTypeEnum.QUALIFIED.getCode());
//                    qualified_5.add(blkPoint);
//                } else {
//                    Plane zeroPlane = new Plane(plane.getNormalVector(), 0);
//                    double distance2 = zeroPlane.getDistance(blkPoint);
//                    boolean isSag = Math.abs(plane.getIntercept()) + quota < distance2;
//                    boolean isRaise = Math.abs(plane.getIntercept()) - quota > distance2;
//
//                    if (isSag) {
//                        sag_5.add(blkPoint);
//                    }
//                    if (isRaise) {
//                        raise_5.add(blkPoint);
//                    }
//                }
//
//                if (distance1 <= 0.008) {
//                    qualified_9.add(blkPoint);
//                } else {
//                    Plane zeroPlane = new Plane(plane.getNormalVector(), 0);
//                    double distance2 = zeroPlane.getDistance(blkPoint);
//                    boolean isSag = Math.abs(plane.getIntercept()) + 0.008 < distance2;
//                    boolean isRaise = Math.abs(plane.getIntercept()) - 0.008 > distance2;
//                    if (isSag) {
//                        sag_9.add(blkPoint);
//                    }
//                    if (isRaise) {
//                        raise_9.add(blkPoint);
//                    }
//                }
//            }
//        }
        double distance3 = wall.getPlane().getDistance(new Point3D(0.0, 0.0, 0.0));
        SimpleFilter simpleFilter = new SimpleFilter();
        simpleFilter.filter(raise_5, raise_9, distance3, "raise", false, wall);
        simpleFilter.filter(sag_5, sag_9, distance3, "sag", false, wall);

//        水平度(天花、地板)/垂直度(墙)
        Plane plane = wall.getPlane();
        Vector3D normalVector = plane.getNormalVector();
        double angle = normalVector.getAngle(new Vector3D(0, 0, 1));
        Long detectionPoints = (long) points.size();
        Long passPoints = (long) qualified_5.size();
        //设置指标
        Integer quotaType;
        if (Coordinate.Z.equals(wall.getCoordinate())) {
            quotaType = QuotaEnum.LEVELNESS.getCode();
        } else {
            quotaType = QuotaEnum.VERTICAL.getCode();
        }
        QuotaData quotaData1 = new QuotaData(angle, quotaType, detectionPoints, passPoints, AssociateEnum.WALL_DATA.getCode());
        wall.addQuotaData(quotaData1);

        //计算出平整度
        double flatness = (double) qualified_5.size() / points.size();


        if (null != wall.getName() && wall.getName().equals(PlaneName.CEILING)) {
            QuotaData quotaData = new QuotaData(flatness, QuotaEnum.CEIL_FLATNESS.getCode(), detectionPoints, passPoints, AssociateEnum.WALL_DATA.getCode());
            wall.addQuotaData(quotaData);
        } else if (null != wall.getName() && wall.getName().equals(PlaneName.FLOOR)) {
            QuotaData quotaData = new QuotaData(flatness, QuotaEnum.FLOOR_FLATNESS.getCode(), detectionPoints, passPoints, AssociateEnum.WALL_DATA.getCode());
            wall.addQuotaData(quotaData);
        } else {
            QuotaData quotaData = new QuotaData(flatness, QuotaEnum.FLATNESS.getCode(), detectionPoints, passPoints, AssociateEnum.WALL_DATA.getCode());
            wall.addQuotaData(quotaData);
//            踢脚线平整度
            if (baseboard_qualified_5.size() > 0 && baseboard_points.size() > 0) {
                double baseboard_flatness = (double) baseboard_qualified_5.size() / baseboard_points.size();
//                System.out.println(baseboard_flatness+" "+AssociateEnum.WALL_DATA.getCode());
                QuotaData quotaData_baseboard = new QuotaData(baseboard_flatness, QuotaEnum.BASEBOARD.getCode(), detectionPoints, passPoints, AssociateEnum.WALL_DATA.getCode());
                wall.addQuotaData(quotaData_baseboard);
            }
        }
        chooseNotQualified_v(qualified_5, wall, quota);

    }

    /**
     * 墙面平整度合格点数统计
     *
     * @param planePoints
     * @param wall
     * @param qualified_5
     * @param quota
     */
    public static void testQualified(List<BlkPoint> planePoints, Wall wall, List<BlkPoint> qualified_5, double quota) {
        Plane subPlane = PlaneUtil.planeFitting(planePoints);
        for (BlkPoint blkPoint : wall.getPoints()) {
//            double quota = PermitDeviation.SMALL;
            if (null != wall.getName() && wall.getName().equals(PlaneName.CEILING)) {
                quota += PermitDeviation.LARGE;
            }
            double distance1 = subPlane.getDistance(blkPoint);
            if (distance1 <= quota) {
                qualified_5.add(blkPoint);
            }
        }
    }

    /**
     * @param subPoints
     * @param wall
     * @param qualified_5
     * @param sag_5
     * @param raise_5
     * @param sag_9
     * @param raise_9
     * @param baseboard_qualified_5
     * @param baseboard_points
     * @param floor
     * @param quota
     */
    private static void chooseNotQualified(List<BlkPoint> subPoints, Wall wall, List<BlkPoint> qualified_5, List<BlkPoint> sag_5,
                                           List<BlkPoint> raise_5, List<BlkPoint> sag_9, List<BlkPoint> raise_9,
                                           List<BlkPoint> baseboard_qualified_5, List<BlkPoint> baseboard_points, double floor, double quota) {
        double sagQuota = (double) ((int) (quota / 100) % 100) / 1000;
        double raiseQuota = quota % 100 / 1000;
//        System.out.println(sagQuota + " " + raiseQuota);

        Plane subPlane = PlaneUtil.planeFitting(subPoints);
        for (BlkPoint blkPoint : subPoints) {
            if (PointTypeEnum.BASIS.getCode().equals(blkPoint.getType())) {
                //天花的指标做特殊处理
//                double quota = PermitDeviation.SMALL;
                if (null != wall.getName() && wall.getName().equals(PlaneName.CEILING)) {
                    sagQuota += PermitDeviation.LARGE;
                    raiseQuota += PermitDeviation.LARGE;
                }

//                踢脚线的点
                if (blkPoint.getZ() < floor + Const.Threshold.BASEBOARD_HEIGHT && blkPoint.getZ() > floor)
                    baseboard_points.add(blkPoint);


//                double distance1 = subPlane.getDistance(blkPoint);
//                if (distance1 <= quota) {
//                    blkPoint.setType(PointTypeEnum.QUALIFIED.getCode());
//                    qualified_5.add(blkPoint);
//                    if (blkPoint.getZ() < floor + Const.Threshold.BASEBOARD_HEIGHT && blkPoint.getZ() > floor)
//                        baseboard_qualified_5.add(blkPoint);
//                } else {
                Plane zeroPlane = new Plane(subPlane.getNormalVector(), 0);
                double distance2 = zeroPlane.getDistance(blkPoint);
                boolean isSag = (Math.abs(subPlane.getIntercept()) + sagQuota < distance2); // &&
//                            (Math.abs(subPlane.getIntercept()) + quota + 0.001 >= distance2);
                boolean isRaise = (Math.abs(subPlane.getIntercept()) - raiseQuota > distance2); //  &&
//                            (Math.abs(subPlane.getIntercept()) - quota -0.001 <= distance2);

                if (isSag) {
                    sag_5.add(blkPoint);
                }
                if (isRaise) {
                    raise_5.add(blkPoint);
                }
//                合格点
                if (!isSag && !isRaise) {
                    blkPoint.setType(PointTypeEnum.QUALIFIED.getCode());
                    qualified_5.add(blkPoint);
                    if (blkPoint.getZ() < floor + Const.Threshold.BASEBOARD_HEIGHT && blkPoint.getZ() > floor)
                        baseboard_qualified_5.add(blkPoint);
                }
//                }

//                假设去除九带五
//                if (distance1 > 0.008) {
////                    qualified_9.add(blkPoint);
////                } else {
//                    Plane zeroPlane = new Plane(subPlane.getNormalVector(), 0);
//                    double distance2 = zeroPlane.getDistance(blkPoint);
//                    boolean isSag = Math.abs(subPlane.getIntercept()) + 0.008 < distance2;
//                    boolean isRaise = Math.abs(subPlane.getIntercept()) - 0.008 > distance2;
//                    if (isSag) {
//                        sag_9.add(blkPoint);
//                    }
//                    if (isRaise) {
//                        raise_9.add(blkPoint);
//                    }
//                }
            }
        }
    }

    //    垂直度和水平度的不合格点
    private static void chooseNotQualified_v(List<BlkPoint> points, Wall wall, double quota) {
        double sagQuota = (double) ((int) (quota / 1000000)) / 1000;
        double raiseQuota = (double) ((int) (quota / 10000) % 100) / 1000;

        List<BlkPoint> result_Sag = new ArrayList<>();
        List<BlkPoint> result_Raise = new ArrayList<>();

        List<BlkPoint> result_Sag_8 = new ArrayList<>();
        List<BlkPoint> result_Raise_8 = new ArrayList<>();

        String coordinate = wall.getCoordinate();
        Map<Double, List<BlkPoint>> map = new TreeMap<>();
        switch (coordinate) {
            case Coordinate.X:
                map = PointUtil.groupByCoordinate(points, Coordinate.DIS_TO_X_AXIS);
                break;
            case Coordinate.Y:
                map = PointUtil.groupByCoordinate(points, Coordinate.DIS_TO_Y_AXIS);
                break;
            case Coordinate.Z:
                map = PointUtil.groupByCoordinate(points, Coordinate.DIS_TO_Z_AXIS);
                break;
        }

//        double disToPlane = wall.getTargetPlane().getDistance(new Point3D(0.0,0.0,0.0));
//        double keyMin = 0;
//        if(disToPlane< Const.Threshold.BLK_MINDISTANCE){
//            keyMin = Math.sqrt(Math.pow(Const.Threshold.BLK_MINDISTANCE, 2) - Math.pow(disToPlane, 2));
//        }
        List<BlkPoint> subPoints = new ArrayList<>();
        for (double key : map.keySet()) {
//            if(key<keyMin){
//                continue;
//            }
            subPoints.addAll(map.get(key));
            if (subPoints.size() < Const.SubSize.subPointsSize / 2)
                continue;

            Plane subPlane = PlaneUtil.optimalPlane(subPoints, coordinate);
            for (BlkPoint point : subPoints) {
//                double quota = PermitDeviation.SMALL;
                if (null != wall.getName() && wall.getName().equals(PlaneName.CEILING)) {
                    continue;
                }
//                double distance = subPlane.getDistance(point);

//                if (distance <= quota) {
//                    point.setType(PointTypeEnum.QUALIFIED.getCode());
//                } else {
                Plane zeroPlane = new Plane(subPlane.getNormalVector(), 0);
                double distance2 = zeroPlane.getDistance(point);
                boolean isSag_v = Math.abs(subPlane.getIntercept()) + sagQuota < distance2;
                boolean isRaise_v = Math.abs(subPlane.getIntercept()) - raiseQuota > distance2;

                if (isSag_v) {
                    result_Sag.add(point);
                }
                if (isRaise_v) {
                    result_Raise.add(point);
                }
                if (!isRaise_v && !isSag_v) {
                    point.setType(PointTypeEnum.QUALIFIED.getCode());
                }
            }

//                if (distance > 0.006) {
//                    Plane zeroPlane = new Plane(subPlane.getNormalVector(), 0);
//                    double distance2 = zeroPlane.getDistance(point);
//                    boolean isSag_v = Math.abs(subPlane.getIntercept()) + 0.006 < distance2;
//                    boolean isRaise_v = Math.abs(subPlane.getIntercept()) - 0.006 > distance2;
//
//                    if (isSag_v) {
//                        result_Sag_8.add(point);
//                    }
//                    if (isRaise_v) {
//                        result_Raise_8.add(point);
//                    }
//                }
//            }
            subPoints.clear();
        }

        Plane plane = wall.getTargetPlane();

        SimpleFilter simpleFilter = new SimpleFilter();
        simpleFilter.filter(result_Raise, result_Raise_8, plane.getDistance(new Point3D(0.0, 0.0, 0.0)), "raise", true, wall);
        simpleFilter.filter(result_Sag, result_Sag_8, plane.getDistance(new Point3D(0.0, 0.0, 0.0)), "sag", true, wall);

        //计算垂直度的合格率

    }

    public static void symmetry(@NonNull Wall wall) {
        String axis = wall.getAxis();
        String coordinate = wall.getCoordinate();
        List<BlkPoint> pointData = wall.getPoints();
        if (Coordinate.Z.equals(coordinate) && Axis.POSITIVE.equals(axis)) {
            for (Point3D point3D : pointData) {
                point3D.setZ(-point3D.getZ());
            }
        } else if (Coordinate.X.equals(coordinate) && Axis.POSITIVE.equals(axis)) {
            for (Point3D point3D : pointData) {
                //以z轴为对称轴
                point3D.setX(-point3D.getX());
                point3D.setY(-point3D.getY());
            }
        } else if (Coordinate.Y.equals(coordinate) && Axis.NEGATIVE.equals(axis)) {
            for (Point3D point3D : pointData) {
                //以z轴为对称轴
                point3D.setX(-point3D.getX());
                point3D.setY(-point3D.getY());
            }
        }
    }
}
