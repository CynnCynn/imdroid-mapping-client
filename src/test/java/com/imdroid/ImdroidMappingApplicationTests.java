package com.imdroid;

import com.imdroid.algorithm.findHole.FindHole;
import com.imdroid.algorithm.kmeans.Kmeans;
import com.imdroid.pojo.bo.*;
import com.imdroid.pojo.dto.StationDataDTO;
import com.imdroid.pojo.entity.BlkPoint;
import com.imdroid.pojo.entity.QuotaData;
import com.imdroid.programSelfStart.TaskDataPrepare;
import com.imdroid.service.TaskDataService;
import com.imdroid.service.TaskService;
import com.imdroid.service.impl.TaskServiceImpl;
import com.imdroid.utils.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.Find;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.imdroid.pojo.bo.Const.Encoding.GBK;
import static com.imdroid.service.impl.TaskServiceImpl.rotate;
import static com.imdroid.utils.PointUtil.filter;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ImdroidMappingApplicationTests {
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskDataService taskDataService;

//    @Test
//    public void convexHull() {
//        List<BlkPoint> blkPoints = pointMapper.findPoint();
//        double[][] data = new double[2][blkPoints.size()];
//        for (int i = 0; i < blkPoints.size(); i++) {
//            BlkPoint blkPoint = blkPoints.get(i);
//            data[0][i] = blkPoint.getX();   //x轴
//            data[1][i] = blkPoint.getY();   //y轴
//        }
////        ImageUtil.scatterPlot(data, "G://test.jpg", 1500, 2000);
//    }
//
//    @Test
//    public void lofTest() {
//        java.text.DecimalFormat df = new java.text.DecimalFormat("#.####");
//
//        ArrayList<DataNode> dpoints = new ArrayList<DataNode>();
//
//        List<Point2D> points = pointMapper.findPointByDistance();
//        for (int i = 0; i < points.size(); i++) {
//            dpoints.add(new DataNode(i + "", new double[]{points.get(i).getX(), points.get(i).getY()}));
//        }
//
//        OutlierNodeDetect lof = new OutlierNodeDetect();
//
//        List<DataNode> nodeList = lof.getOutlierNode(dpoints);
//
//        double[][] data = new double[2][points.size()];
//        for (int i = 0; i < nodeList.size(); i++) {
//            if (nodeList.get(i).getLof() < 1) {
//                data[0][i] = nodeList.get(i).getDimension()[0];
//                data[1][i] = nodeList.get(i).getDimension()[1];
//            }
//        }
////        ImageUtil.scatterPlot(data,"G://test2.jpg",1500,2000);
//    }

//    @Autowired
//    private TaskDataPrepare taskDataPrepare;
//
//    @Test
//    public void analyzeTest() {
//        String folder = Const.Folder.POINT_CLOUD;
////        String folder = "D:\\pointCloud";
//        String fileName = "0415kerenfang"; //20190123-3 pc_20190111182835 2018122856
//        String suffix = ".txt";
//        File file = new File(folder + "/" + fileName + suffix);
//
//        StationDataDTO stationDataDTO = new StationDataDTO();
//        stationDataDTO.setPk(155722573152800001L);
//        stationDataDTO.setStationType(111);
//        stationDataDTO.setStationAlias("station1");
//        stationDataDTO.setActualOrder(1);
//        stationDataDTO.setComplete(false);
//        taskDataPrepare.init("155722573152800001", true);
//        taskDataService.saveStationData(stationDataDTO);
//        taskService.prepareData(stationDataDTO);
//
//        taskService.analyzeTxt(file, Const.Encoding.GBK);
//    }
//

    @Test
    public void analyzeWall() throws Exception {
        String folder = "C:\\Users\\Lenovo\\Documents\\WeChat Files\\Cynn15819422740\\FileStorage\\File\\2019-07";
        String fileName = "No3";
        String suffix = ".txt";
        File file = new File(folder + "/" + fileName + suffix);
        List<BlkPoint> allPoints = PointUtil.blkPointFromTxt(file, Const.Encoding.GBK);
        rotate(allPoints);
//        Plane plane = PlaneUtil.planeFitting(allPoints);
//        List<BlkPoint> qualified_5 = new ArrayList<>();
        double maxX=0.0, maxY=0.0, minX=0.0, minY=0.0,minZ=0.0;

        for(BlkPoint blkPoint:allPoints){
//            if(plane.getDistance(blkPoint)<0.001){
//                qualified_5.add(blkPoint);
//            }
            maxX = blkPoint.getX()>maxX?blkPoint.getX():maxX;
            maxY = blkPoint.getY()>maxY?blkPoint.getY():maxY;
            minX = blkPoint.getX()<minX?blkPoint.getX():minX;
            minY = blkPoint.getY()<minY?blkPoint.getY():minY;
            minZ = blkPoint.getZ()<minZ?blkPoint.getZ():minZ;
        }

        String coordinate = maxX-minX>maxY-minY? Const.Coordinate.X: Const.Coordinate.Y;

//        double flatness = (double) qualified_5.size() / allPoints.size();
        Wall wall = new Wall();
        wall.setPoints(allPoints);
        wall.setName(fileName);
        wall.setAxis(coordinate);
        wall.setImagePath(folder + fileName+ Const.Suffix.JPG);
        wall.setCoordinate(coordinate);
        wall.setImagePath2(folder + fileName+"2"+ Const.Suffix.JPG);
        WallUtil.formatWall(wall,minZ,3333);
        ImageUtil.generateImage(wall);


//        System.out.println(fileName+" "+flatness);
    }
    @Test
    public void analyzeLength() throws Exception {
        String folder ="H:\\";
        String fileName = "12-scan12"; //20190123-3 pc_20190111182835 2018122856
        String suffix = ".txt";
        File file = new File(folder + "/" + fileName + suffix);
        List<BlkPoint> allPoints = PointUtil.blkPointFromTxt(file, Const.Encoding.GBK);
//        分成两个平面
        List<BlkPoint> planePoint1 = new ArrayList<>();
        List<BlkPoint> planePoint2 = new ArrayList<>();
        int flag = 0;
        BlkPoint planeKernel1 = new BlkPoint(), planeKernel2;
        for(BlkPoint blkPoint:allPoints){
            if(flag == 0){
                planePoint1.add(blkPoint);
                planeKernel1 = blkPoint;
                flag = 1;
                continue;
            }
            if(blkPoint.getDistance(planeKernel1) < 2){
                planePoint1.add(blkPoint);
            }
            else
                planePoint2.add(blkPoint);

        }

        double x1_sum = 0.0, y1_sum = 0.0, z1_sum = 0.0;
        double x2_sum = 0.0, y2_sum = 0.0, z2_sum = 0.0;

        for(BlkPoint blkPoint : planePoint1){
            x1_sum += blkPoint.getX();
            y1_sum += blkPoint.getY();
            z1_sum += blkPoint.getZ();
        }

        for(BlkPoint blkPoint : planePoint2){
            x2_sum += blkPoint.getX();
            y2_sum += blkPoint.getY();
            z2_sum += blkPoint.getZ();
        }

        BlkPoint blkPointMid1 = new BlkPoint(x1_sum/planePoint1.size(), y1_sum/planePoint1.size(), z1_sum/planePoint1.size());
        BlkPoint blkPointMid2 = new BlkPoint(x2_sum/planePoint2.size(), y2_sum/planePoint2.size(), z2_sum/planePoint2.size());

        System.out.println(blkPointMid1.getDistance(blkPointMid2)); // "中心点距离"+

        Plane plane1 = PlaneUtil.planeFitting(planePoint1);
        Plane plane2 = PlaneUtil.planeFitting(planePoint2);

        double distance = plane1.getDistance(new Point3D(0.0,0.0,0.0))+
                plane2.getDistance(new Point3D(0.0,0.0,0.0));
        System.out.println(distance); //"长度测试(平面到平面)"+
//        System.out.println("长度测试"+plane1.getDistance(planePoint2.get(0)));
//        System.out.println("长度测试"+plane2.getDistance(planePoint1.get(0)));
        System.out.println(distance*Math.abs(Math.cos(plane1.getAngle(plane2)/180*Math.PI))); // "校正夹角误差后"+
        System.out.println(plane1.getAngle(plane2)); //"夹角"+
//        System.out.println(Math.cos(plane1.getAngle(plane2)/180*Math.PI));
//        System.out.println(Math.cos(Math.PI));    4.9628 4.953
        System.out.println((plane1.getDistance(new Point3D(0.0,0.0,0.0))*Math.abs(Math.cos(plane1.getAngle(plane2)/180*Math.PI))+
                plane2.getDistance(new Point3D(0.0,0.0,0.0)))); // "校正夹角(一边)"+
    }

    @Test
    public void analyzeAngle() throws Exception {
        String folder = Const.Folder.POINT_CLOUD;
        String fileName = "angle"; //20190123-3 pc_20190111182835 2018122856
        String suffix = ".txt";
        File file = new File(folder + "/" + fileName + suffix);
        List<BlkPoint> allPoints = PointUtil.blkPointFromTxt(file, Const.Encoding.GBK);
        Plane plane = PlaneUtil.planeFitting(allPoints);
        double angle = plane.getNormalVector().getAngle(new Vector3D(0, 0, 1));
        System.out.println("角度测试"+angle);
    }

    @Test
    public void analyzeThick() throws Exception {
        String folder = Const.Folder.POINT_CLOUD;
        String fileName = "thick"; //20190123-3 pc_20190111182835 2018122856
        String suffix = ".txt";
        File file = new File(folder + "/" + fileName + suffix);

        List<BlkPoint> allPoints = PointUtil.blkPointFromTxt(file, Const.Encoding.GBK);
//        如果是垂直方向的
//        rotateVertical(allPoints, 90*Math.PI/180);
        rotate(allPoints);
        FindHole.pointsToFile(allPoints, "D:\\study\\python\\fof\\test1.txt");
//        将全部点进行旋转坐标系
        rotateVertical(allPoints, 90*Math.PI/180);
//        log.info("水平旋转完成，旋转了" + deflectionAngleArc / Math.PI * 180 + "度");
        FindHole.pointsToFile(allPoints, "D:\\study\\python\\fof\\test2.txt");
        Map<Double, List<BlkPoint>> map = PointUtil.groupByCoordinate(allPoints, Const.Coordinate.Z_2);

        Map<Double, List<BlkPoint>> Z = FindHole.groupBy(allPoints, Const.Coordinate.Z, 3);
        Double keyMax = 0.0, keyMin = 9999.0;
        for(Double key : Z.keySet()){
            keyMax = (key>keyMax? key: keyMax);
            keyMin = (key<keyMin ? key:keyMin);
        }

        Plane plane1 = PlaneUtil.planeFitting(Z.get(keyMax));
        Plane plane2 = PlaneUtil.planeFitting(Z.get(keyMin));

        double distance = Math.abs(plane1.getDistance(new Point3D(0.0,0.0,0.0))-plane2.getDistance(new Point3D(0.0,0.0,0.0)));
        System.out.println("厚度测试"+distance);

//        System.out.println(plane.getNormalVector().getAngle(new Vector3D(0, 0, 1)));
//
////        List <BlkPoint> closePoints = new ArrayList<>();
//////        寻找接近平面的点
////        for(BlkPoint blkPoint:allPoints){
////            double distance = plane.getDistance(blkPoint);
////            if(distance < 0.01){
////                closePoints.add(blkPoint);
////            }
////        }
////
////        String coordinate = Const.Coordinate.Y;
//
//
//        System.out.println(closePoints.size());
//
////        寻找距离平面两个方向最远的两点
//        Plane zeroPlane = new Plane(plane.getNormalVector(), 0);
//        double sagDis=0.0, raiseDis = 0.0;
//        BlkPoint sagPoint = new BlkPoint(0.0, 0.0, 0.0);
//        BlkPoint raisePoint = new BlkPoint(0.0, 0.0, 0.0);
//        for(BlkPoint blkPoint:allPoints){
//            plane.getDistance(blkPoint);
//            double distance = zeroPlane.getDistance(blkPoint);
//            if(Math.abs(plane.getIntercept()) - distance < 0){
////                凹
//                sagDis = Math.min(Math.abs(plane.getIntercept()) - distance, sagDis);
//                sagPoint = blkPoint;
//
//            }
//            else{
////                凸
//                raiseDis = Math.max(Math.abs(plane.getIntercept()) - distance, raiseDis);
//                raisePoint = blkPoint;
//            }
//        }
//
//        if(Math.abs(raisePoint.getX()-sagPoint.getX())<0.001 &&
//                Math.abs(raisePoint.getY()-sagPoint.getY())<0.001){
//            double thick = Math.abs(raisePoint.getZ()-sagPoint.getZ());
//            System.out.println(thick);
//        }
//        if(Math.abs(raisePoint.getZ()-sagPoint.getZ())<0.001 &&
//                Math.abs(raisePoint.getY()-sagPoint.getY())<0.001){
//            double thick = Math.abs(raisePoint.getX()-sagPoint.getX());
//            System.out.println(thick);
//        }
//        if(Math.abs(raisePoint.getX()-sagPoint.getX())<0.001 &&
//                Math.abs(raisePoint.getZ()-sagPoint.getZ())<0.001){
//            double thick = Math.abs(raisePoint.getY()-sagPoint.getY());
//            System.out.println(thick);
//        }
//
//        System.out.println(sagPoint.getX() +" "+ raisePoint.getX());
//        System.out.println(sagPoint.getY() +" "+ raisePoint.getY());
//        System.out.println(sagPoint.getZ() +" "+ raisePoint.getZ());


    }

    public static <T extends BlkPoint> void rotateVertical(@NonNull List<T> pointData, double rotateAngle) {
        double newX, newZ;

        for (T point3D : pointData) {
            double x = point3D.getX();
            double z = point3D.getZ();

            newX = x * Math.cos(rotateAngle) - z * Math.sin(rotateAngle);
            newZ = x * Math.sin(rotateAngle) + z * Math.cos(rotateAngle);

            point3D.setX(newX);
            point3D.setZ(newZ);
            //设置所有点的xyz到小数点后几位 默认4位
            point3D.format();
        }
    }

//    @Test
//    public void walltest() throws Exception {
//        String folder = Const.Folder.POINT_CLOUD;
//        String fileName = "test"; //20190123-3 pc_20190111182835 2018122856
//        String suffix = ".txt";
//        File file = new File(folder + "/" + fileName + suffix);
//        List<BlkPoint> allPoints = PointUtil.blkPointFromTxt(file, GBK);
//        Wall wall = new Wall();
//        wall.setPoints(allPoints);
//        Plane plane = PlaneUtil.planeFitting(allPoints);
//        Plane targetPlane = PlaneUtil.optimalPlane(allPoints, Const.Coordinate.Z);
//        wall.setPlane(plane);
//        wall.setTargetPlane(targetPlane);
//        Vector3D normalVector = plane.getNormalVector();
//        double angle = normalVector.getAngle(new Vector3D(0, 0, 1));
//        System.out.println(angle);
//    }
//    @Test
//    public void analyzeTest() {
//        String folder = Const.Folder.POINT_CLOUD;
//        String fileName = "pc_20190220173534"; //20190123-3 pc_20190111182835 2018122856
//        String suffix = ".txt";
//        File file = new File(folder + "/" + fileName + suffix);
//
//        StationDataDTO stationDataDTO = new StationDataDTO();
//        stationDataDTO.setPk(155179851232500001L);
//        stationDataDTO.setStationType(111);
//        stationDataDTO.setStationAlias("station1");
//        stationDataDTO.setActualOrder(1);
//        stationDataDTO.setComplete(false);
//        taskDataService.saveStationData(stationDataDTO);
//        taskService.prepareData(stationDataDTO);
//
//        taskService.analyzeTxt(file, Const.Encoding.GBK);
//    }


//    @Test
//    public void holeTest() throws Exception{
//        try {
//            String folder = Const.Folder.POINT_CLOUD;
//            String fileName = "pc_20190215154642"; //pc_20190111182835
//            String suffix = ".txt";
//            File file = new File(folder + "/" + fileName + suffix);
//            List<BlkPoint> allPoints = PointUtil.blkPointFromTxt(file, Const.Encoding.GBK);
//            rotate(allPoints);
////            List<Point2D> bottom = PointUtil.reduceDimension(allPoints, Const.Coordinate.Z);
////            List<Double> parameters = LineUtil.lineRansac(bottom, allPoints.size() / 10);
////            double deflectionAngleArc = Math.atan(-parameters.get(1) / parameters.get(0));
////            //将全部点进行旋转坐标系
////            PointUtil.rotateHorizontally(allPoints, deflectionAngleArc);
////            System.out.println("水平旋转完成，旋转了" + deflectionAngleArc/Math.PI*180 + "度");
////            FindHole.pointsToFile(allPoints, "D:\\study\\python\\fof\\windowsPoints.txt");
//
//            Map<Double, List<BlkPoint>> map1 = PointUtil.groupByCoordinate(allPoints, Const.Coordinate.Z);
//            int ceilingSize = 0, floorSize = 0; double ceilingKey = 0, floorKey = 0;
//            for(double key:map1.keySet()) {
//                int pointNum = map1.get(key).size();
//                if (map1.get(key).get(0).getZ() < 0 && pointNum > floorSize) {
//                    floorSize = pointNum;
//                    floorKey = key;
//                } else if (map1.get(key).get(0).getZ() > 0 && pointNum > ceilingSize) {
//                    ceilingSize = pointNum;
//                    ceilingKey = key;
//                }
//            }
//            double ceiling = FindHole.computeAver(map1.get(ceilingKey), Const.Coordinate.Z);
//            double floor = FindHole.computeAver(map1.get(floorKey), Const.Coordinate.Z);
//
//
////        取墙面数据点
//            List<BlkPoint> wallPoints = FindHole.getWallPoints(allPoints, ceiling - Const.Threshold.CEILING_THRESHOLD, floor+ Const.Threshold.FLOOR_THRESHOLD);
//            //根据坐标轴对点集分组
//            Map<Double, List<BlkPoint>> map = PointUtil.groupByCoordinate(wallPoints, Const.Coordinate.PHI);
//
//
//            List<BlkPoint> datumPoints = FindHole.cycle(map, ceiling - Const.Threshold.SAFE_THRESHOLD_UP, ceiling - Const.Threshold.SAFE_THRESHOLD_LOW);
//
////            标记飘出点
//            FindHole.setOuterPoints(allPoints, datumPoints);
//            FindHole.pointsToFile(datumPoints, "D:\\study\\python\\fof\\datumPoints.txt");
//
//
//            List<Double> holeMark = FindHole.findHoleMark(map, datumPoints, ceiling, floor);
//            List<QuotaData> quotaDataList = FindHole.computeHole(map, holeMark, datumPoints, floor);
//            for (QuotaData quotaData : quotaDataList) {
//                log.info(quotaData.toString());
//                System.out.println(quotaData.toString());
//            }
//            List<BlkPoint> outerPoints = new ArrayList<>();
//            List<BlkPoint> doorsPoints = new ArrayList<>();
//            List<BlkPoint> windowsPoints = new ArrayList<>();
//            for(double key:map.keySet()){
//                for(BlkPoint blkPoint:map.get(key)){
//                    if(blkPoint.getType()==6)
//                        outerPoints.add(blkPoint);
//                    if(blkPoint.getType()==4)
//                        doorsPoints.add(blkPoint);
//                    if(blkPoint.getType()==5)
//                        windowsPoints.add(blkPoint);
//                }
//            }
//            FindHole.pointsToFile(windowsPoints, "D:\\study\\python\\fof\\windowsPoints.txt");
//            FindHole.pointsToFile(doorsPoints, "D:\\study\\python\\fof\\doorsPoints.txt");
//            FindHole.pointsToFile(outerPoints, "D:\\study\\python\\fof\\outerPoints.txt");
//        }catch (Exception e)
//        {
//            System.out.println(e.toString());
//        }
//
//    }

//    @Test
//    public void planeTest() throws Exception {
//        String folder = "C:\\Mapping";
//        String fileName = "zhu1";
//        String suffix = ".txt";
//        File file = new File(folder + "\\" + fileName + suffix);
//        List<BlkPoint> points = PointUtil.blkPointFromTxt(file, Const.Encoding.GBK, 6);
//        Wall wall = new Wall();
//        wall.setPoints(points);
//        wall.setAxis(Const.Axis.NEGATIVE);
//        wall.setCoordinate(Const.Coordinate.X);
//
//        WallUtil.findBound(wall);
////        Plane optimalPlane = PlaneUtil.getOptimalPlane(wall);
////        System.out.println(optimalPlane);
//    }

//@Test
//public void uploadTest() {
//    File dataFolder = new File(Const.Folder.DATA);
//    File[] dataFiles = dataFolder.listFiles();
//    String url = Const.ServerAddress.MAPPING + "/taskData/uploadResult.do";
//    if (null != dataFiles && dataFiles.length > 0) {
//        for (File data : dataFiles) {
//            boolean isUploadSuccess = HttpClientUtil.uploadFile(url, data);
//        }
//    }

//}

}
