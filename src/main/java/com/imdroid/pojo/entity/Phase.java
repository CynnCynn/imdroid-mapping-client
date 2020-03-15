package com.imdroid.pojo.entity;


import lombok.Data;

import java.io.Serializable;

/**
 * 阶段表
 * Created by saka947 on 2018/11/16.
 */
@Data
public class Phase implements Serializable {
    private Long pk;
    /*
     * 主阶段
     * */
    private String mainPhase;


    /*
     * 主阶段释义
     * */
    private String mainMeanings;


    /*
     * 子阶段
     * */
    private String secPhase;


    /*
     * 子阶段释义
     * */
    private String secMeanings;

}
