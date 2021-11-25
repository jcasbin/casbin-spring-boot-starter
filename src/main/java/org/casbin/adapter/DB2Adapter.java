package org.casbin.adapter;

import org.casbin.spring.boot.autoconfigure.properties.CasbinExceptionProperties;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author liuyunsh@cn.ibm.com
 * @version V1.0
 * @date 2019/4/23
 */
public class DB2Adapter extends JdbcAdapter {

    private static final String CHECK_TABLE_SQL = "select 1 from syscat.tables where tabname = upper('CASBIN_RULE')";

    private static final  String DROP_TABLE_SQL = "DROP TABLE CASBIN_RULE";

    private static final String INIT_TABLE_SQL = "CREATE TABLE CASBIN_RULE (" +
            "  ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1), "+
            "  PTYPE VARCHAR(255) NOT NULL ," +
            "  V0 VARCHAR(255) DEFAULT NULL ," +
            "  V1 VARCHAR(255) DEFAULT NULL ," +
            "  V2 VARCHAR(255) DEFAULT NULL ," +
            "  V3 VARCHAR(255) DEFAULT NULL ," +
            "  V4 VARCHAR(255) DEFAULT NULL ," +
            "  V5 VARCHAR(255) DEFAULT NULL ," +
            "  PRIMARY KEY (ID) " +
            ")";

    /**
     * @param jdbcTemplate
     * @param autoCreateTable
     */
    public DB2Adapter(JdbcTemplate jdbcTemplate, CasbinExceptionProperties casbinExceptionProperties, boolean autoCreateTable) {
        super(jdbcTemplate, casbinExceptionProperties, autoCreateTable);
    }

    @Override
    protected void initTable() {
        try {
            jdbcTemplate.queryForObject(CHECK_TABLE_SQL, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            super.initTable();
        }
    }

    @Override
    protected String getInitTableSql() {
        return INIT_TABLE_SQL;
    }

    @Override
    protected String getDropTableSql() {
        return DROP_TABLE_SQL;
    }

}
