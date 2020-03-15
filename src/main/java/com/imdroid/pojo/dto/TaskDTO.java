package com.imdroid.pojo.dto;

import com.imdroid.pojo.entity.*;
import lombok.Data;

import java.util.List;

/**
 * 用于包装各类list数据并返回工作站
 * Created By skyline in 2019/1/4 11:05
 */
@Data
public class TaskDTO {
    /**
     * 封装任务数据
     */
    private List<TaskData> taskDataList;
    /**
     * 封装测站数据
     * 方案数据中已包括阶段以及户型的数据
     */
    private List<StationData> stationDataList;
    /**
     * 封装指标
     */
    private List<Quota> quotaList;

    /**
     * 封装墙面数据
     */
    private List<WallData> wallDataList;
    /**
     * 封装指标数据
     */
    private List<QuotaData> quotaDataList;
}
