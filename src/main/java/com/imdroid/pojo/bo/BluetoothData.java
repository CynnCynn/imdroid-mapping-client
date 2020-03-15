package com.imdroid.pojo.bo;

import lombok.Data;

/**
 * @Description:用于处理来自蓝牙的协定数据
 * @Author: iceh
 * @Date: create in 2019-01-05 09:36
 * @Modified By:
 */
@Data
public class BluetoothData {
    private String type;
    private Object obj;
}
