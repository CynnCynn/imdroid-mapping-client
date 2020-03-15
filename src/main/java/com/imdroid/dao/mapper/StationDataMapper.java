package com.imdroid.dao.mapper;

import com.imdroid.pojo.entity.StationData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2019-01-05 11:04
 * @Modified By:
 */
@Mapper
public interface StationDataMapper {

    void insertStationData(StationData stationData);

    void updateStationData(StationData stationData);

    void deleteStationData(Long pk);

    void deleteAllStationData();

    StationData selectStationData(Long pk);

    List<StationData> selectStationDataByTaskDataPk(Long taskDataPk);

}
