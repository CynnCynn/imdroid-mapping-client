package com.imdroid.utils;


import com.imdroid.algorithm.dbscan.DBscan;
import com.imdroid.algorithm.lof.OutlierNodeDetect;
import com.imdroid.enums.PointTypeEnum;
import com.imdroid.pojo.bo.BusinessException;
import com.imdroid.pojo.bo.DataNode;
import com.imdroid.pojo.bo.Point2D;
import com.imdroid.pojo.bo.Point3D;
import com.imdroid.pojo.entity.BlkPoint;
import com.imdroid.programSelfStart.statusNotificationUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.imdroid.pojo.bo.Const.Coordinate;

/**
 * @Description:点工具类
 * @Author: iceh
 * @Date: create in 2018-09-26 16:27
 * @Modified By:
 */
@Slf4j
public class PointUtil {
    public static int orientation(BlkPoint p, BlkPoint q, BlkPoint r) {
        double val = (q.getY() - p.getY()) * (r.getX() - q.getX()) -
                (q.getX() - p.getX()) * (r.getY() - q.getY());

        if (val == 0) return 0;
        return (val > 0) ? 1 : 2;
    }

    //使用Jarvis算法求离散点的凸包
    public static List<BlkPoint> convexHull(List<BlkPoint> points) {
        if (points.size() < 3) return null;

        List<BlkPoint> hull = new ArrayList<>();

        int l = 0;
        for (int i = 1; i < points.size(); i++)
            if (points.get(i).getX() < points.get(l).getX())
                l = i;

        int p = l, q;
        do {
            hull.add(points.get(p));
            q = (p + 1) % points.size();

            for (int i = 0; i < points.size(); i++) {
                if (orientation(points.get(p), points.get(i), points.get(q))
                        == 2)
                    q = i;
            }
            p = q;
        } while (p != l);
        return hull;
    }

    /**
     * 根据指定坐标类型 对三维点云数据分组
     *
     * @param points     三维点云数据
     * @param coordinate 坐标类型
     * @param <T>        三维点云数据格式
     * @return
     */
    public static <T extends BlkPoint> Map<Double, List<T>> groupByCoordinate(@NonNull List<T> points, String coordinate) {
        //借助TreeMap的自动排序
        Map<Double, List<T>> map = new TreeMap<>();
        //根据坐标类型及坐标值分组
        for (T point : points) {
            double coord = 0d;
            if (Coordinate.X.equals(coordinate)) {
                coord = point.getX();
            }
            if (Coordinate.Y.equals(coordinate)) {
                coord = point.getY();
            }
            if (Coordinate.Z.equals(coordinate)) {
                coord = BigDecimalUtil.scale(point.getZ(),2);
            }
            if (Coordinate.PHI.equals(coordinate)) {
                coord = point.getPhi();
            }
            if (Coordinate.PHI_2.equals(coordinate)) {
                coord = BigDecimalUtil.scale(point.getPhi()%90/200, 1);
            }
            if (Coordinate.THETA.equals(coordinate)) {
                coord = BigDecimalUtil.scale(point.getTheta(), 2);
            }
            if (Coordinate.DIS_TO_X_AXIS.equals(coordinate)){
                coord = BigDecimalUtil.scale(Math.sqrt(Math.pow(point.getY(),2)+Math.pow(point.getZ(),2)),2);
            }
            if (Coordinate.DIS_TO_Y_AXIS.equals(coordinate)){
                coord = BigDecimalUtil.scale(Math.sqrt(Math.pow(point.getX(),2)+Math.pow(point.getZ(),2)),2);
            }
            if (Coordinate.DIS_TO_Z_AXIS.equals(coordinate)){
                coord = BigDecimalUtil.scale(Math.sqrt(Math.pow(point.getX(),2)+Math.pow(point.getY(),2)),2);
            }
            if (Coordinate.X_2.equals(coordinate)){
                coord = BigDecimalUtil.scale(point.getX(),2);
            }
            if (Coordinate.Y_2.equals(coordinate)){
                coord = BigDecimalUtil.scale(point.getY(),2);
            }
            if (Coordinate.Z_2.equals(coordinate)){
                coord = BigDecimalUtil.scale(point.getZ(),4);
            }
            List<T> subList = map.get(coord);
            if (null == subList) {
                subList = new ArrayList<>();
            }
            subList.add(point);
            map.put(coord, subList);
        }
        return map;
    }

    /**
     * 从文件中 读取Blk360的点云数据
     *
     * @param file     文件
     * @param encoding 编码格式
     * @return 点集
     */
    public static List<BlkPoint> blkPointFromTxt(@NonNull File file, String encoding) throws Exception{
        List<BlkPoint> tmpPoints = new ArrayList<>();
        List<BlkPoint> points = new ArrayList<>();
//        List<BlkPoint> points1 = new ArrayList<>();
//        List<BlkPoint> points2 = new ArrayList<>();
//        List<BlkPoint> points3 = new ArrayList<>();
//        List<BlkPoint> points4 = new ArrayList<>();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (file.isFile() && file.exists()) { //判断文件是否存在
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), encoding)); //防止编码格式影响读取结果
            String rowRecord;
            int flag = 0;

            while ((rowRecord = bufferedReader.readLine()) != null) {
                BlkPoint blkPoint = filter(rowRecord);
                if (null != blkPoint) {
                    tmpPoints.add(blkPoint);

//                    double quadrant[] = {0, 90, 180, 270, 360}; // 分象限
//
//                    if (blkPoint.getPhi() < quadrant[1] && blkPoint.getPhi() > quadrant[0])
//                        points1.add(blkPoint);
//                    else if (blkPoint.getPhi() < quadrant[2] && blkPoint.getPhi() > quadrant[1])
//                        points2.add(blkPoint);
//                    else if (blkPoint.getPhi() < quadrant[3] && blkPoint.getPhi() > quadrant[2])
//                        points3.add(blkPoint);
//                    else if (blkPoint.getPhi() < quadrant[4] && blkPoint.getPhi() > quadrant[3])
//                        points4.add(blkPoint);
                }
            }

//            重新设定Z轴的0点
            double sumZ = 0.0;
            for(BlkPoint blkPoint:tmpPoints){
                sumZ += blkPoint.getZ();
            }
            double averageZ = sumZ/tmpPoints.size();
            for(BlkPoint blkPoint:tmpPoints){
                blkPoint.setZ(blkPoint.getZ()-averageZ);
            }

//                后期可以分为8个块排序
//            Collections.sort(points1, new Comparator<BlkPoint>() {
//                @Override
//                public int compare(BlkPoint o1, BlkPoint o2) {
//                    return o1.getTheta().compareTo(o2.getTheta());
//                }
//            });
//            Collections.sort(points2, new Comparator<BlkPoint>() {
//                @Override
//                public int compare(BlkPoint o1, BlkPoint o2) {
//                    return o1.getTheta().compareTo(o2.getTheta());
//                }
//            });
//            Collections.sort(points3,new Comparator<BlkPoint>() {
//                @Override
//                public int compare(BlkPoint o1, BlkPoint o2) {
//                    return o1.getTheta().compareTo(o2.getTheta());
//                }
//            });
//            Collections.sort(points4, new Comparator<BlkPoint>() {
//                @Override
//                public int compare(BlkPoint o1, BlkPoint o2) {
//                    return o1.getTheta().compareTo(o2.getTheta());
//                }
//            });
//
//            points.addAll(points1);
//            points.addAll(points2);
//            points.addAll(points3);
//            points.addAll(points4);
            Map<Double, List<BlkPoint>> map = PointUtil.groupByCoordinate(tmpPoints, Coordinate.THETA);
            for (Double key : map.keySet()) {
                points.addAll(map.get(key));
            }
            if (points.size() < 3) { //15000
                //语音提示  扫描数据异常，请重新扫描
                Thread.sleep(2000);//让导出完成的语音先播完
                statusNotificationUtil.updateStatus(205);
                log.info("扫描数据缺失，请重新扫描");
                throw new BusinessException("点云文件不正常");
            }
        } else {
            throw new BusinessException(file + "不是可解析文件，请使用可解析文件");
        }
        return points;
    }

    /**
     * 对数据进行过滤
     *
     * @param pointStr 点
     * @return 点
     */
    public static BlkPoint filter(String pointStr) {
        String[] point = pointStr.split(",");
        double x = Double.valueOf(point[0]);
        double y = Double.valueOf(point[1]);
        double z = Double.valueOf(point[2]);
        if (0 == x && 0 == y & 0 == z) {
            return null;
        }
        double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
//        点到坐标原点的距离
        double r = Math.sqrt(Math.pow(distance, 2) + Math.pow(z, 2));
//        与z轴的夹角
        double theta = Math.acos(z / r) / Math.PI * 180;



        BlkPoint blkPoint = null;

//            Long intensity = Long.valueOf(point[3]);
            Long intensity = 1L;
            blkPoint = new BlkPoint();
            blkPoint.setType(PointTypeEnum.BASIS.getCode());
            blkPoint.setX(x);
            blkPoint.setY(y);
            blkPoint.setZ(z);
            blkPoint.setR(distance);
            blkPoint.setTheta(theta);
//        水平方向上的夹角
            setPhi(blkPoint);

            blkPoint.setIntensity(intensity);
        return blkPoint;
    }

    public static void setPhi(BlkPoint blkPoint){
        double phi;
        if (Math.abs(blkPoint.getX())<0.0001){
            if(blkPoint.getY()>0){
                phi=90;
            }
            else
                phi=270;
        }
        else {
            phi = Math.atan(blkPoint.getY() / blkPoint.getX()) / Math.PI * 180;
//        按象限处理atan后的结果
            if (blkPoint.getX() < 0)
                phi += 180;
            else if (blkPoint.getX() > 0 && blkPoint.getY() < 0)
                phi += 360;
        }
        blkPoint.setPhi(BigDecimalUtil.scale(phi, 1));
    }

    public static double getDistance(BlkPoint blkPoint1, BlkPoint blkPoint2){
        return Math.sqrt(Math.pow((blkPoint1.getX() - blkPoint2.getX()), 2) +
                Math.pow((blkPoint1.getY() - blkPoint2.getY()), 2));
    }

    /**
     * 将三维点数据 变为 二维点数据
     *
     * @param pointData        三维点数据
     * @param reduceCoordinate 要排除的坐标
     * @param <T>
     * @return
     */
    public static <T extends Point2D, S extends Point3D> List<T> reduceDimension(@NonNull List<S> pointData, String reduceCoordinate) {
        List<T> points = new ArrayList<>();
        for (S point3D : pointData) {
            if (Coordinate.X.equals(reduceCoordinate)) {
                T point2D = (T) new Point2D();
                point2D.setX(point3D.getY());
                point2D.setY(point3D.getZ());
                points.add(point2D);
            }
            if (Coordinate.Y.equals(reduceCoordinate)) {
                T point2D = (T) new Point2D();
                point2D.setX(point3D.getX());
                point2D.setY(point3D.getZ());
                points.add(point2D);
            }
            if (Coordinate.Z.equals(reduceCoordinate)) {
                T point2D = (T) new Point2D();
                point2D.setX(point3D.getX());
                point2D.setY(point3D.getY());
                points.add(point2D);
            }

        }
        return points;
    }

    /**
     * 将数据垂直于z轴旋转 并对数据进行去小数点的处理
     *
     * @param pointData
     * @param rotateAngle
     * @param <T>
     */
    public static <T extends BlkPoint> void rotateHorizontally(@NonNull List<T> pointData, double rotateAngle) {
        double newX, newY, newPhi;

        for (T point3D : pointData) {
            double x = point3D.getX();
            double y = point3D.getY();
            double phi = point3D.getPhi();
            newX = x * Math.cos(rotateAngle) - y * Math.sin(rotateAngle);
            newY = x * Math.sin(rotateAngle) + y * Math.cos(rotateAngle);
            newPhi = (phi + rotateAngle / Math.PI * 180 + 360) % 360; // 水平角度变化

            point3D.setX(newX);
            point3D.setY(newY);
            point3D.setPhi(newPhi);
            //设置所有点的xyz到小数点后几位 默认4位
            point3D.format();
//            设置phi角的精度
            point3D.phi_format(1);
        }
    }

    //三维版
    public static <T extends Point3D> List<DataNode> lofNodeList(@NonNull List<T> pointData) {
        ArrayList<DataNode> dpoints = new ArrayList<>();
        for (int i = 0; i < pointData.size(); i++) {
            dpoints.add(new DataNode(i + "", new double[]{pointData.get(i).getX(), pointData.get(i).getY(), pointData.get(i).getZ()}));
        }
        OutlierNodeDetect lof = new OutlierNodeDetect();
        return lof.getOutlierNode(dpoints);
    }

    public static <T extends Point3D> void outlierFilter(@NonNull List<T> pointData, double threshold) {
        List<DataNode> nodeList = lofNodeList(pointData);
        for (int i = 0; i < nodeList.size(); i++) {
            if (nodeList.get(i).getLof() > threshold) {
                BlkPoint temp = new BlkPoint(nodeList.get(i).getDimension()[0], nodeList.get(i).getDimension()[1], nodeList.get(i).getDimension()[2]);
                pointData.remove(temp);
            }
        }
    }

    public static void outlierFilterByDBscan(@NonNull List<BlkPoint> pointData, double distance) {
        DBscan dBscan = new DBscan();
        pointData = dBscan.process(pointData, distance);
    }

    public static double getRadiusByDistance(BlkPoint point3D, double distance) {
        double l = Math.sqrt(point3D.getX() * point3D.getX() + point3D.getY() * point3D.getY() + point3D.getZ() * point3D.getZ());
        return Math.abs(distance * Math.tan(Math.atan(l / distance) + 0.1) - l) * 0.15 + 0.02;
    }

}
