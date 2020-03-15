package com.imdroid.dao.mapper;

import com.imdroid.pojo.entity.Quota;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2019-01-05 11:04
 * @Modified By:
 */
@Mapper
public interface QuotaMapper {

    void insertQuota(Quota quota);

    void updateQuota(Quota quota);

    void deleteQuota(Long pk);

    void deleteAllQuota();

    Quota selectQuota(Long pk);

    Quota selectByQuotaType(Integer quotaType);

}
