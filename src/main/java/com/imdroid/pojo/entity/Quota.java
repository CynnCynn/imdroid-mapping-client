package com.imdroid.pojo.entity;


import lombok.Data;

import java.io.Serializable;

/**
 * @Description:指标设置表
 * @Author: iceh
 * @Date: create in 2018-11-17 11:53
 * @Modified By:
 */
@Data
public class Quota implements Serializable {
    private Long pk;
    /**
     * 指标类型
     */
    private Integer quotaType;
    /**
     * 指标默认值
     */

    private Double defaults;
    /**
     * 指标下限
     */
    private Double lowerLimit;
    /**
     * 指标上限
     */
    private Double upperLimit;
    /**
     * 指标权重
     */
    private Double weight;

}
