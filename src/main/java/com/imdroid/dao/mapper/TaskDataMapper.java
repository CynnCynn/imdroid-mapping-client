package com.imdroid.dao.mapper;

import com.imdroid.pojo.entity.TaskData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2019-01-05 11:04
 * @Modified By:
 */
@Mapper
public interface TaskDataMapper {

    void insertTaskData(TaskData taskData);

    void updateTaskData(TaskData taskData);

    void deleteTaskData(Long pk);

    void deleteAllTaskData();

    TaskData selectTaskData(Long pk);

    List<TaskData> selectTaskDataByTaskPk(Long taskPk);

}
