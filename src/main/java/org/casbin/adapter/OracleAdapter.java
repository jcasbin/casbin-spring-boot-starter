package org.casbin.adapter;

import org.casbin.spring.boot.autoconfigure.properties.CasbinExceptionProperties;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: OracleAdapter
 * @package org.casbin.adapter
 * @description:
 * @date 2019/4/4 16:03
 */
public class OracleAdapter extends JdbcAdapter {

    private static final String INIT_TABLE_SQL = "CREATE TABLE casbin_rule (" +
            "  ID int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, "+
            "  PTYPE VARCHAR2(255) NOT NULL ," +
            "  V0 VARCHAR2(255) DEFAULT NULL ," +
            "  V1 VARCHAR2(255) DEFAULT NULL ," +
            "  V2 VARCHAR2(255) DEFAULT NULL ," +
            "  V3 VARCHAR2(255) DEFAULT NULL ," +
            "  V4 VARCHAR2(255) DEFAULT NULL ," +
            "  V5 VARCHAR2(255) DEFAULT NULL" +
            ")";
    private static final String DROP_TABLE_SQL = "DROP TABLE casbin_rule";
    private static final String CHECK_TABLE_EXIST = "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = UPPER('casbin_rule')";

    public OracleAdapter(JdbcTemplate jdbcTemplate, CasbinExceptionProperties casbinExceptionProperties, boolean autoCreateTable) {
        super(jdbcTemplate, casbinExceptionProperties, autoCreateTable);
    }

    public OracleAdapter(JdbcTemplate jdbcTemplate, CasbinExceptionProperties casbinExceptionProperties, String tableName, boolean autoCreateTable) {
        super(jdbcTemplate, casbinExceptionProperties, tableName, autoCreateTable);
    }
 
    @Override
    protected void initTable() {
        Integer count = jdbcTemplate.queryForObject(getCheckTableExistSql(), Integer.class);
        if (count != null && count == 0) {
            super.initTable();
        }
    }
    
    protected String getCheckTableExistSql() {
    	return renderActualSql(CHECK_TABLE_EXIST);
    }

    @Override
    protected String getInitTableSql() {
        return renderActualSql(INIT_TABLE_SQL);
    }

    @Override
    protected String getDropTableSql() {
        return renderActualSql(DROP_TABLE_SQL);
    }
}
