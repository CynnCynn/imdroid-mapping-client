package com.imdroid.pojo.bo;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2018-11-26 15:11
 * @Modified By:
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Exception e) {
        super(e);
    }
}
