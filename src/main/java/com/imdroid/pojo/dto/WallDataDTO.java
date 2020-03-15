package com.imdroid.pojo.dto;


import com.imdroid.pojo.entity.QuotaData;
import com.imdroid.pojo.entity.StationData;
import com.imdroid.pojo.entity.WallData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:数据传输用
 * @Author: iceh
 * @Date: create in 2018-11-12 16:02
 * @Modified By:
 */
@Data
public class WallDataDTO extends WallData {
    private List<QuotaData> quotaDataList = new ArrayList<>();

    private StationData stationData;
}
