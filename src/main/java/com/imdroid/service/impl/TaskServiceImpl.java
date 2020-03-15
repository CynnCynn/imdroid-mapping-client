package com.imdroid.service.impl;

import com.alibaba.fastjson.JSON;
import com.imdroid.algorithm.computeCorner.ComputeCorner;
import com.imdroid.algorithm.findHole.FindCycle;
import com.imdroid.algorithm.findHole.FindHole;
import com.imdroid.dao.mapper.*;
import com.imdroid.enums.AssociateEnum;
import com.imdroid.enums.ImageEnum;
import com.imdroid.enums.PointTypeEnum;
import com.imdroid.enums.QuotaEnum;
import com.imdroid.pojo.bo.*;
import com.imdroid.pojo.bo.Const.*;
import com.imdroid.pojo.dto.StationDataDTO;
import com.imdroid.pojo.dto.TaskDataDTO;
import com.imdroid.pojo.dto.WallDataDTO;
import com.imdroid.pojo.entity.*;
import com.imdroid.programSelfStart.BluetoothSerialPort;
import com.imdroid.programSelfStart.DisplaySerialPort;
import com.imdroid.programSelfStart.TaskDataPrepare;
import com.imdroid.programSelfStart.statusNotificationUtil;
import com.imdroid.service.TaskDataService;
import com.imdroid.service.TaskService;
import com.imdroid.utils.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.json.impl.JSONObject;
import org.jfree.data.xy.DefaultXYDataset;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;
import java.util.List;

import static com.imdroid.algorithm.computeCorner.ComputeCorner.computeCorner;


/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2018-09-04 15:11
 * @Modified By:
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskServiceImpl implements TaskService {
    private TaskDataDTO taskDataDTO;
    private StationDataDTO stationDataDTO;
    private OverView overView;
    private static double ceiling = 0.0;
    private static double floor = 0.0;

    private static double quota = 3030303;

    private final TaskDataPrepare taskDataPrepare;

    private final TaskDataMapper taskDataMapper;
    private final StationDataMapper stationDataMapper;
    private final WallDataMapper wallDataMapper;
    private final QuotaDataMapper quotaDataMapper;

    private volatile boolean calculation;
    private final QuotaMapper quotaMapper;
    /**
     * @param file     文件
     * @param encoding 编码格式
     * @return
     */
//    @Async("calculationExecutor")
    public void analyzeTxt(@NonNull File file, String encoding) {
        try {
            calculation = true;
//            Quota newQuota = quotaMapper.selectByQuotaType(QuotaEnum.VERTICAL.getCode());
//            if(newQuota != null) {
//                System.out.println(newQuota.toString()); // 垂直度
//                quota = newQuota.getDefaults()*10000 + newQuota.getDefaults()*1000000 + quota % 10000;
//            }
//
//            Quota newQuota1 = quotaMapper.selectByQuotaType(QuotaEnum.FLATNESS.getCode());
//            if(newQuota1 != null) {
//                System.out.println(newQuota1.toString()); // 平整度
//                quota = quota + newQuota1.getDefaults() + newQuota1.getDefaults()*100 - quota % 10000;
//            }

            System.out.println(quota);

            this.overView = new OverView();
            statusNotificationUtil.updateStatus(104);// 开始计算
            log.info("计算" + stationDataDTO.getStationAlias() + "开始");
            List<BlkPoint> allPoints = PointUtil.blkPointFromTxt(file, encoding);
//            旋转坐标点
            rotate(allPoints);
//            计算天花地板高度
            computeCeilingFloor(allPoints);
//            标记门洞，窗洞，飘出点
            List<BlkPoint> datumPoints = findHole(allPoints, quota);
//            计算洞口进深、开间
            setDepthBay(datumPoints, stationDataDTO.getQuotaDataList());

            String filename = Folder.IMAGE + "/" + taskDataDTO.getPk() + "_" +
                    stationDataDTO.getActualOrder() + "/" + "overView" + Suffix.JPG;
            overView.setImagePath(filename);

            List<BlkPoint> innerPoints = new ArrayList<>();
            for (BlkPoint blkPoint : allPoints) {
//                飘出点暂时先参与不计算
                if (!PointTypeEnum.OUT_POINT.getCode().equals(blkPoint.getType())) {
                    innerPoints.add(blkPoint);
                }
            }
            StationCalculation zCalculation = wallExtract(innerPoints, Coordinate.Z);
            StationCalculation yCalculation = wallExtract(zCalculation.getRemainPoints(), Coordinate.Y);
            StationCalculation xCalculation = wallExtract(yCalculation.getRemainPoints(), Coordinate.X);


//            开间进深放在俯视图正中间
            double xMax = 0, xMin = xMax;
            double yMax = 0, yMin = yMax;
            for (Text text : overView.getTexts()) {
//            System.out.println(text.getText());
                double currentX = text.getX();
                double currentY = text.getY();
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
            }

            Text text = new Text();
            text.setX((xMax + xMin) / 2);
            text.setY((yMax + yMin) / 2 + 0.1);
            text.setText("开间:" + BigDecimalUtil.scale(stationDataDTO.getBay(), 3) +
                    " 进深:" + BigDecimalUtil.scale(stationDataDTO.getDepth(), 3));
            overView.addTexts(text);

            ImageUtil.scatterPlot(overView, 0.35);


            Wall wall = new Wall();
            wall.setImagePath(overView.getImagePath());
            wall.setImagePath2(overView.getImagePath());
            wall.setName("overView");
            WallDataDTO wallDataDTO = new WallDataDTO();
            BeanUtils.copyProperties(wall, wallDataDTO);
            stationDataDTO.addWallDataDTO(wallDataDTO);
            overView = null;

            //将墙面统一起来做处理
            StationCalculation stationCalculation = new StationCalculation();
            List<Wall> walls = stationCalculation.getWalls();
            walls.addAll(zCalculation.getWalls());
            walls.addAll(xCalculation.getWalls());
            walls.addAll(yCalculation.getWalls());

//            将中间结果写入文件
            resultToFile(walls);
//            计算阴阳角
            List<QuotaData> cornerQuotaList = ComputeCorner.computeCorner(walls, datumPoints);
            stationDataDTO.addQuotaDataList(cornerQuotaList);

//            计算方正度
            for (Wall xWall : xCalculation.getWalls()) {
                for (Wall yWall : yCalculation.getWalls()) {
                    double angle = xWall.getPlane().getAngle(yWall.getPlane());
                    Long detectionPoints = (long) xWall.getPoints().size();
                    long passPoints = 0L;
                    for (BlkPoint point : xWall.getPoints()) {
                        if (point.getType().equals(PointTypeEnum.QUALIFIED.getCode())) {
                            passPoints++;
                        }
                    }
//                    Long passPoints = Math.round(0.8 * detectionPoints);
                    QuotaData xQuotaData = new QuotaData(angle, QuotaEnum.SQUARE.getCode(), detectionPoints, passPoints, AssociateEnum.WALL_DATA.getCode(), yWall.getName());
                    xWall.addQuotaData(xQuotaData);
                    QuotaData yQuotaData = new QuotaData(angle, QuotaEnum.SQUARE.getCode(), detectionPoints, passPoints, AssociateEnum.WALL_DATA.getCode(), xWall.getName());
                    yWall.addQuotaData(yQuotaData);
                }
            }


            batchGenerateImage(stationCalculation);
            stationDataDTO.setTotalPoints((long) allPoints.size());
            //数据在本地数据库与服务器的流转处理
            dataFlow();
            //更新状态
            statusNotificationUtil.updateStatus(105);

            calculation = false;
            log.info("计算" + stationDataDTO.getStationAlias() + "完成");
        } catch (Exception e) {
            log.error(e.toString());
            calculation = false;
        }
//   之后将点云文件提交上去 taskDataDTO.getPk() + "_" + stationDataDTO.getActualOrder();
    }

    public void computeCeilingFloor(List<BlkPoint> allPoints) {


        Map<Double, List<BlkPoint>> map = PointUtil.groupByCoordinate(allPoints, Coordinate.Z);
        int ceilingSize = 0, floorSize = 0;
        double ceilingKey = 0, floorKey = 0;
        for (double key : map.keySet()) {
            int pointNum = map.get(key).size();
            if (map.get(key).get(0).getZ() < 0 && pointNum > floorSize) {
                floorSize = pointNum;
                floorKey = key;
            } else if (map.get(key).get(0).getZ() > 0 && pointNum > ceilingSize) {
                ceilingSize = pointNum;
                ceilingKey = key;
            }
        }
        ceiling = FindHole.computeAver(map.get(ceilingKey), Coordinate.Z);
        floor = FindHole.computeAver(map.get(floorKey), Coordinate.Z);
    }

    @Override
    public void prepareData(StationDataDTO stationDataDTO) {
        StationData stationData = stationDataMapper.selectStationData(stationDataDTO.getPk());
        BeanUtils.copyProperties(stationData, stationDataDTO);
        stationDataDTO.setBay(0.0);
        stationDataDTO.setDepth(0.0);

        TaskData taskData = taskDataMapper.selectTaskData(stationDataDTO.getTaskDataPk());
        TaskDataDTO taskDataDTO = new TaskDataDTO();
        try {
            BeanUtils.copyProperties(taskData, taskDataDTO);
        } catch (Exception e) {
            statusNotificationUtil.updateStatus(206);
            log.error(e.toString());
            throw new BusinessException("找不到该任务");
        }
        this.taskDataDTO = taskDataDTO;
        this.stationDataDTO = stationDataDTO;
        statusNotificationUtil.updateStatus(106);
    }

    //        @Async("nonCalculationExecutor")
    public void dataFlow() {
        //TODO 之后这部分的补传机制要改
        if (HttpClientUtil.isServerNormal()) {
            String stationMac = stationDataDTO.getPk() + "";
            taskDataPrepare.init(stationMac, true);
        }
        //解析完成后，将计算结果更新到数据库
        updateStationData();
        //判断是否已按方案完成了所需测量的测站
        List<StationData> stationDataList = stationDataMapper.selectStationDataByTaskDataPk(taskDataDTO.getPk());
        updateTaskData(stationDataList);
        //生成任务相关数据的文件
        generateTaskDataFile(stationDataList);

//        if (HttpClientUtil.isServerNormal()) {
//            //将所有图片上传，如果有之前未上传的，这次也会被上传。
//            String imageUrl = ServerAddress.MAPPING + "/taskData/uploadImage.do";
//            File imageFolder = new File(Folder.IMAGE);
//            File[] files = imageFolder.listFiles();
//            //图片大小较大，所以压缩多次传
//            if (null != files && files.length > 0) {
//                for (File file : files) {
//                    uploadImage(imageUrl, file);
//                }
//            }
////              上传结果集，用于画图
////            String textUrl = ServerAddress.MAPPING + "taskData/uploadText.do";
////            File dataFolder = new File(Folder.RESULT);
////            File[] dataFiles = dataFolder.listFiles();
////            if (null != dataFiles && dataFiles.length > 0) {
////                for (File data : dataFiles) {
////                    uploadResult(textUrl, data);
////                }
////            }
////              上传结果集
//            String url = ServerAddress.MAPPING + "/taskData/uploadResult.do";
//            //因为网络不好时，结果文件不会向上传，所以每次将整个data文件夹里的内容都上传
//            File dataFolder = new File(Folder.DATA);
//            File[] dataFiles = dataFolder.listFiles();
//            if (null != dataFiles && dataFiles.length > 0) {
//                for (File data : dataFiles) {
//                    uploadResult(url, data);
//                }
//            }
//        } else {
//            statusNotificationUtil.updateStatus(208);//上传结果失败
//        }
    }

    /**
     * 将中间结果输出到文件中
     *
     * @param walls
     */
    private void resultToFile(List<Wall> walls) throws IOException {
        String filename = Folder.RESULT + "/" + taskDataDTO.getPk() + Suffix.TXT;
        try {
            File file = new File(filename);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("{\"" + "wallDataList" + "\":" + "[");

//            墙面名，墙面函数，不合格点集(水平凸，水平凹)
            int wallFlag = 0;
            for (Wall wall : walls) {
                if (wallFlag == 1)
                    fileWriter.write(",");
                if (wallFlag == 0)
                    wallFlag = 1;
                fileWriter.write("{\"wallName\":\"" + wall.getName() + "\",");
                fileWriter.write("\"Plane\":\"" + wall.getPlane().getFormula() + "\",");

                unqualifiedToFile(fileWriter, wall.getPoints(), PointTypeEnum.BOUND_POINT.getCode(), "boundPoints");
                fileWriter.write(",");
                unqualifiedToFile(fileWriter, wall.getPoints(), PointTypeEnum.SAG_FLAT.getCode(), "sagFlatPoints");
                fileWriter.write(",");
                unqualifiedToFile(fileWriter, wall.getPoints(), PointTypeEnum.RAISE_FLAT.getCode(), "raiseFlatPoints");
                fileWriter.write("}");
            }
            fileWriter.write("],");
            fileWriter.write("\"" + "holeDataList" + "\":" + "[");
            int holeFlag = 0;
            for (QuotaData quotaData : stationDataDTO.getQuotaDataList()) {
                if (quotaData.getQuotaType().equals(QuotaEnum.DOOR_HOLE_HEIGHT_SIZE.getCode()) ||
                        quotaData.getQuotaType().equals(QuotaEnum.WINDOW_HOLE_HEIGHT_SIZE.getCode())) {
                    if (holeFlag == 1)
                        fileWriter.write(",");
                    if (holeFlag == 0)
                        holeFlag = 1;
                    fileWriter.write(quotaData.getManuals());
                }
            }
            fileWriter.write("]}");
            fileWriter.close();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }


    private void unqualifiedToFile(FileWriter fileWriter, List<BlkPoint> blkPoints, Integer code, String key) throws IOException {
        fileWriter.write("\"" + key + "\":[");
        int flag = 0;
        for (BlkPoint blkPoint : blkPoints) {
            if (blkPoint.getType().equals(code)) {
                if (flag == 1)
                    fileWriter.write(",");
                if (flag == 0)
                    flag = 1;
                fileWriter.write("[" + blkPoint.getX() + "," + blkPoint.getY() + "," + blkPoint.getZ() + "]");
            }
        }
        fileWriter.write("]");
    }


    /**
     * 向服务器提交结果文件及图片
     *
     * @param url
     * @param file
     */
    private void uploadResult(String url, File file) {
        log.info("开始提交文件：" + file.getName());
        boolean isUploadSuccess = HttpClientUtil.uploadFile(url, file);
        if (isUploadSuccess) {
            FileUtils.deleteQuietly(file);
            log.info("向服务器提交结果文件完成");
        }
    }

    /**
     * 统计taskData并输出成文件
     *
     * @param stationDataList
     */
    private File generateTaskDataFile(List<StationData> stationDataList) {
        //设置任务数据
        Long taskDataPk = taskDataDTO.getPk();
        List<StationDataDTO> stationDataDTOList = new ArrayList<>();
        //查出任务对应指标
        List<QuotaData> taskQuotaDataList = quotaDataMapper.selectQuotaDataByAssociate(AssociateEnum.TASK_DATA.getCode(), taskDataPk);
        taskDataDTO.setQuotaDataList(taskQuotaDataList);
        //查出任务对应测站数据
        for (StationData stationData : stationDataList) {
            if (!stationData.getPk().equals(stationDataDTO.getPk())) {
                continue;
            }
            List<WallData> wallDataList = wallDataMapper.selectCompleteWallDataByStationDataPk(stationData.getPk());
            List<QuotaData> stationQuotaDataList = quotaDataMapper.selectQuotaDataByAssociate(AssociateEnum.STATION_DATA.getCode(), stationData.getPk());
            List<WallDataDTO> wallDataDTOList = new ArrayList<>();
            for (WallData wallData : wallDataList) {
                List<QuotaData> wallQuotaDataList = quotaDataMapper.selectQuotaDataByAssociate(AssociateEnum.WALL_DATA.getCode(), wallData.getPk());
                //数据转移给DTO
                WallDataDTO wallDataDTO = new WallDataDTO();
                BeanUtils.copyProperties(wallData, wallDataDTO);
                wallDataDTO.setQuotaDataList(wallQuotaDataList);
                wallDataDTOList.add(wallDataDTO);
            }
            //数据转移给DTO
            StationDataDTO stationDataDTO = new StationDataDTO();
            BeanUtils.copyProperties(stationData, stationDataDTO);
            stationDataDTO.setQuotaDataList(stationQuotaDataList);
            stationDataDTO.setWallDataDTOList(wallDataDTOList);
            stationDataDTOList.add(stationDataDTO);
        }
        taskDataDTO.setStationDataDTOList(stationDataDTOList);
        //结果输出到文件
        String content = JSON.toJSONString(taskDataDTO);
        File resultFile = new File(Folder.DATA + "/" + taskDataPk + Suffix.TXT);
        try {
            FileUtils.writeStringToFile(resultFile, content, Encoding.UTF8);
        } catch (IOException e) {
            log.error("生成结果文件失败", e);
        }
        return resultFile;
    }

    /**
     * 用于将quota向上统计
     *
     * @param quotaDataList
     * @param newAssociateType
     * @return
     */
    private List<QuotaData> groupIntegration(List<QuotaData> quotaDataList, Integer newAssociateType) {
        Map<Integer, List<QuotaData>> quotaDataMap = new HashMap<>();
        List<QuotaData> newQuotaDataList = new ArrayList<>();
        //对指标分组
        for (QuotaData perQuotaData : quotaDataList) {
            Integer quotaType = perQuotaData.getQuotaType();
            List<QuotaData> dataList = quotaDataMap.get(quotaType);
            if (null == dataList) {
                dataList = new ArrayList<>();
            }
            dataList.add(perQuotaData);

            quotaDataMap.put(quotaType, dataList);
        }
        //对分组的map进行数据整合
        for (Integer quotaType : quotaDataMap.keySet()) {
            List<QuotaData> quotaDataGroup = quotaDataMap.get(quotaType);
            Double quotaValue = 0.0;
            Long detectionPoints = 0l;
            Long passPoints = 0l;
            //数据统
            // 计
            for (QuotaData quotaData : quotaDataGroup) {
                quotaValue += quotaData.getQuotaValue();
                detectionPoints += quotaData.getDetectionPoints();
                passPoints += quotaData.getPassPoints();
            }
            quotaValue /= quotaDataGroup.size();
            QuotaData newQuotaData = new QuotaData(quotaValue, quotaType, detectionPoints, passPoints, newAssociateType);
            newQuotaDataList.add(newQuotaData);
        }
        return newQuotaDataList;
    }

    /**
     * 旋转所有点
     *
     * @param allPoints
     * @return
     */
    public static void rotate(List<BlkPoint> allPoints) {
        //因为点集本身有序，上下分别为地，所以取中间部分
        int fromIndex = allPoints.size() * 2 / 5;
        int toIndex = allPoints.size() * 3 / 5;
        List<? extends Point3D> subList = allPoints.subList(fromIndex, toIndex);
        List<Point2D> bottom = PointUtil.reduceDimension(subList, Coordinate.Z);
        List<Double> parameters = LineUtil.lineRansac(bottom, subList.size() / 10);
        double deflectionAngleArc = Math.atan(-parameters.get(1) / parameters.get(0));
        //将全部点进行旋转坐标系
        PointUtil.rotateHorizontally(allPoints, deflectionAngleArc);
        log.info("水平旋转完成，旋转了" + deflectionAngleArc / Math.PI * 180 + "度");
    }

    /**
     * 根据坐标轴向筛选出可能的墙面，并返回剩余的点
     *
     * @param allPoints
     * @param coordinate
     * @return
     */
    private StationCalculation wallExtract(List<BlkPoint> allPoints, String coordinate) throws IOException, ClassNotFoundException {
        //根据坐标轴对点集分组
        Map<Double, List<BlkPoint>> map = PointUtil.groupByCoordinate(allPoints, coordinate);
        //以点均匀散落在坐标上对应的标准点集大小作为标准
        List<Integer> mapSize = new ArrayList<>();
        for (double key : map.keySet()) {
            mapSize.add(map.get(key).size());
        }
        Collections.sort(mapSize);
        Collections.reverse(mapSize); // 倒序排序
        int normSize;
//        normSize = (int)(1.5*mapSize.get((int)(0.5*mapSize.size())));
        normSize = 3 * allPoints.size() / map.size();
        StationCalculation stationCalculation = getStationCalculation(map, normSize, coordinate);
        //将数据填充好
        if (Coordinate.Z.equals(coordinate)) {
            //将天花地板筛选出来
            int ceilingSize = 0, floorSize = 0, ceilingIndex = 0, floorIndex = 0;
            //找点数最大的面
            List<Wall> zWalls = stationCalculation.getWalls();
            for (int i = 0; i < zWalls.size(); i++) {
                Wall wall = zWalls.get(i);
                int pointNum = wall.getPoints().size();
                if (Axis.NEGATIVE.equals(wall.getAxis()) && pointNum > floorSize) {
                    floorSize = pointNum;
                    floorIndex = i;
                } else if (Axis.POSITIVE.equals(wall.getAxis()) && pointNum > ceilingSize) {
                    ceilingSize = pointNum;
                    ceilingIndex = i;
                }
            }
            if (stationCalculation.getWalls().size() < 2) {
                statusNotificationUtil.updateStatus(207);
//                log.info("扫描数据缺失，请重新扫描");
                throw new BusinessException("天花地板缺失，请清理后重新扫描");
            }
            //设置名字
            zWalls.get(floorIndex).setName(PlaneName.FLOOR);
            //TODO 之后要写的简单些
            Wall ceiling = zWalls.get(ceilingIndex);
            ceiling.setName(PlaneName.CEILING);

            Plane plane = PlaneUtil.planeFitting(ceiling.getPoints());
            Plane plane1 = PlaneUtil.planeFitting(zWalls.get(floorIndex).getPoints());
            stationDataDTO.setHeight(BigDecimalUtil.scale(Math.abs(plane.getIntercept()) + Math.abs(plane1.getIntercept()), 2));
        }
        int i = 1;
        for (Wall wall : stationCalculation.getWalls()) {
            if (null == wall.getName()) {

                //设置墙面名
                if (Coordinate.X.equals(wall.getCoordinate())) {
                    wall.setName("S2." + (i++));
                } else if (Coordinate.Y.equals(wall.getCoordinate())) {
                    wall.setName("S3." + (i++));
                } else if (Coordinate.Z.equals(wall.getCoordinate())) {
                    continue;//Z方向上只计算天花和地板
                }
//                排除是横梁的墙
                if (FindHole.computeAver(wall.getPoints(), Coordinate.Z) < ceiling - 0.5) {
                    overView.addAllWallsPoint(wall.getPoints());
                    Text text = new Text();
                    text.setText(wall.getName());
                    if (Coordinate.X.equals(wall.getCoordinate())) {
                        double wallX = FindHole.computeAver(wall.getPoints(), Coordinate.X);
                        wallX = (wallX < 0 ? wallX + 0.2 : wallX - 0.2);
                        double wallY = FindHole.computeAver(wall.getPoints(), Coordinate.Y);
                        text.setX(wallX);
                        text.setY(wallY);
                    } else if (Coordinate.Y.equals(wall.getCoordinate())) {
                        double wallX = FindHole.computeAver(wall.getPoints(), Coordinate.X);
                        double wallY = FindHole.computeAver(wall.getPoints(), Coordinate.Y);
                        wallY = (wallY < 0 ? wallY + 0.1 : wallY - 0.1);
                        text.setX(wallX);
                        text.setY(wallY);
                    }
                    overView.getTexts().add(text);
                }

            }
            //根据图片名设置图片路径
            //TODO 之后图片路径的要改
            String folder = Folder.IMAGE + "/" + taskDataDTO.getPk() + "_" + stationDataDTO.getActualOrder() + "/";
            wall.setImagePath(folder + "flatness-" + wall.getName() + Suffix.JPG);
            wall.setImagePath2(folder + "vertical-" + wall.getName() + Suffix.JPG);
            //数据填充完后，转换为wallDataDTO
            WallDataDTO wallDataDTO = WallUtil.formatWall(wall, floor, quota);
            //wallData放到stationData
            stationDataDTO.addWallDataDTO(wallDataDTO);
        }
        return stationCalculation;
    }

    /**
     * @param allPoints
     * @return
     */
    private List<BlkPoint> findHole(List<BlkPoint> allPoints, double quota) {
//        取墙面数据点
        List<BlkPoint> wallPoints = FindHole.getWallPoints(allPoints, ceiling - Threshold.CEILING_THRESHOLD, floor + Threshold.FLOOR_THRESHOLD);
        //根据坐标轴对点集分组
        Map<Double, List<BlkPoint>> map = PointUtil.groupByCoordinate(wallPoints, Coordinate.PHI);
        List<BlkPoint> datumPoints = FindHole.cycle(map, ceiling - Threshold.SAFE_THRESHOLD_UP, ceiling - Threshold.SAFE_THRESHOLD_LOW);
//        先计算一遍开间、进深
//        FindHole.pointsToFile(datumPoints, "D:\\study\\python\\fof\\datumPoints.txt");
//        computeDepthBay(datumPoints);
        FindHole.setOuterPoints(allPoints, datumPoints, Threshold.OUT_POINT_DISTANCE); // 标记飘出点
        FindHole.holeCycle(map, floor + 0.2, floor + 0.15, datumPoints);
//        FindHole.pointsToFile(datumPoints, "D:\\study\\python\\fof\\datumPoints1.txt");
        FindHole.setOuterPoints(allPoints, datumPoints, Threshold.OUT_POINT_DISTANCE_EXACT);
        List<Double> holeMark = FindHole.findHoleMark(map, datumPoints, ceiling, floor);
        List<QuotaData> quotaDataList = FindHole.computeHole(map, holeMark, datumPoints, floor);

//        计算洞边平整度
        List<QuotaData> holeQuotaDataList = FindHole.computeHoleFlatness(quotaDataList, map, datumPoints, quota);
        stationDataDTO.addQuotaDataList(holeQuotaDataList);

        //增加门窗洞指标数据
        stationDataDTO.addQuotaDataList(createDoorQuotaData(quotaDataList));
//        绘制俯视图门洞所需的数据点
        List<Point2D> holePoints = new ArrayList<>();
        for (QuotaData quotaData : quotaDataList) {
            log.info(quotaData.toString());
            String manuals = quotaData.getManuals();
            Hole hole = JSON.parseObject(manuals, Hole.class);
            Text text = new Text();
            double coordinateValue = hole.getCoordinateValue();
            coordinateValue = (coordinateValue < 0 ? coordinateValue - 0.1 : coordinateValue + 0.1);
            if (hole.getCoordinate().equals(Coordinate.X)) {
                holePoints.add(new Point2D(hole.getCoordinateValue(), hole.getStartW()));
                holePoints.add(new Point2D(hole.getCoordinateValue(), hole.getEndW()));
                text.setX(coordinateValue);
                text.setY((hole.getStartW() + hole.getEndW()) / 2);
            } else {
                holePoints.add(new Point2D(hole.getStartW(), hole.getCoordinateValue()));
                holePoints.add(new Point2D(hole.getEndW(), hole.getCoordinateValue()));
                text.setX((hole.getStartW() + hole.getEndW()) / 2);
                text.setY(coordinateValue);
            }
            text.setText(hole.getType() + hole.getName());
            overView.getTexts().add(text);
//            System.out.println(quotaData.toString());
        }
        overView.addAllHolePoints(holePoints);
        return datumPoints;
    }

    /**
     * 保存门窗洞口指标数据
     *
     * @param quotaDataList
     */
    private List<QuotaData> createDoorQuotaData(List<QuotaData> quotaDataList) {
        List<QuotaData> newQuotaDataList = new ArrayList<>();
        for (QuotaData quotaData : quotaDataList) {
            String manuals = quotaData.getManuals();
            Hole hole = JSON.parseObject(manuals, Hole.class);
            QuotaData quotaData1 = new QuotaData();
            QuotaData quotaData2 = new QuotaData();

            quotaData1.setQuotaValue(hole.getHeight());
            quotaData1.setDetectionPoints(quotaData.getDetectionPoints());
            quotaData1.setPassPoints(quotaData.getPassPoints());
            quotaData1.setQuotaAlias(hole.getName());
            quotaData1.setManuals(manuals);
            quotaData1.setAssociateType(AssociateEnum.STATION_DATA.getCode());

            quotaData2.setQuotaValue(hole.getWidth());
            quotaData2.setDetectionPoints(quotaData.getDetectionPoints());
            quotaData2.setPassPoints(quotaData.getPassPoints());
            quotaData2.setQuotaAlias(hole.getName());
            quotaData2.setManuals(manuals);
            quotaData2.setAssociateType(AssociateEnum.STATION_DATA.getCode());

            if (hole.getType().equals("门")) {
                quotaData1.setQuotaType(QuotaEnum.DOOR_HOLE_HEIGHT_SIZE.getCode());
                quotaData2.setQuotaType(QuotaEnum.DOOR_HOLE_WIDTH_SIZE.getCode());
            } else if (hole.getType().equals("窗")) {
                quotaData1.setQuotaType(QuotaEnum.WINDOW_HOLE_HEIGHT_SIZE.getCode());
                quotaData2.setQuotaType(QuotaEnum.WINDOW_HOLE_WIDTH_SIZE.getCode());
            }
            newQuotaDataList.add(quotaData1);
            newQuotaDataList.add(quotaData2);
        }
        return newQuotaDataList;
    }

    /**
     * @param depthBayEdge
     * @param quotaDataList
     */
    private void setDepthBay(List<BlkPoint> depthBayEdge, List<QuotaData> quotaDataList) {
//        List<BlkPoint> wallPoints = FindHole.getWallPoints(allPoints, ceiling - Threshold.CEILING_THRESHOLD, floor + Threshold.FLOOR_THRESHOLD);
//        //根据坐标轴对点集分组
//        Map<Double, List<BlkPoint>> map = PointUtil.groupByCoordinate(wallPoints, Coordinate.PHI);
//        List<BlkPoint> depthBayEdge = FindCycle.findCycle(map, floor + 0.2, floor + 0.15);
        computeDepthBay(depthBayEdge);
        for (QuotaData quotaData : quotaDataList) {
            String manuals = quotaData.getManuals();
            if (manuals == null)
                continue;
            Hole hole = JSON.parseObject(manuals, Hole.class);
//            System.out.println(hole.toString());
            if (hole.getType().equals("门"))
                if (hole.getCoordinate().equals(Coordinate.Y)) {
//                    交换开间进深
                    double tempDepth = stationDataDTO.getDepth();
                    stationDataDTO.setDepth(BigDecimalUtil.scale(stationDataDTO.getBay(), 3));
                    stationDataDTO.setBay(BigDecimalUtil.scale(tempDepth, 3));
                    break;
                }
        }
        //        开间进深俯视图
    }

    private void computeDepthBay(List<BlkPoint> depthBayEdge) {
        Map<Double, List<BlkPoint>> x = FindHole.groupBy(depthBayEdge, Const.Coordinate.X, 2);
        Map<Double, List<BlkPoint>> y = FindHole.groupBy(depthBayEdge, Const.Coordinate.Y, 2);
//        System.out.println(x.keySet().toString());
//        System.out.println(y.keySet().toString());
        double xMax = 0.0, xMin = 0.0;
        double yMax = 0.0, yMin = 0.0;

        for (double key : x.keySet()) {
            double m = FindHole.computeAver(x.get(key), Const.Coordinate.X);
            xMin = (m < xMin ? m : xMin);
            xMax = (m > xMax ? m : xMax);
        }
        for (double key : y.keySet()) {
            double m = FindHole.computeAver(y.get(key), Const.Coordinate.Y);
            yMin = (m < yMin ? m : yMin);
            yMax = (m > yMax ? m : yMax);
        }
        if (stationDataDTO.getDepth() < 0.01 || Math.abs(xMax - xMin - stationDataDTO.getDepth())< 0.2) {
            stationDataDTO.setDepth(BigDecimalUtil.scale(xMax - xMin, 3));
        }
        if (stationDataDTO.getBay() < 0.01 || Math.abs(yMax - yMin - stationDataDTO.getBay()) < 0.2) {
            stationDataDTO.setBay(BigDecimalUtil.scale(yMax - yMin, 3));
        }
//        System.out.println("进深"+stationDataDTO.getDepth());
//        System.out.println("开间"+stationDataDTO.getBay());
    }

    /**
     * 批量生成图片
     *
     * @param stationCalculation
     */
    public void batchGenerateImage(StationCalculation stationCalculation) {
        for (Wall wall : stationCalculation.getWalls()) {
            if (null == wall.getName()) {
                continue;
            }
            WallUtil.symmetry(wall);
            generateImage(wall);
        }
        log.info("图片生成完毕");
    }

    /**
     * 图片传上服务器
     *
     * @param url
     * @param sourceFile
     */
    private void uploadImage(String url, File sourceFile) {
        File zipFile = new File(Folder.SUBMIT + "/" + taskDataDTO.getPk() + "_" + stationDataDTO.getActualOrder() + Suffix.ZIP);
        if (!zipFile.getParentFile().exists()) {
            zipFile.getParentFile().mkdirs();
        }
        if (zipFile.exists()) {
            FileUtils.deleteQuietly(zipFile);
        }
        try (FileOutputStream fos = new FileOutputStream(zipFile)) {
            FileUtil.toZip(sourceFile, fos, true);
            boolean isUploadSuccess = HttpClientUtil.uploadFile(url, zipFile);
            if (isUploadSuccess) {
                log.info("提交测站图片成功");
                FileUtils.deleteQuietly(sourceFile);
//                FileUtils.deleteQuietly(zipFile);
            }
        } catch (FileNotFoundException e) {
            log.error("文件无法找到", e);
        } catch (IOException e) {
            log.error("关闭文件输出流失败", e);
        }
    }

    private void generateImage(@NonNull Wall wall) {
        String xAxisLabel = "", yAxisLabel = "", title = "";
        String coordinate = wall.getCoordinate();
        //设置报表字体样式以解决中文乱码
        StandardChartTheme standardChartTheme = ImageUtil.getStandardChartTheme();
        ChartFactory.setChartTheme(standardChartTheme);
        //根据坐标类型 构造chart所需参数
        if (Coordinate.Z.equals(coordinate)) {
            xAxisLabel = Coordinate.X;
            yAxisLabel = Coordinate.Y;
        } else if (Coordinate.X.equals(coordinate)) {
            xAxisLabel = Coordinate.Y;
            yAxisLabel = Coordinate.Z;
        } else if (Coordinate.Y.equals(coordinate)) {
            xAxisLabel = Coordinate.X;
            yAxisLabel = Coordinate.Z;
        }
        //根据指标类型，组拼title
        for (QuotaData quotaData : wall.getQuotaDataList()) {
            Integer code = quotaData.getQuotaType();
            String quotaName = EnumUtil.getByCode(code, QuotaEnum.class).getMeaning();
            if (code.equals(QuotaEnum.SQUARE.getCode())) {
                quotaName = "与" + quotaData.getAssociateName() + quotaName + "为";
            }
            title += quotaName + ":" + quotaData.getQuotaValue() + ";";
        }
        //构造数据集
        DefaultXYDataset xyDataSet = new DefaultXYDataset();
        DefaultXYDataset xyDataSet2 = new DefaultXYDataset();

        List<BlkPoint> raise = new ArrayList<>();
        List<BlkPoint> sag = new ArrayList<>();
        List<BlkPoint> qualified = new ArrayList<>();
        List<BlkPoint> qualified_v = new ArrayList<>();
//        List<BlkPoint> doorHole = new ArrayList<>();
//        List<BlkPoint> windowHole = new ArrayList<>();
//        List<BlkPoint> outpoint = new ArrayList<>();
        List<BlkPoint> raise_v = new ArrayList<>();
        List<BlkPoint> sag_v = new ArrayList<>();
        for (BlkPoint blkPoint : wall.getPoints()) {
            Integer type = blkPoint.getType();
            if (PointTypeEnum.RAISE_FLAT.getCode().equals(type) ||
                    PointTypeEnum.RAISE.getCode().equals(type)) {
                raise.add(blkPoint);
            } else if (PointTypeEnum.SAG_FLAT.getCode().equals(type) ||
                    PointTypeEnum.SAG.getCode().equals(type)) {
                sag.add(blkPoint);
            } else {
                qualified.add(blkPoint);
            }
//            else if(PointTypeEnum.BOUND_POINT.getCode().equals(type))
//                qualified.add(blkPoint);

//            if (PointTypeEnum.QUALIFIED.getCode().equals(type)||
//                    PointTypeEnum.BASIS.getCode().equals(type))

//            else if (PointTypeEnum.DOOR_HOLE.getCode().equals(type)) {
//                doorHole.add(blkPoint);
//            } else if (PointTypeEnum.WINDOW_HOLE.getCode().equals(type)) {
//                windowHole.add(blkPoint);
//            } else if (PointTypeEnum.OUT_POINT.getCode().equals(type)){
//                outpoint.add(blkPoint);
//            }
            if (PointTypeEnum.RAISE_VERTICAL.getCode().equals(type) ||
                    PointTypeEnum.RAISE.getCode().equals(type)) {
                raise_v.add(blkPoint);
            } else if (PointTypeEnum.SAG_VERTICAL.getCode().equals(type) ||
                    PointTypeEnum.SAG.getCode().equals(type)) {
                sag_v.add(blkPoint);
            } else {
                qualified_v.add(blkPoint);
            }
        }


        xyDataSet.addSeries(PointTypeEnum.RAISE.getMeaning(), ImageUtil.getDataSet(raise, coordinate));
        xyDataSet.addSeries(PointTypeEnum.SAG.getMeaning(), ImageUtil.getDataSet(sag, coordinate));
//        xyDataSet.addSeries(PointTypeEnum.RAISE_VERTICAL.getMeaning(), ImageUtil.getDataSet(raise_v, coordinate));
//        xyDataSet.addSeries(PointTypeEnum.SAG_VERTICAL.getMeaning(), ImageUtil.getDataSet(sag_v, coordinate));
        xyDataSet.addSeries(PointTypeEnum.QUALIFIED.getMeaning(), ImageUtil.getDataSet(qualified, coordinate));
//        xyDataSet.addSeries(PointTypeEnum.DOOR_HOLE.getMeaning(), ImageUtil.getDataSet(doorHole, coordinate));
//        xyDataSet.addSeries(PointTypeEnum.WINDOW_HOLE.getMeaning(), ImageUtil.getDataSet(windowHole, coordinate));
//        xyDataSet.addSeries(PointTypeEnum.OUT_POINT.getMeaning(), ImageUtil.getDataSet(outpoint, coordinate));

        JFreeChart chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel,
                xyDataSet,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        XYItemRenderer renderer = (XYItemRenderer) chart.getXYPlot().getRenderer();
        renderer.setSeriesShape(0, new QuadCurve2D.Double(-5D, 0D, 0D, -20D, 5D, 0D));
        renderer.setSeriesShape(1, new QuadCurve2D.Double(-5D, 0D, 0D, 20D, 5D, 0D));
        renderer.setSeriesShape(2, new Rectangle2D.Double(-2D, -2D, 4D, 4D));
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.BLUE);
        renderer.setSeriesPaint(2, Color.GREEN);
        chart.getXYPlot().setRenderer(renderer);

        xyDataSet2.addSeries(PointTypeEnum.RAISE.getMeaning(), ImageUtil.getDataSet(raise_v, coordinate));
        xyDataSet2.addSeries(PointTypeEnum.SAG.getMeaning(), ImageUtil.getDataSet(sag_v, coordinate));
        xyDataSet2.addSeries(PointTypeEnum.QUALIFIED.getMeaning(), ImageUtil.getDataSet(qualified_v, coordinate));
        JFreeChart chart2 = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel,
                xyDataSet2,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        chart2.getXYPlot().setRenderer(renderer);

        ImageUtil.scatterPlot(chart, wall, ImageEnum.FLATNESS.getCode());
        ImageUtil.scatterPlot(chart2, wall, ImageEnum.VERTICAL.getCode());
    }

    /**
     * 拆分点云，组装进wallDto
     *
     * @param map
     * @param normSize
     * @param coordinate
     * @return
     */
    private StationCalculation getStationCalculation(Map<Double, List<BlkPoint>> map, int normSize, String coordinate) {
        StationCalculation stationCalculation = new StationCalculation();
        Wall currentWall = new Wall();
        //根据是否属于平面分类点
        int previousSize = 0, previousChangeSize = 0, normChangeSize = 5;
        boolean likeWall = false, likeWallStart, likeWallEnd, isWall = false, isWallStart, isWallEnd;
        int coordNum = 0;
        for (Double coord : map.keySet()) {
            List<BlkPoint> currentGroup = map.get(coord);
            int currentSize = currentGroup.size();
//            int changeSize = currentSize - previousSize;
            //判断前后两值是否分布在标准值两侧
            likeWallStart = currentSize >= normSize && previousSize <= normSize;
            likeWallEnd = currentSize <= normSize && previousSize >= normSize;
            if (!likeWall && likeWallStart) {
                coordNum = 0;
                likeWall = true;
                //创建新的墙
                currentWall = new Wall();
                if (coord >= 0) {
                    currentWall.setAxis(Axis.POSITIVE);
                } else {
                    currentWall.setAxis(Axis.NEGATIVE);
                }
                currentWall.setCoordinate(coordinate);
            } else if (likeWall && likeWallEnd) {

                likeWall = false;
//                将墙面两边的点集加进来
                if (currentWall.getPointsKeySet().isEmpty()) {
                    continue;
                }
//                double startKey = currentWall.getPointsKeySet().get(0);
//                double endKey = currentWall.getPointsKeySet().get(currentWall.getPointsKeySet().size() - 1);
//                if (Axis.POSITIVE.equals(currentWall.getAxis())) {
//                    if (map.get(BigDecimalUtil.scale(startKey - 0.0001, 4)) != null)
//                        if (map.get(BigDecimalUtil.scale(startKey - 0.0001, 4)).size() > normSize / 2)
//                            currentWall.getPoints().addAll(map.get(BigDecimalUtil.scale(startKey - 0.0001, 4)));
//                } else {
//                    if (map.get(BigDecimalUtil.scale(endKey + 0.0001, 4)) != null)
//                        if (map.get(BigDecimalUtil.scale(endKey + 0.0001, 4)).size() > normSize / 2)
//                            currentWall.getPoints().addAll(map.get(BigDecimalUtil.scale(endKey + 0.0001, 4)));
//                }

//                for(double key=startKey-0.0001; key<startKey; key+=0.0001) {
//                    if (map.get(BigDecimalUtil.scale(key, 4)) == null)
//                        continue;
//                    if (map.get(BigDecimalUtil.scale(key, 4)).size()>normSize/2)
//                        currentWall.getPlanePoints().put(BigDecimalUtil.scale(key, 4), map.get(BigDecimalUtil.scale(key, 4)));
//                    currentWall.getPoints().addAll(map.get(BigDecimalUtil.scale(key, 4)));
//                }
//                for(double key=endKey; key<endKey+0.001; key+=0.0001) {
//                    if (map.get(BigDecimalUtil.scale(key, 4)) == null)
//                        continue;
//                    if (map.get(BigDecimalUtil.scale(key, 4)).size()>normSize/2)
//                        currentWall.getPlanePoints().put(BigDecimalUtil.scale(key, 4), map.get(BigDecimalUtil.scale(key, 4)));
//                    currentWall.getPoints().addAll(map.get(BigDecimalUtil.scale(key, 4)));
//                }
                if (currentWall.getCoordinate().equals(Coordinate.X)) {
                    currentWall = removeBoundLine(currentWall, Coordinate.Z_2, stationCalculation);
                    currentWall = removeBoundLine(currentWall, Coordinate.Y, stationCalculation);
                } else if (currentWall.getCoordinate().equals(Coordinate.Y)) {
                    currentWall = removeBoundLine(currentWall, Coordinate.Z_2, stationCalculation);
                    currentWall = removeBoundLine(currentWall, Coordinate.X, stationCalculation);
                }
                else if (currentWall.getCoordinate().equals(Coordinate.Z)) {
                    currentWall = removeBoundLine(currentWall, Coordinate.Y, stationCalculation);
                    currentWall = removeBoundLine(currentWall, Coordinate.X, stationCalculation);
                }
                int pointSize = currentWall.getPoints().size();
                //将点数太少不算墙的排除 pointSize/coordNum<4* &&
                if (coordinate.equals(Coordinate.Z) || pointSize > 15000) {
                    stationCalculation.getWalls().add(currentWall);
                } else {
                    stationCalculation.getRemainPoints().addAll(currentWall.getPoints());
                }
            }
            //根据初步筛选结果，进行更进一步筛选
            if (likeWall) {
                coordNum++;
//                for (BlkPoint blkPoint : currentGroup) {
//                    stationCalculation.getRemainPoints().add(blkPoint);
//                }
                currentWall.getPoints().addAll(currentGroup);
                currentWall.getPointsKeySet().add(coord);
                currentWall.getPlanePoints().put(coord, currentGroup);
//                } else {
//                    stationCalculation.getRemainPoints().addAll(currentGroup);
//                }
            } else {
                stationCalculation.getRemainPoints().addAll(currentGroup);
            }
            //用于记录上个点集的size
            previousSize = currentSize;
//            previousChangeSize = changeSize;
        }
        return stationCalculation;
    }

    private Wall removeBoundLine(Wall currentWall, String coordinate, StationCalculation stationCalculation) {
        int count = 0, count2 = 0;
        Map<Double, List<BlkPoint>> newmap = PointUtil.groupByCoordinate(currentWall.getPoints(), coordinate);
        int size = newmap.size();
        double previousKey = -999.9;
        Iterator<Map.Entry<Double, List<BlkPoint>>> it = newmap.entrySet().iterator();
//        System.out.println("a" + newmap.size());
        int countNum = (int) (newmap.size() * 0.002);
//        System.out.println(countNum);
        while (it.hasNext()) {
            Map.Entry<Double, List<BlkPoint>> entry = it.next();
            count++;
            if (count <= countNum || count >= size - countNum) {
                stationCalculation.getRemainPoints().addAll(entry.getValue());
                it.remove();
            } else {
                if (Math.abs(entry.getKey() - previousKey) > 0.4 && Math.abs(entry.getKey() - previousKey) < 900 && count2 < 50) {
                    count2++;
                    stationCalculation.getRemainPoints().addAll(entry.getValue());
                    it.remove();
                }
            }
            previousKey = entry.getKey();
        }
        TreeMap<Double, List<BlkPoint>> newTreemap = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                if (o1 - o2 < 0) {
                    return -1;
                } else if (o1 - o2 == 0) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        it = newmap.entrySet().iterator();
        int a = 0;
        while (it.hasNext()) {
            Map.Entry<Double, List<BlkPoint>> entry = it.next();
            newTreemap.put(entry.getKey(), entry.getValue());
        }
//        System.out.println(newTreemap.size());
        Iterator<Map.Entry<Double, List<BlkPoint>>> newIt = newTreemap.entrySet().iterator();
        previousKey = -999.9;
        count2 = 0;
        while (newIt.hasNext()) {
            Map.Entry<Double, List<BlkPoint>> entry = newIt.next();
            if (Math.abs(entry.getKey() - previousKey) > 0.4 && Math.abs(entry.getKey() - previousKey) < 900 && count2 < 50) {
                count2++;
                stationCalculation.getRemainPoints().addAll(entry.getValue());
                newIt.remove();
            }
            previousKey = entry.getKey();
        }
        currentWall.getPoints().clear();
        for (Double coord : newTreemap.keySet()) {
            List<BlkPoint> currentGroup = newTreemap.get(coord);
            currentWall.getPoints().addAll(currentGroup);
        }
        return currentWall;
    }

    /**
     * 计算完，将数据存入数据库
     */
    private void updateStationData() {
        Long stationDataPk = stationDataDTO.getPk();
        //用于将对应墙的指标统计起来
        List<QuotaData> wallQuotaDataList = new ArrayList<>();
        //存入数据库
        for (WallDataDTO wallDataDTO : stationDataDTO.getWallDataDTOList()) {
            List<QuotaData> quotaDataList = wallDataDTO.getQuotaDataList();
            wallQuotaDataList.addAll(quotaDataList);
            //设置唯一标识
            wallDataDTO.setPk(SequencePrimaryKey.getSequence());
            wallDataDTO.setStationDataPk(stationDataPk);
            //新的图片路径,与服务器同步
            String serverImagePath = wallDataDTO.getImagePath().replace(Folder.PROJECT, "/imdroid-mapping");
            String serverImagePath2 = wallDataDTO.getImagePath2().replace(Folder.PROJECT, "/imdroid-mapping");
            wallDataDTO.setImagePath(serverImagePath);
            wallDataDTO.setImagePath2(serverImagePath2);
            //将数据保存到数据库
            wallDataMapper.insertWallData(wallDataDTO);
            for (QuotaData quotaData : quotaDataList) {
                quotaData.setPk(SequencePrimaryKey.getSequence());
                quotaData.setAssociatePk(wallDataDTO.getPk());
                quotaDataMapper.insertQuotaData(quotaData);
            }
        }
        List<QuotaData> stationQuotaDataList = groupIntegration(wallQuotaDataList, AssociateEnum.STATION_DATA.getCode());
        //将指标数据保存到数据库
        updateQuotaData(stationQuotaDataList, stationDataPk);
        for (QuotaData quotaData : stationDataDTO.getQuotaDataList()) {
            if (QuotaEnum.SQUARE.getCode().equals(quotaData.getQuotaType())) {
                continue;
            }
            quotaData.setPk(SequencePrimaryKey.getSequence());
            quotaData.setAssociatePk(stationDataPk);
            quotaDataMapper.insertQuotaData(quotaData);
        }
        //将测站更新
        stationDataDTO.setComplete(true);
        stationDataDTO.setCompleteTime(new Date());
        stationDataMapper.updateStationData(stationDataDTO);
        log.info("测站数据导入数据库");
    }

    private void updateTaskData(List<StationData> stationDataList) {
        //将对应测站的指标统计起来
        Long totalPoints = 0L;
        Integer completeNum = 0;
        List<QuotaData> stationQuotaDataList = new ArrayList<>();
        for (StationData stationData : stationDataList) {
            List<QuotaData> quotaDataList = quotaDataMapper.selectQuotaDataByAssociate(AssociateEnum.STATION_DATA.getCode(), stationData.getPk());
            stationQuotaDataList.addAll(quotaDataList);
            if (stationData.getComplete()) {
                completeNum++;
                totalPoints += stationData.getTotalPoints();
            }
        }
        List<QuotaData> taskQuotaDataList = groupIntegration(stationQuotaDataList, AssociateEnum.TASK_DATA.getCode());
        //将指标数据保存到数据库
        updateQuotaData(taskQuotaDataList, taskDataDTO.getPk());
        //将任务更新
        taskDataDTO.setTotalPoints(totalPoints);
        if (completeNum.equals(taskDataDTO.getStationNumber())) {
            taskDataDTO.setComplete(true);
            taskDataDTO.setCompleteTime(new Date());
        }
        taskDataMapper.updateTaskData(taskDataDTO);
        log.info("任务数据导入数据库");
    }

    private void updateQuotaData(List<QuotaData> quotaDatas, Long associatePk) {
        for (QuotaData quotaData : quotaDatas) {
            List<QuotaData> quotaDataList = quotaDataMapper.selectQuotaDataByQuotaType(quotaData.getAssociateType(), quotaData.getQuotaType(), associatePk);
            if (!quotaDataList.isEmpty() && quotaDataList.size() == 1) {
                QuotaData data = quotaDataList.get(0);
                quotaData.setPk(data.getPk());
                quotaData.setAssociatePk(associatePk);
                quotaDataMapper.updateQuotaData(quotaData);
            } else {
                quotaData.setPk(SequencePrimaryKey.getSequence());
                quotaData.setAssociatePk(associatePk);
                quotaDataMapper.insertQuotaData(quotaData);
            }
        }
    }

    @Override
    public boolean isCalculation() {
        return calculation;
    }

}
