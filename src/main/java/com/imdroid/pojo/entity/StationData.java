package com.imdroid.pojo.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 测站数据表
 * @Author: iceh
 * @Date: create in 2018-11-16 15:44
 * @Modified By:
 */
@Data
public class StationData implements Serializable {
    private Long pk;
    /*
     * 高度
     * */
    private Double height;
    /*
     * 开间
     * */
    private Double bay = 0.0;
    /*
     * 进深
     * */
    private Double depth = 0.0;
    /*
     * 总点数
     * */
    private Long totalPoints;
    /*
     * 得分率
     * */
    private Double scoringRate;
    /*
     * 实际顺序
     * */
    private Integer actualOrder;
    /*
     * 是否完成
     * */
    private Boolean complete;
    /*
     * 完成时间
     * */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;
    /**
     * 测站名
     */
    private String stationAlias;
    /**
     * 测站类型
     */
    private Integer stationType;
    /*
     * 任务数据pk
     * */
    private Long taskDataPk;
    /**
     * 点云文件路径
     */
    private String pointCloudFile;
}
