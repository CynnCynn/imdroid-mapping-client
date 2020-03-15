package com.imdroid.algorithm.simpleFilter;

import com.imdroid.enums.PointTypeEnum;
import com.imdroid.pojo.bo.Const;
import com.imdroid.pojo.bo.Point3D;
import com.imdroid.pojo.bo.Wall;
import com.imdroid.pojo.entity.BlkPoint;
import com.imdroid.utils.PointUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SimpleFilter {
    private double dis;

    public void filter(List<BlkPoint> points_5, List<BlkPoint> points_9, double distance3, String type, boolean isVer, Wall wall) {

//        显示所有不合格的点
//        for(BlkPoint point:points_5){
//            if (type.equals("raise")) {
//                if (!isVer)
//                    point.setType(PointTypeEnum.RAISE_FLAT.getCode());
//                else {
//                    if (PointTypeEnum.RAISE_FLAT.getCode().equals(point.getType()))
//                        point.setType(PointTypeEnum.RAISE.getCode());
//                    else
//                        point.setType(PointTypeEnum.RAISE_VERTICAL.getCode());
//                }
//            } else {
//                if (!isVer)
//                    point.setType(PointTypeEnum.SAG_FLAT.getCode());
//                else {
//                    if (PointTypeEnum.SAG_FLAT.getCode().equals(point.getType()))
//                        point.setType(PointTypeEnum.SAG.getCode());
//                    else
//                        point.setType(PointTypeEnum.SAG_VERTICAL.getCode());
//                }
//            }
//        }
//        显示所有不合格的点
//        ----------------------------------------------------------------------
//        9带5
//        for (BlkPoint point : points_9) {
//            if (isNotQualified(points_5, point, distance3)) {
//                for (BlkPoint point1 : points_5) {
//                    if (point1.getDistance(point) < 1 * dis)
//                        if (type.equals("raise"))
//                        {
//                            if (!isVer)
//                                point1.setType(PointTypeEnum.RAISE_FLAT.getCode());
//                            else {
//                                if(PointTypeEnum.RAISE_FLAT.getCode().equals(point1.getType()))
//                                    point1.setType(PointTypeEnum.RAISE.getCode());
//                                else
//                                    point1.setType(PointTypeEnum.RAISE_VERTICAL.getCode());
//                            }
//                        } else{
//                            if (!isVer)
//                                point1.setType(PointTypeEnum.SAG_FLAT.getCode());
//                            else {
//                                if(PointTypeEnum.SAG_FLAT.getCode().equals(point1.getType()))
//                                    point1.setType(PointTypeEnum.SAG.getCode());
//                                else
//                                    point1.setType(PointTypeEnum.SAG_VERTICAL.getCode());
//                            }
//                        }
//                }
//            }
//        }
//        ------------------------------------------------------------

//
//        统计区域中不合格点
        double flatness = (double) points_5.size() / wall.getPoints().size();
        Map<Integer, List<BlkPoint>> noQualified_5 = groupByGrid(points_5, wall);
        Map<Integer, List<BlkPoint>> wallPoints = groupByGrid(wall.getPoints(), wall);

        for (Integer key : wallPoints.keySet()) {
            if (noQualified_5.get(key) != null) {
                double gridFaltness = (double) noQualified_5.get(key).size() / wallPoints.get(key).size();
//                if (gridFaltness > flatness) {

////                临近8个小方格
//                    for (int i = -1; i <= 1; i++) {
//                        for (int j = -1; j <= 1; j++) {
//                            Integer tempKey = key + i * 100 + j;
//                            if (noQualified_5.get(tempKey) != null) {
                                for (BlkPoint point : noQualified_5.get(key)) {
                                    if (type.equals("raise")) {
                                        if (!isVer)
                                            point.setType(PointTypeEnum.RAISE_FLAT.getCode());
                                        else {
                                            if (PointTypeEnum.RAISE_FLAT.getCode().equals(point.getType()))
                                                point.setType(PointTypeEnum.RAISE.getCode());
                                            else
                                                point.setType(PointTypeEnum.RAISE_VERTICAL.getCode());
                                        }
                                    } else {
                                        if (!isVer)
                                            point.setType(PointTypeEnum.SAG_FLAT.getCode());
                                        else {
                                            if (PointTypeEnum.SAG_FLAT.getCode().equals(point.getType()))
                                                point.setType(PointTypeEnum.SAG.getCode());
                                            else
                                                point.setType(PointTypeEnum.SAG_VERTICAL.getCode());
                                        }
                                    }
//                                }
//                            }
//                        }
//                    }
                }
            }
//                    }
        }
    }


    public boolean isNotQualified(List<BlkPoint> list_5, BlkPoint blkPoint, double distance) {
        int count = 0;
        dis = PointUtil.getRadiusByDistance(blkPoint, distance);
        for (BlkPoint point : list_5) {
            if (point.getDistance(new Point3D(0.0, 0.0, 0.0)) < 20 * distance) {
                count++;
            }
            if (count >= 3) {
                return true;
            }
        }
        return false;
    }

    public Map<Integer, List<BlkPoint>> groupByGrid(List<BlkPoint> blkPoints, Wall wall) {
        Map<Integer, List<BlkPoint>> grid_points = new TreeMap<>();
        double dis = 0.2;

        for (BlkPoint blkPoint : blkPoints) {
            int i, j;
            Integer key = 0;
            if (wall.getCoordinate().equals(Const.Coordinate.X)) {
                i = (int) ((blkPoint.getY() - wall.getBound()[1][0]) / dis);
                j = (int) ((blkPoint.getZ() - wall.getBound()[2][0]) / dis);
                key += i * 100 + j;
            }
            if (wall.getCoordinate().equals(Const.Coordinate.Y)) {
                i = (int) ((blkPoint.getX() - wall.getBound()[0][0]) / dis);
                j = (int) ((blkPoint.getZ() - wall.getBound()[2][0]) / dis);
                key += i * 100 + j;
            }
            if (wall.getCoordinate().equals(Const.Coordinate.Z)) {
                i = (int) ((blkPoint.getX() - wall.getBound()[0][0]) / dis);
                j = (int) ((blkPoint.getY() - wall.getBound()[1][0]) / dis);
                key += i * 100 + j;
            }
            List<BlkPoint> subList = grid_points.get(key);
            if (null == subList) {
                subList = new ArrayList<>();
            }
            subList.add(blkPoint);
            grid_points.put(key, subList);
        }
        return grid_points;
    }
}
