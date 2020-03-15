package com.imdroid.programSelfStart;

import com.imdroid.pojo.entity.DisplayData;
import com.imdroid.utils.SerialPortManager;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisplaySerialPort {
    public static SerialPort mSerialport;
    public static DisplayData mDisplayData;

    static void openSerialPort(String com) {
        // 检查串口名称是否获取正确
        if (com == null || com.equals("")) {
            System.out.println("test 请输入正确端口号");
            return;
        }
        try {
            SerialPort serialport = SerialPortManager.openPort(com, 38400);
            if (serialport != null) {
                log.info("工控屏串口打开");
                mSerialport = serialport;
                //初始化工控屏
                mDisplayData = new DisplayData(0, 0, 0, DisplayData.WAITING, 0xff);
//                SerialPortManager.sendToPort(serialport, mDisplayData.getDataBytes());
            }
        } catch (PortInUseException e) {
            System.out.println("test 串口已被占用！");
        }
    }
}
