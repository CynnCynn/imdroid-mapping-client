package com.imdroid.pojo.entity;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description:任务数据表
 * @Author: iceh
 * @Date: create in 2018-11-12 19:55
 * @Modified By:
 */
@Data
public class TaskData implements Serializable {
    private Long pk;
    /*
     * 高度
     * */
    private Double height;
    /*
     * 开间
     * */
    private Double bay;
    /*
     * 进深
     * */
    private Double depth;
    /*
     * 总点数
     * */
    private Long totalPoints;
    /*
     * 得分率
     * */
    private Double scoringRate;
    /**
     * 测站数量
     */
    private Integer stationNumber;
    /*
     * 实际顺序
     * */
//    private Integer actualOrder;
    /*
     * 是否完成
     * */
    private Boolean complete;
    /*
     * 完成时间
     * */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;
    /*
     * 任务pk
     * */
    private Long taskPk;


}
