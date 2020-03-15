package com.imdroid.utils;

import lombok.NonNull;

import java.math.BigDecimal;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2018-11-10 12:35
 * @Modified By:
 */
public class BigDecimalUtil {

    public static double scale(@NonNull Double val, int scale) {
        return new BigDecimal(val).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
