package com.imdroid.programSelfStart;

import com.alibaba.fastjson.JSON;
import com.imdroid.pojo.bo.ResponseResult;
import com.imdroid.pojo.dto.TaskDTO;
import com.imdroid.pojo.entity.*;
import com.imdroid.service.TaskDataService;
import com.imdroid.utils.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.imdroid.pojo.bo.Const.ServerAddress;

/**
 * @Description:对所需主数据，提前请求服务器拉取下来
 * @Author: iceh
 * @Date: create in 2019-01-04 17:56
 * @Modified By:
 */
@Slf4j
@Component
@Order(value = 4)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskDataPrepare implements ApplicationRunner {
    private final TaskDataService taskDataService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        for (int i = 0; i < 3; i++) {
//            if (HttpClientUtil.isServerNormal()) {
//                String stationMac = "1457894178";
//                //TODO 后续应改为通过硬件 获取机器码
//                init(stationMac, true);
//                log.info("数据初始化完成");
//                break;
//            }
//        }
    }

    /**
     * 根据stationMac从服务器请求当天的可执行任务
     *
     * @param stationMac
     * @param clear
     */
    public void init(String stationMac, boolean clear) {
        Map<String, String> param = new HashMap<>();
        param.put("stationDataPk", stationMac);
        String data = HttpClientUtil.doGet(ServerAddress.MAPPING + "/workstation/getTaskData.do", param);
        ResponseResult responseResult = JSON.parseObject(data, ResponseResult.class);
        if (responseResult.isSuccess()) {
            TaskDTO taskDTO = JSON.parseObject(responseResult.getObj().toString(), TaskDTO.class);
            if (clear) {
                taskDataService.clearDatabase();
            }
            dataUpdate(taskDTO);
        } else {
            log.error("请求服务器数据失败,失败原因：" + responseResult.getErrorMsg());
        }
    }


    /**
     * @param taskDTO
     */
    public void dataUpdate(TaskDTO taskDTO) {
        //数据重新导入
        List<Quota> quotaList = taskDTO.getQuotaList();
        List<TaskData> taskDataList = taskDTO.getTaskDataList();
        List<StationData> stationDataList = taskDTO.getStationDataList();
        List<WallData> wallDataList = taskDTO.getWallDataList();
        List<QuotaData> quotaDataList = taskDTO.getQuotaDataList();
        //要按这样的顺序去存
        for (Quota quota : quotaList) {
            taskDataService.saveQuota(quota);
        }
        for (WallData wallData : wallDataList) {
            taskDataService.saveWallData(wallData);
        }
        for (QuotaData quotaData : quotaDataList) {
            taskDataService.saveQuotaData(quotaData);
        }
        for (StationData stationData : stationDataList) {
            taskDataService.saveStationData(stationData);
        }
        for (TaskData taskData : taskDataList) {
            taskDataService.saveTaskData(taskData);
        }
    }
}
