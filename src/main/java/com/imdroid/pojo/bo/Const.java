package com.imdroid.pojo.bo;


/**
 * @Description:常量类
 * @Author: iceh
 * @Date: create in 2018-09-05 14:50
 * @Modified By:
 */
public final class Const {
    private Const() {
    }

    //坐标（x,y,z)
    public interface Coordinate {
        String X = "X";
        String Y = "Y";
        String Z = "Z";
        String PHI = "Phi";
        String THETA = "Theta";
        String DIS_TO_X_AXIS = "DisToXAxis";
        String DIS_TO_Y_AXIS = "DisToYAxis";
        String DIS_TO_Z_AXIS = "DisToZAxis";
        String X_2 = "X2";
        String Y_2 = "Y2";
        String Z_2 = "Z2";
        String PHI_2 = "Phi_mod90";
    }


    public interface SubSize{
        int subPointsSize = 5000;
    }
    //轴（正半轴、负半轴）
    public interface Axis {
        String POSITIVE = "+";
        String NEGATIVE = "-";
    }

    //仪器误差
    public interface InstrumentDeviation {
        double DISTANCE = 0.002; //单位:m
        double DEGREE = 0.008; //单位:度
    }

    //许可误差
    public interface PermitDeviation {
        double LARGE = 0.001;//单位:m
        //        double MIDDLE = 0.001;
        double SMALL = 0.003 + InstrumentDeviation.DISTANCE;
    }

//    距离
    public interface Threshold{
    double CEILING_THRESHOLD = 0.1; // 天花往下CEILINGTHRESHOLD米
    double FLOOR_THRESHOLD = 0.01; // 地板往上FLOORTHRESHOLD米认为是墙
    double SAFE_THRESHOLD_UP = 0.1;
    double SAFE_THRESHOLD_LOW = 0.15; //安全范围为天花往下SAFETHRESHOLDUP~SAFETHRESHOLDLOW的区间
    double OUT_POINT_DISTANCE = 0.15; // 比标准距离多15cm的点为飘出点
    double OUT_POINT_DISTANCE_EXACT = 0.04;
    double HOLE_WIDTH_MIN = 0.4; // 洞口宽度至少有40cm
    double HOLE_HEIGHT_MIN = 0.4; // 洞口高度最小值
    double BASEBOARD_HEIGHT = 0.15; //踢脚线高度
    double BLK_MINDISTANCE = 0.9; //扫描仪精度很低的阈值
    }

    public interface HoleDeviation {
        double HEIGHT_DEVIATION = 0.01; // 洞口高度误差
        double WIDTH_DEVIATION = 0; //洞口宽度误差
    }

    //编码格式
    public interface Encoding {
        String GBK = "GBK";
        String UTF8 = "UTF-8";
    }

    //文件位置
    public interface Folder {
        String PROJECT = "D:/imdroid/mapping/client";
        String IMAGE = PROJECT + "/image";
        String DATA = PROJECT + "/data";
        String SCRIPT = PROJECT + "/script";
        String POINT_CLOUD = PROJECT + "/pointCloud";
        String SUBMIT = PROJECT + "/submit";
        String DEVICE_STATUS = PROJECT + "/deviceStatus";
        String RESULT = PROJECT + "/result";
    }

    //文件名
    public interface FileName {
        String EXPORT = "export";
        String BATTERY_LEVEL = "batteryLevel";
        String WORKING_CONDITION = "workingCondition";
    }

    //特殊平面命名
    public interface PlaneName {
        String CEILING = "ceil";
        String FLOOR = "floor";
    }


    //文件后缀名
    public interface Suffix {
        String TXT = ".txt";
        String JPG = ".jpg";
        String EXE = ".exe";
        String ZIP = ".zip";
    }

    //服务器路径
    public interface ServerAddress {
        //测试服务器
        String TEST = "https://jr.im-droid.com/imdroid-mapping";
        String LOCAL = "http://localhost:8080/imdroid-mapping";
        String MAPPING = TEST + "/mapping";
    }

    //服务器路径
    public interface Weights {
        //测试服务器
        int FLATNESS = 12;
        int VERTICAL = 12;
        int LEVELNESS = 15;
        int SQUARE = 4;
        int SECTION_SIZE = 2;//截面尺寸、门窗洞、阴阳角
//        int LEVELNESS = 15;
    }

    //语音路径
    public interface VoiceAddress {
        //正常流程语音
        String REGULAR_101 = Folder.PROJECT + "/voice/REGULAR_101.mp3";//开机完成
        String REGULAR_102 = Folder.PROJECT + "/voice/REGULAR_102.mp3"; //开始导入扫描数据
        String REGULAR_103 = Folder.PROJECT + "/voice/REGULAR_103.mp3";//导入扫描数据完成
        String REGULAR_104 = Folder.PROJECT + "/voice/REGULAR_104.mp3";//开始计算
        String REGULAR_105 = Folder.PROJECT + "/voice/REGULAR_105.mp3";//计算完成
        String REGULAR_106 = Folder.PROJECT + "/voice/REGULAR_106.mp3";//同步任务数据完成
        //错误语音提示
        String ERROR_201 = Folder.PROJECT + "/voice/ERROR_201.mp3";//找不到扫描文件，请重新扫描
        String ERROR_202 = Folder.PROJECT + "/voice/ERROR_202.mp3";//扫描仪电量过低，请更换电池
        String ERROR_203 = Folder.PROJECT + "/voice/ERROR_203.mp3";//扫描仪连接断开，请重新连接
        String ERROR_204 = Folder.PROJECT + "/voice/ERROR_204.mp3";//连接扫描仪失败
        String ERROR_205 = Folder.PROJECT + "/voice/ERROR_205.mp3";//扫描数据缺失，请重新扫描
        String ERROR_206 = Folder.PROJECT + "/voice/ERROR_206.mp3";//找不到该任务
        String ERROR_207 = Folder.PROJECT + "/voice/ERROR_207.mp3";//扫描数据缺失，请清理遮挡物后重新扫描
        String ERROR_208 = Folder.PROJECT + "/voice/ERROR_208.mp3";//上传计算结果失败
        String ERROR_209 = Folder.PROJECT + "/voice/ERROR_209.mp3";//连接网络失败
        String ERROR_210 = Folder.PROJECT + "/voice/ERROR_210.mp3";//工作站异常，请检修设备
        String ERROR_211 = Folder.PROJECT + "/voice/ERROR_211.mp3";//连接服务器失败
    }

    public interface WiFiAddress {
        /**
         * 密码路径
         */
        String PASSWORD_PATH = Folder.PROJECT + "/wlan/password/wake.txt";

        /**
         * 配置文件暂时存放路径
         */
        String PROFILE_TEMP_PATH = Folder.PROJECT + "/wlan/profile";

        /**
         * 破解成功wifi存放路径
         */
        String RESULT_PATH = Folder.PROJECT + "/wlan/record.txt";

        /**
         * 日志存放路径
         */
        String LOG_PATH = Folder.PROJECT + "/wlan/log.txt";
    }
}
