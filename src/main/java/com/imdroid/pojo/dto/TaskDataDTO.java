package com.imdroid.pojo.dto;


import com.imdroid.pojo.entity.QuotaData;
import com.imdroid.pojo.entity.TaskData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:数据传输用
 * @Author: iceh
 * @Date: create in 2018-11-12 19:55
 * @Modified By:
 */
@Data
public class TaskDataDTO extends TaskData {
    /**
     * 测站数据数组
     */
    private List<StationDataDTO> stationDataDTOList = new ArrayList<>();

    /**
     * 指标数据数组
     */
    private List<QuotaData> quotaDataList = new ArrayList<>();


    public void addStationDataDTO(StationDataDTO stationDataDTO) {
        this.stationDataDTOList.add(stationDataDTO);
    }

    public void addQuotaData(QuotaData quotaData) {
        this.quotaDataList.add(quotaData);
    }
}
