package com.imdroid.pojo.entity;


import com.imdroid.utils.BigDecimalUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description:指标数据表
 * @Author: iceh
 * @Date: create in 2018-11-16 16:32
 * @Modified By:
 */
@Data
public class QuotaData implements Serializable {
    private Long pk;
    /*
     * 指标数据
     * */
    private Double quotaValue;
    /*
     * 指标类型
     * */
    private Integer quotaType;
    /*
     * 参与计算点数
     * */
    private Long detectionPoints;
    /*
     * 合格点数
     * */
    private Long passPoints;
    /**
     * 原始数据
     */
    private String manuals;
    /*
     * 关联类型
     * */
    private Integer associateType;
    /*
     * 关联pk
     * */
    private Long associatePk;
    /*
     * 关联墙面名
     * */
    private String associateName;

    /**
     * 指标别名
     */
    private String quotaAlias;

    /**
     * 指标标准值
     */
    private Double standardValue;

    public QuotaData() {
    }

    public QuotaData(Double quotaValue, Integer quotaType, Long detectionPoints, Long passPoints, Integer associateType) {
        this.quotaValue = BigDecimalUtil.scale(quotaValue, 4);
        this.quotaType = quotaType;
        this.detectionPoints = detectionPoints;
        this.passPoints = passPoints;
        this.associateType = associateType;
    }

    public QuotaData(Double quotaValue, Integer quotaType, Long detectionPoints, Long passPoints, Integer associateType, String associateName) {
        this.quotaValue = BigDecimalUtil.scale(quotaValue, 4);
        this.quotaType = quotaType;
        this.detectionPoints = detectionPoints;
        this.passPoints = passPoints;
        this.associateType = associateType;
        this.associateName = associateName;
    }
}
