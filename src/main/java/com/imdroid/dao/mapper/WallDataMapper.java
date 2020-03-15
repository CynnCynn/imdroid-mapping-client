package com.imdroid.dao.mapper;

import com.imdroid.pojo.entity.WallData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2019-01-05 11:04
 * @Modified By:
 */
@Mapper
public interface WallDataMapper {

    void insertWallData(WallData wallData);

    void updateWallData(WallData wallData);

    void deleteWallData(Long pk);

    void deleteAllWallData();

    void deleteWallDataByStationDataPk(Long stationDataPk);

    WallData selectWallData(Long pk);

    List<WallData> selectWallDataByStationDataPk(Long stationDataPk);

    List<WallData> selectCompleteWallDataByStationDataPk(Long stationDataPk);
}
