package com.imdroid.programSelfStart;

import com.imdroid.pojo.bo.BusinessException;
import com.imdroid.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static com.imdroid.pojo.bo.Const.Encoding.GBK;
import static com.imdroid.pojo.bo.Const.Folder;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * @Description:监控文件夹，处理cyclone导出的点云文件
 * @Author: iceh
 * @Date: create in 2018-11-26 11:04
 * @Modified By:
 */
@Slf4j
@Component
@Order(value = 3)
public class FileMonitor implements ApplicationRunner {
    private static volatile boolean receiveFile = false;
    @Autowired
    TaskService taskService;

    private WatchService watchService;
    private Path path;
    public static boolean isReceiveFile() {
        return receiveFile;
    }

    public static void setReceiveFile(boolean receiveFile) {
        FileMonitor.receiveFile = receiveFile;
    }

    @Override
    @Async("fileMonitorExecutor")
    public void run(ApplicationArguments args) {
        Path pointCloudPath = Paths.get(Folder.POINT_CLOUD);
        register(pointCloudPath);
        monitor();
        //线程池会在jvm关闭时被回收，故不需要用钩子来关闭监听
    }

    /**
     * 开启文件监控的一些准备工作，由于watchService是阻塞线程，故需要用线程池开个子线程来调度
     *
     * @param path
     */
    private void register(Path path) {
        try {
            File folder = path.toFile();
            if (!folder.exists()) {
                folder.mkdirs();
            }
            this.path = path;
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, OVERFLOW, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            log.info("开启文件监控成功");
        } catch (IOException e) {
            log.error("开启文件监控失败", e);
        }
    }

    /**
     * 文件监控的实际处理
     */
    private void monitor() {
        WatchEvent.Kind<?> previousKind = null;
        File previousFile = null;
        while (true) {
            //获取事件变化
            try {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    WatchEvent<Path> pathWatchEvent = (WatchEvent<Path>) event;
                    File file = new File(path.toFile().getAbsolutePath() + "/" + pathWatchEvent.context());
//                    if (file.getName().startsWith("pc_") && ENTRY_CREATE.equals(kind)) {
//                        log.info("test");
//                        taskService.analyzeTxt(file, GBK);
//                        receiveFile = true;
//                    }
                    if (ENTRY_MODIFY.equals(kind)) {
                        //cyclone创建点云文件的触发事件顺序为 新增 -》修改 -》修改
                        boolean isCycloneExportPointCloud = file.getName().startsWith("pc_") && file.equals(previousFile) && ENTRY_MODIFY.equals(previousKind);
                        if (isCycloneExportPointCloud) {
                            log.info("cyclone导入点云文件: " + file);
                            receiveFile = true;
                            taskService.analyzeTxt(file, GBK);
                        }
                    } else if (OVERFLOW.equals(kind)) {
                        throw new BusinessException("文件夹: " + file.getParentFile() + "已存满，请及时清理");
                    }
                    //由于reset会清空上一次事件，故记录上一次事件与文件路径来进行校验
                    previousKind = kind;
                    previousFile = file;
                    if (!key.reset()) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                log.error("文件监控异常中断", e);
            }
        }
    }
}
