package com.imdroid.controller;

import com.imdroid.pojo.bo.Const;
import com.imdroid.pojo.dto.StationDataDTO;
import com.imdroid.service.TaskDataService;
import com.imdroid.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;


/**
 * @Description:任务控制层
 * @Author: iceh
 * @Date: create in 2018-09-04 15:20
 * @Modified By:
 */
@Slf4j
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskController {
    @Autowired
    private final TaskService taskService;

    @Autowired
    private TaskDataService taskDataService;


    @RequestMapping(value = "/end")
    public String endTask(Long taskPk) {
        return "task end success";
    }


}
