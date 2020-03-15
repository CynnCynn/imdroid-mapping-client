package com.imdroid.programSelfStart;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.imdroid.enums.AssociateEnum;
import com.imdroid.enums.BluetoothEnum;
import com.imdroid.pojo.bo.BluetoothData;
import com.imdroid.pojo.bo.BusinessException;
import com.imdroid.pojo.bo.Const;
import com.imdroid.pojo.bo.ResponseResult;
import com.imdroid.pojo.dto.StationDataDTO;
import com.imdroid.pojo.dto.WallDataDTO;
import com.imdroid.pojo.entity.QuotaData;
import com.imdroid.pojo.entity.StationData;
import com.imdroid.pojo.entity.TaskData;
import com.imdroid.pojo.entity.WallData;
import com.imdroid.service.TaskDataService;
import com.imdroid.service.TaskService;
import com.imdroid.utils.HttpClientUtil;
import com.imdroid.utils.SerialPortManager;
import com.imdroid.utils.WiFiUtil.Connector;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.imdroid.enums.BluetoothEnum.*;

/**
 * @Description:将串口变为蓝牙模块，项目启动时唤醒
 * @Author: iceh
 * @Date: create in 2019-01-03 16:00
 * @Modified By:
 */
@Slf4j
@Component
@Order(value = 1)
public class BluetoothSerialPort implements ApplicationRunner {
    public static SerialPort bluetoothSerialPort;
    private static volatile StringBuffer message = new StringBuffer();
    private static int mActualOrder = -1;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskDataService taskDataService;

    private LinkedList<StationDataDTO> queue = new LinkedList<>();

    @Override
    public void run(ApplicationArguments args) throws BusinessException {
        List<String> availablePorts = SerialPortManager.findPorts();
        if (availablePorts == null || availablePorts.size() <= 0) {
            statusNotificationUtil.updateStatus(210);
            log.error("找不到串口");
            return;
//            throw new BusinessException("无可用端口");
        }
        //判断串口
        difSerialPort(availablePorts);

        // 增加jvm关闭的钩子来关闭监听
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                SerialPortManager.closePort(bluetoothSerialPort);
                log.info("关闭蓝牙串口成功");
            } catch (Exception e) {
                log.error("关闭蓝牙串口失败", e);
            }
        }));
    }

    //判断串口类型
    private void difSerialPort(List<String> availablePorts) {
        String com = availablePorts.get(0);
        availablePorts.remove(0);
        AtomicBoolean isDisplay = new AtomicBoolean(false);
        try {
            SerialPort serialPort = SerialPortManager.openPort(com, 38400);
            if (serialPort != null) {
                System.out.println("test 串口已打开");
            }

            // 添加串口监听
            if (serialPort != null) {
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (!isDisplay.get()) {
                            serialPort.close();
                            System.out.println("test 不是工控屏串口");
                            log.info(com + "不是工控屏串口");
                            //打开蓝牙串口
                            openSerialPort(com, 9600);
                            if (availablePorts.size() > 0)
                                difSerialPort(availablePorts);
                            timer.cancel();
                        }
                    }
                };
                //发送HELLO
                String hello = "HELLO\r\n";
                SerialPortManager.sendToPort(serialPort, hello.getBytes());
                SerialPortManager.addListener(serialPort, () -> {
                    // 读取串口数据
                    byte[] data = SerialPortManager.readFromPort(serialPort);
                    System.out.println(new String(data));
                    if (new String(data).equals("WORLD\r\n")) {
                        isDisplay.set(true);
                        timer.cancel();
                        serialPort.close();
                        log.info(com + "是工控屏串口");
                        DisplaySerialPort.openSerialPort(com);
                        if (availablePorts.size() > 0)
                            difSerialPort(availablePorts);
                    }
                });
                timer.schedule(timerTask, 50);
            }
        } catch (PortInUseException e) {
            System.out.println("test 串口已被占用！");
        }
    }

    private void openSerialPort(String com, int baudRate) {
        if (com == null || com.equals("")) {
            throw new BusinessException("无效端口");
        }
        // 检查串口名称是否获取正确
        try {
            bluetoothSerialPort = SerialPortManager.openPort(com, baudRate);

            if (bluetoothSerialPort != null) {
                log.info("打开蓝牙串口成功");
            }
        } catch (PortInUseException e) {
            log.error("蓝牙串口已被占用");
        }

        // 添加串口监听
        SerialPortManager.addListener(bluetoothSerialPort, // 读取串口数据
                this::dataAvailable);

    }

    /**
     * 校验蓝牙传入信息是否可以解析
     *
     * @param message
     * @return
     */
    private boolean isBluetoothData(StringBuffer message) {
        String content = message.toString();
        if (content.contains("CONNECTED")){
            log.info("蓝牙连接成功");
            message.delete(0, message.length());
            return false;
        }
        try {
            BluetoothData bluetoothData = JSONObject.parseObject(content, BluetoothData.class);
            if (null != bluetoothData) {
                BluetoothData data = new BluetoothData();
                data.setType(I_AM_HERE.getType());
                return subcontracting(data);
            } else {
                return false;
            }
        } catch (Exception e) {
            if (content.length() > 200) {
                message.delete(0, message.length());
            }
            return false;
        }
    }

    private void dataAvailable() {
        byte[] data = SerialPortManager.readFromPort(bluetoothSerialPort);
        message.append(new String(data, StandardCharsets.UTF_8));
        log.info(message.toString());
        if (isBluetoothData(message)) {
            try {
                BluetoothData bluetoothData = JSONObject.parseObject(message.toString(), BluetoothData.class);
                //转换后将数据置空
                if (SCAN_COMPLETE.getType().equals(bluetoothData.getType())) {
                    checkScanComplete(bluetoothData);
                } else if (TASK_DATA_QUOTA.getType().equals(bluetoothData.getType())) {
                    sendTaskDataQuota(bluetoothData);
                } else if (STATION_DATA_QUOTA.getType().equals(bluetoothData.getType())) {
                    sendStationDataQuota(bluetoothData);
                } else if (WALL_DATA_QUOTA.getType().equals(bluetoothData.getType())) {
                    sendWallDataQuota(bluetoothData);
                } else if (TEAM_VIEW.getType().equals(bluetoothData.getType())) {

                }
            } finally {
                message.delete(0, message.length());
            }
        }
    }
    @Autowired
    private TaskDataPrepare taskDataPrepare;

    private void checkScanComplete(BluetoothData bluetoothData) {
//        if (Connector.isConnectBLK360()) {
            //解析传来的json
            JSONObject obj = (JSONObject) bluetoothData.getObj();
            Long stationDataPk = obj.getLong("stationDataPk");
            String stationAlias = obj.getString("stationAlias");
            Integer stationType = obj.getInteger("stationType");
            Integer actualOrder = obj.getInteger("actualOrder");
            StationDataDTO stationDataDTO = new StationDataDTO();
            stationDataDTO.setPk(stationDataPk);
            stationDataDTO.setStationType(stationType);
            stationDataDTO.setStationAlias(stationAlias);
            stationDataDTO.setActualOrder(actualOrder);
            if (HttpClientUtil.isServerNormal()) {
                String stationMac = stationDataDTO.getPk()+"";
                taskDataPrepare.init(stationMac, true);
            }
            //记录站序号
            mActualOrder = actualOrder;

            if (taskService.isCalculation()) {
                log.info("计算中接到扫描完成蓝牙指令，忽略此指令");
                return;
            } else {
                //TODO 只后要加上阻塞
                taskDataService.saveStationData(stationDataDTO);
                taskService.prepareData(stationDataDTO);
            }
            try {
//          再使用导出脚本，导出完成会触发文件监控
                openExportScript();
                while (!FileMonitor.isReceiveFile() && !WatchDirService.isScriptError()) {
                    Thread.sleep(3000);
                }
                WatchDirService.setScriptError(false);
                FileMonitor.setReceiveFile(false);
                sendReceiveFile(actualOrder);
            } catch (InterruptedException e) {
                log.error("获取文件导入状态失败", e);
            }
//        } else {
//            statusNotificationUtil.updateStatus(204);
//            log.info("扫描仪wifi连接失败");
//        }
    }

    private void sendReceiveFile(Integer order) {
        BluetoothData bluetoothData = new BluetoothData();
        bluetoothData.setType(BluetoothEnum.RECEIVE_FILE.getType());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("order", order);
        bluetoothData.setObj(jsonObject);
        String receiveFile = JSON.toJSONString(bluetoothData);
        //收到文件，要发给小程序一个回复
        SerialPortManager.sendToPort(bluetoothSerialPort, receiveFile.getBytes());
//        subcontracting(bluetoothData);
    }

    private void sendTaskDataQuota(BluetoothData bluetoothData) {
        JSONObject obj = (JSONObject) bluetoothData.getObj();
        Long taskDataPk = obj.getLong("pk");
        ResponseResult responseResult = new ResponseResult(true, null, null, null);
        TaskData taskData = taskDataService.findTaskData(taskDataPk);
        if (null == taskData) {
            responseResult.setSuccess(false);
            responseResult.setErrorMsg("该任务不存在");
            log.error("任务指标请求失败,失败taskDataPk:" + taskDataPk);
        } else {
            List<QuotaData> quotaDataList = taskDataService.selectQuotaDataList(AssociateEnum.TASK_DATA.getCode(), taskDataPk);
            if (quotaDataList.isEmpty()) {
                responseResult.setSuccess(false);
                responseResult.setErrorMsg("该任务没有指标数据，可能任务还未完成");
                log.error("任务指标请求失败,失败taskDataPk:" + taskDataPk);
            } else {
                responseResult.setObj(quotaDataList);
            }
        }
        bluetoothData.setObj(responseResult);
//        String taskDataQuota = JSON.toJSONString(bluetoothData);
        //回复小程序任务指标数据
//        SerialPortManager.sendToPort(bluetoothSerialPort, taskDataQuota.getBytes());
        subcontracting(bluetoothData);
    }

    private void sendStationDataQuota(BluetoothData bluetoothData) {
        JSONObject obj = (JSONObject) bluetoothData.getObj();
        Long taskDataPk = obj.getLong("taskDataPk");
        ResponseResult responseResult = new ResponseResult(true, null, null, null);
        TaskData taskData = taskDataService.findTaskData(taskDataPk);
        if (null == taskData) {
            responseResult.setSuccess(false);
            responseResult.setErrorMsg("该任务不存在");
            log.error("测站数据请求失败,失败taskDataPk:" + taskDataPk);
        } else {
            List<StationDataDTO> stationDataDTOList = new ArrayList<>();
            List<StationData> stationDataList = taskDataService.findStationDataList(taskDataPk);
            if (stationDataList.isEmpty()) {
                responseResult.setSuccess(false);
                responseResult.setErrorMsg("该任务没有测站数据，可能任务还未开始");
                log.error("测站数据请求失败,失败taskDataPk:" + taskDataPk);
            } else {
                for (StationData stationData : stationDataList) {
                    //将查询结果存入dto
                    List<QuotaData> quotaDataList = taskDataService.selectQuotaDataList(AssociateEnum.STATION_DATA.getCode(), stationData.getPk());
                    StationDataDTO stationDataDTO = new StationDataDTO();
                    BeanUtils.copyProperties(stationData, stationDataDTO);
                    stationDataDTO.setQuotaDataList(quotaDataList);
                    stationDataDTOList.add(stationDataDTO);
                }
                responseResult.setObj(stationDataDTOList);
            }
        }
        bluetoothData.setObj(responseResult);
//        String stationDataQuota = JSON.toJSONString(bluetoothData);
        //回复小程序测站指标数据
//        SerialPortManager.sendToPort(bluetoothSerialPort, stationDataQuota.getBytes());
        subcontracting(bluetoothData);
    }

    private void sendWallDataQuota(BluetoothData bluetoothData) {
        JSONObject obj = (JSONObject) bluetoothData.getObj();
        Long stationDataPk = obj.getLong("stationDataPk");
        Integer quotaType = obj.getInteger("quotaType");
        ResponseResult responseResult = new ResponseResult(true, null, null, null);
        StationData stationData = taskDataService.findStationData(stationDataPk);
        if (null == stationData) {
            responseResult.setSuccess(false);
            responseResult.setErrorMsg("该测站不存在");
            log.error("墙面数据请求失败,失败stationDataPk:" + stationDataPk);
        } else {
            List<WallDataDTO> wallDataDTOList = new ArrayList<>();
            List<WallData> wallDataList = taskDataService.findWallDataList(stationDataPk);
            if (wallDataList.isEmpty()) {
                responseResult.setSuccess(false);
                responseResult.setErrorMsg("该测站没有墙面数据，可能计算还未结束");
                log.error("墙面数据请求失败,失败stationDataPk:" + stationDataPk);
            } else {
                for (WallData wallData : wallDataList) {
                    List<QuotaData> quotaDataList = taskDataService.selectQuotaDataList(AssociateEnum.WALL_DATA.getCode(), quotaType, wallData.getPk());
                    WallDataDTO wallDataDTO = new WallDataDTO();
                    BeanUtils.copyProperties(wallData, wallDataDTO);

                    StationData newStationData = new StationData();
                    newStationData.setCompleteTime(stationData.getCompleteTime());
                    wallDataDTO.setStationData(newStationData);

                    wallDataDTO.setQuotaDataList(quotaDataList);
                    wallDataDTOList.add(wallDataDTO);
                }

                responseResult.setObj(wallDataDTOList);
            }
        }
        bluetoothData.setObj(responseResult);
//        String wallDataQuota = JSON.toJSONString(bluetoothData);
        //回复小程序测站墙面数据
        subcontracting(bluetoothData, 50);
    }

    public static void reminding(int code) {
        if (mActualOrder != -1) {
            BluetoothData bluetoothData = new BluetoothData();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("order", mActualOrder);
            bluetoothData.setObj(jsonObject);
            switch (code) {
                case 103://导入完成
                    bluetoothData.setType(IMPORT_COMPLETE.getType());
                    jsonObject.put("meaning", IMPORT_COMPLETE.getMeaning());
                    break;
                case 105://计算完成
                    bluetoothData.setType(CALCULATION_COMPLETE.getType());
                    jsonObject.put("meaning", CALCULATION_COMPLETE.getMeaning());
                    break;
                case 201://找不到扫描文件，请重新扫描
                    bluetoothData.setType(ERROR_NOT_FIND_DATA.getType());
                    jsonObject.put("meaning", ERROR_NOT_FIND_DATA.getMeaning());
                    break;
                case 202://扫描仪电量过低，请更换电池
                    bluetoothData.setType(ERROR_LOW_BATTERY_LEVEL.getType());
                    jsonObject.put("meaning", ERROR_LOW_BATTERY_LEVEL.getMeaning());
                    break;
                case 203://扫描仪连接断开，请重新连接
                    bluetoothData.setType(ERROR_CONNNECT.getType());
                    jsonObject.put("meaning", ERROR_CONNNECT.getMeaning());

                    break;
                case 204://连接扫描仪失败
                    bluetoothData.setType(ERROR_WIFI.getType());
                    jsonObject.put("meaning", ERROR_WIFI.getMeaning());
                    break;
                case 205://扫描数据异常，请重新扫描
                    bluetoothData.setType(ERROR_DATA.getType());
                    jsonObject.put("meaning", ERROR_DATA.getMeaning());
                    break;
            }
            bluetoothData.setObj(jsonObject);
            if (bluetoothData.getType() != null && bluetoothSerialPort != null) {
                String receiveFile = JSON.toJSONString(bluetoothData);
                //给小程序发送警告
                SerialPortManager.sendToPort(bluetoothSerialPort, receiveFile.getBytes());
            }

        }

    }

    public boolean subcontracting(Object object) {
        return subcontracting(object, 0);
    }

    /**
     * 数据分包发
     *
     * @param object
     * @param delay
     */
    public boolean subcontracting(Object object, long delay) {
        String jsonString = JSON.toJSONString(object);
        int perLength = 15;//每段字数
        int totalLength = jsonString.length();//测试文本长度
        int tail = totalLength % perLength;//结尾段
        int packageNum = totalLength / perLength + (tail > 0 ? 1 : 0);//总包数
        for (int i = 0; i < packageNum; i++) {
            String subString = (i == packageNum - 1) ? jsonString.substring(i * perLength) : jsonString.substring(i * perLength, (i + 1) * perLength);
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    log.error("分包发数据给蓝牙出错", e);
                    return false;
                }
            }
            if (bluetoothSerialPort != null)
                SerialPortManager.sendToPort(bluetoothSerialPort, subString.getBytes());
        }
        return true;
    }

    /**
     * 打开cyclone导出用脚本程序
     */
    private void openExportScript() {
        //去打开脚本
        File exportScript = new File(Const.Folder.SCRIPT, Const.FileName.EXPORT + Const.Suffix.EXE);
        //判断是否存在文件夹
        if (!exportScript.getParentFile().exists()) {
            exportScript.getParentFile().mkdirs();
        }
        if (exportScript.exists()) {
            try {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(exportScript.getAbsolutePath());
                log.info("打开脚本完成");
            } catch (IOException e) {
                throw new BusinessException("导出文件的脚本打开失败");
            }
        } else {
            throw new BusinessException("该脚本文件不存在:" + exportScript);
        }
    }
}
