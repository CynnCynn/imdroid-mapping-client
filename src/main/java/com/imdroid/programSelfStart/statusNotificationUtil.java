package com.imdroid.programSelfStart;

import com.imdroid.pojo.bo.Const;
import com.imdroid.utils.SerialPortManager;
import com.imdroid.utils.VoiceUtil;
import lombok.extern.slf4j.Slf4j;

import static com.imdroid.programSelfStart.DisplaySerialPort.mDisplayData;
import static com.imdroid.programSelfStart.DisplaySerialPort.mSerialport;

@Slf4j
public class statusNotificationUtil {

    public static void updateStatus(int code) {
        //更新到工控屏
        if (mSerialport != null && mDisplayData != null) {
            mDisplayData.setState((byte) (code));
            SerialPortManager.sendToPort(mSerialport, mDisplayData.getDataBytes());
        }
        //通知到微信小程序
        BluetoothSerialPort.reminding(code);
        //语音提示
        switch (code) {
            case 101:
                VoiceUtil.play(Const.VoiceAddress.REGULAR_101);
                log.info("更新处理状态：开机完成");
                break;
            case 102:
                VoiceUtil.play(Const.VoiceAddress.REGULAR_102);
                log.info("更新处理状态：开始导入数据");
                break;
            case 103:
                VoiceUtil.play(Const.VoiceAddress.REGULAR_103);
                log.info("更新处理状态：导入数据完成");
                break;
            case 104:
                VoiceUtil.play(Const.VoiceAddress.REGULAR_104);
                log.info("更新处理状态：开始计算");
                break;
            case 105:
                VoiceUtil.play(Const.VoiceAddress.REGULAR_105);
                log.info("更新处理状态：计算完成");
                break;
            case 106:
                VoiceUtil.play(Const.VoiceAddress.REGULAR_106);
                log.info("更新处理状态：同步任务数据完成");
                break;
            case 201:
                VoiceUtil.play(Const.VoiceAddress.ERROR_201);
                log.info("更新处理状态：找不到扫描文件，请重新扫描");
                break;
            case 202:
                VoiceUtil.play(Const.VoiceAddress.ERROR_202);
                log.info("更新处理状态：扫描仪电量过低，请更换电池");
                break;
            case 203:
                VoiceUtil.play(Const.VoiceAddress.ERROR_203);
                log.info("更新处理状态：扫描仪连接断开，请重新连接");
                break;
            case 204:
                VoiceUtil.play(Const.VoiceAddress.ERROR_204);
                log.info("更新处理状态：连接扫描仪失败");
                break;
            case 205:
                VoiceUtil.play(Const.VoiceAddress.ERROR_205);
                log.info("更新处理状态：扫描数据异常，请重新扫描");
                break;
            case 206:
                VoiceUtil.play(Const.VoiceAddress.ERROR_206);
                log.info("更新处理状态：找不到该任务");
                break;
            case 207:
                VoiceUtil.play(Const.VoiceAddress.ERROR_207);
                log.info("更新处理状态：天花地板缺失，请清理后重新扫描");
                break;
            case 208:
                VoiceUtil.play(Const.VoiceAddress.ERROR_208);
                log.info("更新处理状态：上传计算结果失败");
                break;
            case 209:
                VoiceUtil.play(Const.VoiceAddress.ERROR_209);
                log.info("更新处理状态：连接网络失败");
                break;
            case 210:
                VoiceUtil.play(Const.VoiceAddress.ERROR_210);
                log.info("更新处理状态：工作站异常，请检修设备");
                break;
            case 211:
                VoiceUtil.play(Const.VoiceAddress.ERROR_211);
                log.info("更新处理状态：连接服务器失败");
                break;
        }
    }
}
