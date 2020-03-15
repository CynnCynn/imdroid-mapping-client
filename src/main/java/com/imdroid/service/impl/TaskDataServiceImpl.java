package com.imdroid.service.impl;

import com.imdroid.dao.mapper.*;
import com.imdroid.enums.AssociateEnum;
import com.imdroid.pojo.entity.*;
import com.imdroid.service.TaskDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2019-01-05 11:02
 * @Modified By:
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskDataServiceImpl implements TaskDataService {
    private final QuotaMapper quotaMapper;
    private final TaskDataMapper taskDataMapper;
    private final StationDataMapper stationDataMapper;
    private final WallDataMapper wallDataMapper;
    private final QuotaDataMapper quotaDataMapper;

    @Override
    public void saveTaskData(TaskData taskData) {
        Long taskDataPk = taskData.getPk();
        TaskData data = taskDataMapper.selectTaskData(taskDataPk);
        if (null != data) {
            taskDataMapper.updateTaskData(taskData);
        } else {
            taskDataMapper.insertTaskData(taskData);
        }
    }

    @Override
    public void saveStationData(StationData stationData) {
        Long stationDataPk = stationData.getPk();
        StationData data = stationDataMapper.selectStationData(stationDataPk);
        if (null != data) {
            stationDataMapper.updateStationData(stationData);
            if (!data.getComplete()) {
                wallDataMapper.deleteWallDataByStationDataPk(stationDataPk);
                quotaDataMapper.deleteQuotaDataByAssociate(AssociateEnum.STATION_DATA.getCode(), stationDataPk);
            }
        } else {
            stationDataMapper.insertStationData(stationData);
        }
    }

    @Override
    public void saveWallData(WallData wallData) {
        WallData data = wallDataMapper.selectWallData(wallData.getPk());
        if (null != data) {
            wallDataMapper.updateWallData(wallData);
        } else {
            wallDataMapper.insertWallData(wallData);
        }
    }


    @Override
    public void saveQuotaData(QuotaData quotaData) {
        QuotaData data = quotaDataMapper.selectQuotaData(quotaData.getPk());
        if (null != data) {
            quotaDataMapper.updateQuotaData(quotaData);
        } else {
            quotaDataMapper.insertQuotaData(quotaData);
        }
    }

    @Override
    public void saveQuota(Quota quota) {
        Quota newQuota = quotaMapper.selectQuota(quota.getPk());
        if (null != newQuota) {
            quotaMapper.updateQuota(quota);
        } else {
            quotaMapper.insertQuota(quota);
        }
    }

    @Override
    public void clearDatabase() {
        //将所有表清空
        quotaMapper.deleteAllQuota();
        taskDataMapper.deleteAllTaskData();
        stationDataMapper.deleteAllStationData();
        wallDataMapper.deleteAllWallData();
        quotaDataMapper.deleteAllQuotaData();
    }

    @Override
    public TaskData findTaskData(Long pk) {
        return taskDataMapper.selectTaskData(pk);
    }

    public StationData findStationData(Long stationDataPk) {
        return stationDataMapper.selectStationData(stationDataPk);
    }

    public List<StationData> findStationDataList(Long taskDataPk) {
        return stationDataMapper.selectStationDataByTaskDataPk(taskDataPk);
    }

    @Override
    public List<WallData> findWallDataList(Long stationDataPk) {
        return wallDataMapper.selectWallDataByStationDataPk(stationDataPk);
    }

    public List<QuotaData> selectQuotaDataList(Integer associateType, Long pk) {
        return quotaDataMapper.selectQuotaDataByAssociate(associateType, pk);
    }

    public List<QuotaData> selectQuotaDataList(Integer associateType, Integer quotaType, Long pk) {
        return quotaDataMapper.selectQuotaDataByQuotaType(associateType, quotaType, pk);
    }

    @Override
    public void deleteTaskData(Long pk) {
        taskDataMapper.deleteTaskData(pk);
    }
}
