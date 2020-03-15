package com.imdroid.utils.WiFiUtil;


import com.imdroid.pojo.bo.Const;
import com.imdroid.utils.WiFiUtil.cmd.CheckTask;
import com.imdroid.utils.WiFiUtil.cmd.Ssid;
import com.imdroid.utils.WiFiUtil.cmd.WlanExecute;
import com.imdroid.utils.WiFiUtil.generator.ProfileGenerator;
import com.imdroid.utils.WiFiUtil.util.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.imdroid.utils.WiFiUtil.cmd.WlanExecute.connect;
import static com.imdroid.utils.WiFiUtil.cmd.WlanExecute.disconnect;


/**
 * 连接配置文件
 */
@Slf4j
public class Connector {
    /**
     * 批处理密码数量
     */
    public static final int BATH_SIZE = 10;

    /**
     * 要ping的域名
     */
    public static final String PING_DOMAIN = "www.baidu.com";

    private ExecutorService checkThreadPool = Executors.newFixedThreadPool(4);

    /**
     * 生成配置文件
     */
    private static void genProfile(String ssid) {
        ProfileGenerator profileGenerator = new ProfileGenerator(ssid, Const.WiFiAddress.PASSWORD_PATH);
        profileGenerator.genProfile();
    }

    /**
     * 根据密码验证配置文件
     */
    private String check(String ssid) {
        String password = null;
        List<String> passwordList;
        int counter = 0;
        outer:
        while (true) {
            int start = counter * BATH_SIZE;
            int end = (counter + 1) * BATH_SIZE - 1;
            passwordList = FileUtils.readLine(Const.WiFiAddress.PASSWORD_PATH, start, end);
            if (passwordList != null && passwordList.size() > 0) {
                for (String item : passwordList) {
                    CheckTask task = new CheckTask(ssid, item);
                    Future<Boolean> checked = checkThreadPool.submit(task);
                    try {
                        if (checked.get()) {
                            password = item;
                            break outer;
                        }
                    } catch (Exception e) {
                        System.out.println("校验出错：ssid=>" + ssid + ",passord=>" + null);
                    }
                }
                counter++;
            } else {
                break;
            }
        }
        return password;
    }

    /**
     * 检测是否连上扫描仪WiFi
     */
    public static boolean isConnectBLK360() {
        boolean isConnect = false;
        int num = 0;
        while (!isConnect) {
            try {
                //获取所有接口，并放进枚举集合中，然后使用Collections.list()将枚举集合转换为ArrayList集合
                Enumeration<NetworkInterface> enu = NetworkInterface.getNetworkInterfaces();
                ArrayList<NetworkInterface> arr = Collections.list(enu);
                for (NetworkInterface ni : arr) {
                    String intName = ni.getName();//获取接口名
                    String DisplayName = ni.getDisplayName();
                    //获取每个接口中的所有ip网络接口集合，因为可能有子接口
                    ArrayList<InetAddress> inets = Collections.list(ni.getInetAddresses());
                    for (InetAddress inet : inets) {
                        //只筛选ipv4地址，否则会同时得到Ipv6地址  Qualcomm Intel(R)
                        if (inet instanceof Inet4Address && DisplayName.contains("Intel(R)")) {
                            String ssid = WlanExecute.getInterfaces();
                            if (ssid != null && ssid.contains("BLK360")) {
                                isConnect = true;
                                log.info("InetfaceName:" + intName + "| DisplayName:" + DisplayName);
                            } else if (ssid == null || !ssid.contains("BLK360")) {
                                disconnect();
                            }

                        }
                    }
                }
                if (!isConnect) {
//                    if (connect("BLK360-3595159"))
//                        return true;
                    if (num > 20) {
                        return false;
                    }
                    /*
                      整体步骤如下：
                      <p>
                      -- step1. 扫所有可用的，信号较好的WIFI
                      -- step2. 根据密码批量生成配置文件
                      -- step3. 根据密码一个一个配置文件验证，直到找到正确的密码
                     */
                    // step1
                    List<Ssid> ssidList = WlanExecute.listSsid();
                    if (ssidList.size() > 0) {
                        boolean isFindBLK = false;
                        for (Ssid ssid : ssidList) {
                            log.info(ssid.getName());
                            //判断可用列表中的BLK360设备，信号大于50 的尝试连接
                            if (ssid.getName().contains("BLK360") && ssid.getdB() > 50) {
                                isFindBLK = true;
                                log.info("尝试连接" + ssid.getName());
                                if (connect(ssid.getName()))
                                    return true;
                                long start = System.currentTimeMillis();
                                // step2
                                genProfile(ssid.getName());
                                // step3
                                Connector connector = new Connector();
                                String password = connector.check(ssid.getName());

                                long end = System.currentTimeMillis();
                                System.out.println("耗时：" + (end - start) / 1000 + "秒");
                                if (password != null) {
                                    log.info("尝试连接成功，" + ssid.getName() + " 密码：" + password);
                                    String record = "ssid:" + ssid + ",password:" + password;
                                    FileUtils.appendToFile(Const.WiFiAddress.RESULT_PATH, record);
                                } else {
                                    log.info("尝试连接扫描仪失败");
                                    return false;
                                }
                            }
                        }
                        if (!isFindBLK) {
                            log.info("工作站未开WIFI");
                            num++;
                            Thread.sleep(2000);
                        }
                    } else {
                        log.info("未找到附近WIFI");
                        num++;
                        Thread.sleep(2000);
                    }

                }
            } catch (SocketException | InterruptedException e) {
                e.printStackTrace();
                num++;
            }
        }
        return true;
    }
}
