package com.imdroid.utils.WiFiUtil.cmd;


import com.imdroid.pojo.bo.Const;
import com.imdroid.utils.WiFiUtil.Connector;
import com.imdroid.utils.WiFiUtil.config.Command;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * wlan 命令行执行器
 */
@Slf4j
public class WlanExecute {

    /**
     * 校验WLAN配置文件是否正确
     * <p>
     * 校验步骤为：
     * ---step1 添加配置文件
     * ---step3 连接wifi
     * ---step3 ping校验
     */
    synchronized boolean check(String ssid, String password) {
        System.out.println("check : " + password);
        try {
            String profileName = password + ".xml";
            if (addProfile(profileName)) {
                if (connect(ssid)) {
                    Thread.sleep(50);
//                    if (ping()) {
                    return true;
//                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 列出所有信号较好的ssid
     *
     * @return 所有ssid
     */
    public static List<Ssid> listSsid() {
        List<Ssid> wifiList = new ArrayList<>();
        String cmd = Command.SHOW_NETWORKS;
        List<String> result = execute(cmd, null);
        if (result != null && result.size() > 0) {
            System.out.println("整合信息");
            log.info("整合wifi信息");
            log.info(result.toString());
            Ssid ssid = new Ssid();
            for (String s : result) {
//                log.info(s);
                try {
                    if (s.contains("SSID") && !s.contains("BSSID")) {
                        if (ssid.getdB() != 0) {
                            wifiList.add(ssid);
                            ssid = new Ssid();
                        }
                        ssid.setName(s.split(":")[1].substring(1));
                    } else if (s.contains("加密") || s.contains("Encryption")) {
                        ssid.setAuth(s.split(":")[1].substring(1));
                    } else if (s.contains("信号") || s.contains("Signal")) {
                        int dB = Integer.valueOf(s.split(":")[1].substring(1, 3));
                        ssid.setdB(dB);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }

            }
            if (ssid.getdB() != 0) {
                wifiList.add(ssid);
            }
            System.out.println(wifiList);
            if (wifiList.size() > 1) {
                int size = wifiList.size();
                for (int i = 0; i < size - 1; i++) {//外层循环控制排序趟数
                    for (int j = 0; j < size - 1 - i; j++) {//内层循环控制每一趟排序多少次
                        if (wifiList.get(j).getdB() < wifiList.get(j + 1).getdB()) {
                            Ssid temp = wifiList.get(j);
                            wifiList.add(j, wifiList.get(j + 1));
                            wifiList.remove(j + 1);
                            wifiList.add(j + 1, temp);
                            wifiList.remove(j + 2);
                        }
                    }
                }
            }
        }
        log.info(wifiList.toString());
        return wifiList;
    }

    /**
     * 添加配置文件
     *
     * @param profileName 添加配置文件
     */
    private static boolean addProfile(String profileName) {
        String cmd = Command.ADD_PROFILE.replace("FILE_NAME", profileName);
        List<String> result = execute(cmd, Const.WiFiAddress.PROFILE_TEMP_PATH);
        if (result != null && result.size() > 0) {
            return result.get(0).contains("添加到接口") || result.get(0).contains("Profile");
        }
        return false;
    }

    /**
     * 连接wifi
     *
     * @param ssid 连接wifi
     */
    public static boolean connect(String ssid) {
        boolean connected = false;
        String cmd = Command.CONNECT.replace("SSID_NAME", ssid);
        List<String> result = execute(cmd, null);
        if (result != null && result.size() > 0) {
            if (result.get(0).contains("已成功完成") || result.get(0).contains("successfully")) {
                connected = true;
            }
        }
        return connected;
    }

    /**
     * 断开wifi
     */
    public static boolean disconnect() {
        boolean disconnected = false;
        String cmd = Command.DISCONNECT;
        List<String> result = execute(cmd, null);
        if (result != null && result.size() > 0) {
            if (result.get(0).contains("已成功完成") || result.get(0).contains("successfully")) {
                disconnected = true;
            }
        }
        return disconnected;
    }

    /**
     * ping 校验
     */
    private static boolean ping() {
        boolean pinged = false;
        String cmd = "ping " + Connector.PING_DOMAIN;
        List<String> result = execute(cmd, null);
        if (result != null && result.size() > 0) {
            for (String item : result) {
                if (item.contains("来自")) {
                    pinged = true;
                    break;
                }
            }
        }
        return pinged;
    }

    /**
     * 获取WiFi属性
     * 返回 SSID
     */
    public static String getInterfaces() {
        String cmd = Command.SHOW_INTERFACE;
        List<String> result = execute(cmd, null);
        if (result != null && result.size() > 0) {
            for (String s : result) {
                if (s.contains("SSID") && !s.contains("BSSID")) {
                    return s.split(":")[1].substring(1);
                }
            }
        }
        return null;
    }

    /**
     * 执行器
     *
     * @param cmd      CMD命令
     * @param filePath 需要在哪个目录下执行
     */
    private static List<String> execute(String cmd, String filePath) {
        Process process;
        List<String> result = new ArrayList<>();
        try {
            if (filePath != null) {
                process = Runtime.getRuntime().exec(cmd, null, new File(filePath));
            } else {
                process = Runtime.getRuntime().exec(cmd);
            }
            BufferedReader bReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "gbk"));
            String line;
            while ((line = bReader.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("cmd执行错误：" + e.getMessage());
        }
        return result;
    }
}