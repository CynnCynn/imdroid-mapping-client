package com.imdroid.pojo.dto;
import com.imdroid.pojo.entity.QuotaData;
import com.imdroid.pojo.entity.StationData;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 数据传输用
 * @Author: iceh
 * @Date: create in 2018-11-16 15:44
 * @Modified By:
 */
@Data
public class StationDataDTO extends StationData {
    private List<WallDataDTO> wallDataDTOList = new ArrayList<>();

    private List<QuotaData> quotaDataList = new ArrayList<>();

    public void addWallDataDTO(WallDataDTO wallDataDTO) {
        this.wallDataDTOList.add(wallDataDTO);
    }

    public void addQuotaData(QuotaData quotaData) {
        this.quotaDataList.add(quotaData);
    }

    public void addQuotaDataList(List<QuotaData> quotaDataList) {
        this.quotaDataList.addAll(quotaDataList);
    }

}
