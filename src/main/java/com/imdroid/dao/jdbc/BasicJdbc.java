package com.imdroid.dao.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

import java.util.List;

/**
 * @Description: 统一所用到的jdbcTemplate
 * @Author: iceh
 * @Date: create in 2018-10-27 15:53
 * @Modified By:
 */
public abstract class BasicJdbc<T> {
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 由于mybatis在大批量插入数据的效率太低，故采用jdbc做大批量插入数据
     *
     * @param list
     */
    public void batchInsert(List<T> list) {
        String sql = prepareBatchInsertSql();
        SqlParameterSource[] sources = SqlParameterSourceUtils.createBatch(list.toArray());
        namedParameterJdbcTemplate.batchUpdate(sql, sources);
    }

    protected abstract String prepareBatchInsertSql();
}
