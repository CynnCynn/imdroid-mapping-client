package com.imdroid.programSelfStart;


import com.imdroid.pojo.bo.Const;
import com.imdroid.utils.SerialPortManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.imdroid.programSelfStart.DisplaySerialPort.mDisplayData;
import static com.imdroid.programSelfStart.DisplaySerialPort.mSerialport;

@Slf4j
@Component
@Order(value = 2)
public class WatchDirService implements ApplicationRunner {
    private WatchService watchService;
    private boolean notDone = true;
    private static volatile boolean scriptError = false;

    @Override
    @Async("fileMonitorExecutor")
    public void run(ApplicationArguments args) {
        init();
        start();
    }

    private void init() {
        Path path = Paths.get(Const.Folder.DEVICE_STATUS);
        try {
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        log.info("打开电量监控");
        while (notDone) {
            try {
                WatchKey watchKey = watchService.poll(60, TimeUnit.SECONDS);
                System.out.print("change");
                if (watchKey != null) {
                    List<WatchEvent<?>> events = watchKey.pollEvents();
                    for (WatchEvent event : events) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }
                        Path path = ((WatchEvent<Path>) event).context();
                        File file = new File(path.toFile().getAbsolutePath() + "/" + ((WatchEvent<Path>) event).context());
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            System.out.println("create " + path.getFileName());
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            System.out.println("modify " + path.getFileName());
                            if (file.getName().startsWith(Const.FileName.BATTERY_LEVEL)) {
                                log.info("更新电量");
                                readTxtFile(Const.Folder.DEVICE_STATUS + "/" + Const.FileName.BATTERY_LEVEL + Const.Suffix.TXT, 1);
                            } else if (file.getName().startsWith(Const.FileName.WORKING_CONDITION)) {
                                readTxtFile(Const.Folder.DEVICE_STATUS + "/" + Const.FileName.WORKING_CONDITION + Const.Suffix.TXT, 2);
                            }
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            System.out.println("delete " + path.getFileName());
                        }
                    }
                    if (!watchKey.reset()) {
                        System.out.println("exit watch server");
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * 功能：Java读取txt文件的内容
     * 步骤：1：先获得文件句柄
     * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
     * 3：读取到输入流后，需要读取生成字节流
     * 4：一行一行的输出。readline()。
     * 备注：需要考虑的是异常情况
     *
     * @param filePath
     */
    private void readTxtFile(String filePath, int type) {
        try {
            System.out.println(filePath);
            String encoding = "GBK";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    System.out.println(lineTxt);
                    if (type == 1) {
                        String[] level = lineTxt.split(" ");
                        log.info("电量：" + Integer.valueOf(level[0]));
                        if (mSerialport != null && mDisplayData != null) {
                            mDisplayData.setBatteryLevel(Integer.valueOf(level[0]));
                            mDisplayData.setState((byte) 102);
                            SerialPortManager.sendToPort(mSerialport, mDisplayData.getDataBytes());
                        }
                    } else if (type == 2) {
                        int state = Integer.valueOf(lineTxt);
                        statusNotificationUtil.updateStatus(state);
                    }
                }
                read.close();
            } else {
                log.info("找不到指定的文件");
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            log.info("读取文件内容出错");
            e.printStackTrace();
        }

    }

    public static boolean isScriptError() {
        return scriptError;
    }

    public static void setScriptError(boolean scriptError) {
        WatchDirService.scriptError = scriptError;
    }
}
