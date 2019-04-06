package org.casbin.adapter;

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

    private final static String INIT_TABLE_SQL = "CREATE TABLE CASBIN_RULE (" +
            "  PTYPE VARCHAR2(255) NOT NULL ," +
            "  V0 VARCHAR2(255) DEFAULT NULL ," +
            "  V1 VARCHAR2(255) DEFAULT NULL ," +
            "  V2 VARCHAR2(255) DEFAULT NULL ," +
            "  V3 VARCHAR2(255) DEFAULT NULL ," +
            "  V4 VARCHAR2(255) DEFAULT NULL ," +
            "  V5 VARCHAR2(255) DEFAULT NULL" +
            ");";
    private final static String DROP_TABLE_SQL = "DROP TABLE CASBIN_RULE;";
    private final static String CHECK_TABLE_EXIST = "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = UPPER('CASBIN_RULE');";

    public OracleAdapter(JdbcTemplate jdbcTemplate, boolean autoCreateTable) {
        super(jdbcTemplate, autoCreateTable);
    }

    @Override
    protected void initTable() {
        Integer count = jdbcTemplate.queryForObject(CHECK_TABLE_EXIST, Integer.class);
        if (count != null && count == 0) {
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
