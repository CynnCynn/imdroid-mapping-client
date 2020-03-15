package com.imdroid.service;


import com.imdroid.pojo.dto.StationDataDTO;
import lombok.NonNull;

import java.io.File;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2018-09-05 14:29
 * @Modified By:
 */

public interface TaskService {

    boolean isCalculation();

    void analyzeTxt(@NonNull File file, String encoding);

    void prepareData(StationDataDTO stationDataDTO);

}
