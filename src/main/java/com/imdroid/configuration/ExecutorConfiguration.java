package com.imdroid.configuration;

import com.imdroid.pojo.bo.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description:用于监控文件变化的线程池
 * @Author: iceh
 * @Date: create in 2018-12-12 16:30
 * @Modified By:
 */
@Configuration
@EnableAsync
@Slf4j
public class ExecutorConfiguration {
    @Bean
    public Executor fileMonitorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(2);
        // 设置最大线程数
        executor.setMaxPoolSize(2);
        // 设置队列容量
        executor.setQueueCapacity(5);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(0);
        // 设置默认线程名称
        executor.setBeanName("fileMonitor");
        // 设置拒绝策略
        RejectedExecutionHandler rejectedExecutionHandler = (r, executor1) -> {
            throw new BusinessException("线程池已占满，无法分配新的线程");
        };
        executor.setRejectedExecutionHandler(rejectedExecutionHandler);
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 线程池初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor calculationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(1);
        // 设置最大线程数
        executor.setMaxPoolSize(5);
        // 设置队列容量
        executor.setQueueCapacity(5);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(300);
        // 设置默认线程名称
        executor.setThreadNamePrefix("calculation-");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 线程池初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor nonCalculationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(5);
        // 设置最大线程数
        executor.setMaxPoolSize(10);
        // 设置队列容量
        executor.setQueueCapacity(10);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(60);
        // 设置默认线程名称
        executor.setThreadNamePrefix("non-calculation-");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 线程池初始化
        executor.initialize();
        return executor;
    }
}
