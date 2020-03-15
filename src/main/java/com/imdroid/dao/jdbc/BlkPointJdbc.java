package com.imdroid.dao.jdbc;


import com.imdroid.pojo.entity.BlkPoint;
import org.springframework.stereotype.Repository;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2018-10-27 15:56
 * @Modified By:
 */
@Repository
public class BlkPointJdbc extends BasicJdbc<BlkPoint> {

    @Override
    protected String prepareBatchInsertSql() {
        return "insert into blk_point (x,y,z,red,green,blue,intensity) values (:x,:y,:z,:red,:green,:blue,:intensity)";
    }
}
