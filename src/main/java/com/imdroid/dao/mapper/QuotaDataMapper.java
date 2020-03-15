package com.imdroid.dao.mapper;

import com.imdroid.pojo.entity.QuotaData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2019-01-05 11:04
 * @Modified By:
 */
@Mapper
public interface QuotaDataMapper {

    void insertQuotaData(QuotaData quotaData);

    void updateQuotaData(QuotaData quotaData);

    void deleteQuotaData(Long pk);

    void deleteAllQuotaData();

    void deleteQuotaDataByAssociate(@Param("associateType") Integer associateType, @Param("associatePk") Long associatePk);

    QuotaData selectQuotaData(Long pk);

    List<QuotaData> selectQuotaDataByAssociate(@Param("associateType") Integer associateType, @Param("associatePk") Long associatePk);

    List<QuotaData> selectQuotaDataByQuotaType(@Param("associateType") Integer associateType, @Param("quotaType") Integer quotaType, @Param("associatePk") Long associatePk);

}
