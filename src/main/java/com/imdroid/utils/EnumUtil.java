package com.imdroid.utils;

import com.imdroid.enums.CodeEnum;

/**
 * Created by linge on 2018/5/5.
 */
public class EnumUtil {

    public static <T extends CodeEnum> T getByCode(Integer code, Class<T> enumClass) {

        for (T each : enumClass.getEnumConstants()) {
            if (each.getCode().equals(code)) {
                return each;
            }
        }
        return null;
    }
}
