package com.imdroid.utils;

import lombok.NonNull;

import java.math.BigDecimal;

/**
 * @Description:数组工具类
 * @Author: iceh
 * @Date: create in 2018-09-10 00:45
 * @Modified By:
 */
public class ArrayUtil {
    /**
     * 设置double数组中元素取到小数点后几位
     *
     * @param array
     * @param scale 小数位数
     * @return
     */
    public static double[] setScale(@NonNull double[] array, int scale) {
        double[] newArray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            BigDecimal bg = new BigDecimal(array[i]);
            newArray[i] = bg.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        return newArray;
    }

    /**
     * 将数组中元素全部乘以倍数
     *
     * @param array
     * @param multiple 倍数
     * @return
     */
    public static double[] multiply(@NonNull double[] array, double multiple) {
        double[] newArray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i] * multiple;
        }
        return newArray;
    }

    /**
     * 将数组中元素全部取幂次方
     *
     * @param array
     * @param power 幂
     * @return
     */
    public static double[] pow(@NonNull double[] array, double power) {
        double[] newArray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = Math.pow(array[i], power);
        }
        return newArray;
    }

    /**
     * 合并数组
     *
     * @param firstArray  第一个数组
     * @param secondArray 第二个数组
     * @return 合并后的数组
     */
    public static byte[] concat(byte[] firstArray, byte[] secondArray) {
        if (firstArray == null || secondArray == null) {
            return null;
        }
        byte[] bytes = new byte[firstArray.length + secondArray.length];
        System.arraycopy(firstArray, 0, bytes, 0, firstArray.length);
        System.arraycopy(secondArray, 0, bytes, firstArray.length, secondArray.length);
        return bytes;
    }
}
