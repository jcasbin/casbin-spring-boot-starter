package org.casbin.adapter;

import org.casbin.spring.boot.autoconfigure.properties.CasbinExceptionProperties;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: PostgreSQLAdapter
 * @package org.casbin.adapter
 * @description:
 * @date 2021/05/28 09:30
 */
public class PostgreSQLAdapter extends JdbcAdapter {

    private final static String INIT_TABLE_SQL = "CREATE TABLE IF NOT EXISTS CASBIN_RULE (" +
            " id SERIAL PRIMARY KEY, "+
            " ptype varchar(255) NOT NULL," +
            " v0 varchar(255) DEFAULT NULL," +
            " v1 varchar(255) DEFAULT NULL," +
            " v2 varchar(255) DEFAULT NULL," +
            " v3 varchar(255) DEFAULT NULL," +
            " v4 varchar(255) DEFAULT NULL," +
            " v5 varchar(255) DEFAULT NULL" +
            ")";

    public PostgreSQLAdapter(JdbcTemplate jdbcTemplate, CasbinExceptionProperties casbinExceptionProperties, boolean autoCreateTable) {
        super(jdbcTemplate, casbinExceptionProperties, autoCreateTable);
    }

    @Override
    protected String getInitTableSql() {
        return INIT_TABLE_SQL;
    }
}