package com.imdroid.service;

import com.imdroid.pojo.entity.*;

import java.util.List;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2019-01-05 11:01
 * @Modified By:
 */
public interface TaskDataService {

    void saveTaskData(TaskData taskData);

    void saveStationData(StationData stationData);

    void saveWallData(WallData wallData);

    void saveQuotaData(QuotaData quotaData);

    void saveQuota(Quota quota);

    void clearDatabase();

    TaskData findTaskData(Long pk);

    StationData findStationData(Long stationDataPk);

    List<StationData> findStationDataList(Long taskDataPk);

    List<WallData> findWallDataList(Long stationDataPk);

    void deleteTaskData(Long pk);

    List<QuotaData> selectQuotaDataList(Integer type, Long pk);

    List<QuotaData> selectQuotaDataList(Integer associateType, Integer quotaType, Long pk);
}
