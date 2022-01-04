package org.casbin.adapter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.casbin.exception.CasbinAdapterException;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.FilteredAdapter;
import org.casbin.spring.boot.autoconfigure.properties.CasbinExceptionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author shy
 * @version V1.1
 * @title: JdbcAdapter
 * @package org.casbin.adapter
 * @description:
 * @date 2020/12/23 16:50
 */
public class JdbcAdapter implements FilteredAdapter {

    private static final Logger logger = LoggerFactory.getLogger(JdbcAdapter.class);
    
    public static final String DEFAULT_TABLE_NAME = "casbin_rule";
	
	private String tableName;

    private static final String INIT_TABLE_SQL = "CREATE TABLE IF NOT EXISTS casbin_rule (" +
            "    id int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, "+
            "    ptype varchar(255) NOT NULL," +
            "    v0    varchar(255) DEFAULT NULL," +
            "    v1    varchar(255) DEFAULT NULL," +
            "    v2    varchar(255) DEFAULT NULL," +
            "    v3    varchar(255) DEFAULT NULL," +
            "    v4    varchar(255) DEFAULT NULL," +
            "    v5    varchar(255) DEFAULT NULL" +
            ")";
    private static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS casbin_rule";
    private static final String DELETE_TABLE_CONTENT_SQL = "DELETE FROM casbin_rule";
    private static final String LOAD_POLICY_SQL = "SELECT * FROM casbin_rule";
    private static final String INSERT_POLICY_SQL = "INSERT INTO casbin_rule(ptype, v0, v1, v2, v3, v4, v5) VALUES(?, ?, ?, ?, ?, ?, ?)";
    private static final String DELETE_POLICY_SQL = "DELETE FROM casbin_rule WHERE ptype = ? ";

    protected JdbcTemplate jdbcTemplate;
    protected CasbinExceptionProperties casbinExceptionProperties;

    private volatile boolean isFiltered = false;

    public JdbcAdapter(JdbcTemplate jdbcTemplate, CasbinExceptionProperties casbinExceptionProperties, boolean autoCreateTable) {
    	this(jdbcTemplate,casbinExceptionProperties,DEFAULT_TABLE_NAME,autoCreateTable);
    }
    
    public JdbcAdapter(JdbcTemplate jdbcTemplate, CasbinExceptionProperties casbinExceptionProperties, String tableName, boolean autoCreateTable) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
        this.casbinExceptionProperties = casbinExceptionProperties;
        if (autoCreateTable) {
            initTable();
        }
    }

    /**
     * the filter class.
     * Enforcer only accept this filter currently.
     */
    public static class Filter {
        public String[] p;
        public String[] g;
    }
    
    protected String renderActualSql(String sql) {
    	return sql.replace(DEFAULT_TABLE_NAME, tableName);
    }
 
    protected String getInitTableSql() {
        return renderActualSql(INIT_TABLE_SQL);
    }

    protected String getDropTableSql() {
        return renderActualSql(DROP_TABLE_SQL);
    }

    protected String getLoadPolicySql() {
        return renderActualSql(LOAD_POLICY_SQL);
    }

    protected String getDeleteTableContentSql() {
        return renderActualSql(DELETE_TABLE_CONTENT_SQL);
    }
    
    protected String getInsertPolicySql() {
    	return renderActualSql(INSERT_POLICY_SQL);
    }
    
    protected String getDeletePolicySql() {
    	return renderActualSql(DELETE_POLICY_SQL);
    }
 
    /**
     * Initialize the table structure
     */
    protected void initTable() {
        jdbcTemplate.execute(getInitTableSql());
    }

    /**
     * Delete table
     */
    protected void dropTable() {
        jdbcTemplate.execute(getDropTableSql());
    }

    /**
     * Clear table
     */
    protected void deleteTableContent() {
        jdbcTemplate.execute(getDeleteTableContentSql());
    }

    /**
     * Load all policy rules from storage
     * Duplicate data will be merged when loading
     *
     * @param model the model.
     */
    @Transactional(readOnly = true)
    @Override
    public void loadPolicy(Model model) {
        List<CasbinRule> casbinRules = jdbcTemplate.query(getLoadPolicySql(), BeanPropertyRowMapper.newInstance(CasbinRule.class));
        // group the policies by ptype and merge the duplicate data
        Map<String, List<ArrayList<String>>> policies = casbinRules.parallelStream().distinct()
                .map(CasbinRule::toPolicy)
                .collect(Collectors.toMap(x -> x.get(0), y -> {
                    ArrayList<ArrayList<String>> lists = new ArrayList<>();
                    // remove the first policy type in the list
                    y.remove(0);
                    lists.add(y);
                    return lists;
                }, (oldValue, newValue) -> {
                    oldValue.addAll(newValue);
                    return oldValue;
                }));
        // load grouped policies
        policies.keySet().forEach(
                k -> model.model.get(k.substring(0, 1)).get(k).policy.addAll(policies.get(k))
        );
    }

    /**
     * Save all policy rules to storage, merge duplicate data when saving
     *
     * @param model the model.
     */
    @Transactional
    @Override
    public void savePolicy(Model model) {
        deleteTableContent();
        List<CasbinRule> casbinRules = CasbinRule.transformToCasbinRule(model);
        int[] rows = jdbcTemplate.batchUpdate(
        		getInsertPolicySql(),
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, casbinRules.get(i).getPtype());
                        ps.setString(2, casbinRules.get(i).getV0());
                        ps.setString(3, casbinRules.get(i).getV1());
                        ps.setString(4, casbinRules.get(i).getV2());
                        ps.setString(5, casbinRules.get(i).getV3());
                        ps.setString(6, casbinRules.get(i).getV4());
                        ps.setString(7, casbinRules.get(i).getV5());
                    }

                    @Override
                    public int getBatchSize() {
                        return casbinRules.size();
                    }
                }
        );
        int insertRows = 0;
        for (int row : rows) {
            insertRows += row;
        }
        if (insertRows != casbinRules.size()) {
            throw new CasbinAdapterException(String.format("Add policy error, add %d rows, expect %d rows", insertRows, casbinRules.size()));
        }

    }

    /**
     * Add policy rules to storage
     *
     * @param sec   the section, "p" or "g".
     * @param ptype the policy type, "p", "p2", .. or "g", "g2", ..
     * @param rule  the rule, like (sub, obj, act).
     */
    @Transactional
    @Override
    public void addPolicy(String sec, String ptype, List<String> rule) {
        ArrayList<String> rules = new ArrayList<>(rule);
        rules.add(0, ptype);
        for (int i = 0; i < 6 - rule.size(); i++) {
            rules.add(null);
        }
        int rows = jdbcTemplate.update(getInsertPolicySql(), rules.toArray());
        if (rows != 1) {
            throw new CasbinAdapterException(String.format("Add policy error, add %d rows, expect %d rows", rows, 1));
        }
    }

    /**
     * Delete policy rule from storage
     *
     * @param sec   the section, "p" or "g".
     * @param ptype the policy type, "p", "p2", .. or "g", "g2", ..
     * @param rule  the rule, like (sub, obj, act).
     */
    @Transactional
    @Override
    public void removePolicy(String sec, String ptype, List<String> rule) {
        if (rule.isEmpty()) {
            return;
        }
        removeFilteredPolicy(sec, ptype, 0, rule.toArray(new String[0]));
    }

    /**
     * Delete the matching data after the index
     * specified by the current policy from the storage
     *
     * @param sec         the section, "p" or "g".
     * @param ptype       the policy type, "p", "p2", .. or "g", "g2", ..
     * @param fieldIndex  the policy rule's start index to be matched.
     * @param fieldValues the field values to be matched, value ""
     */
    @Transactional
    @Override
    public void removeFilteredPolicy(String sec, String ptype, int fieldIndex, String... fieldValues) {
        if (fieldValues.length == 0) {
            return;
        }
        List<String> params = new ArrayList<>(Arrays.asList(fieldValues));
        params.add(0, ptype);
        String delSql = getDeletePolicySql();
        int columnIndex = fieldIndex;
        for (int i = 0; i < fieldValues.length; i++) {
            delSql = String.format("%s%s%s%s", delSql, " AND v", columnIndex, " = ? ");
            columnIndex++;
        }
        int rows = jdbcTemplate.update(delSql, params.toArray());
        if (rows < 1) {
            if (casbinExceptionProperties.isRemovePolicyFailed()) {
                throw new CasbinAdapterException(String.format("Remove filtered policy error, remove %d rows, expect least 1 rows", rows));
            } else {
                logger.warn(String.format("Remove filtered policy error, remove %d rows, expect least 1 rows", rows));
            }
        }
    }

    /**
     * loadFilteredPolicy loads only policy rules that match the filter.
     *
     * @param model the model.
     * @param filter the filter used to specify which type of policy should be loaded.
     * @throws CasbinAdapterException if the file path or the type of the filter is incorrect.
     */
    @Transactional(readOnly = true)
    @Override
    public void loadFilteredPolicy(Model model, Object filter) throws CasbinAdapterException {
        if (filter == null) {
            loadPolicy(model);
            isFiltered = false;
            return;
        }
        if (!(filter instanceof Filter)) {
            isFiltered = false;
            throw new CasbinAdapterException("Invalid filter type.");
        }
        try {
            loadFilteredPolicyFromJdbc(model, (Filter) filter);
            isFiltered = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if have any filter roles.
     */
    @Override
    public boolean isFiltered() {
        return isFiltered;
    }

    /**
     * loadFilteredPolicyFromJdbc loads only policy rules that match the filter from file.
     *
     * @param model the model.
     * @param filter the filter used to specify which type of policy should be loaded.
     */
    private void loadFilteredPolicyFromJdbc(Model model, Filter filter) {
        // group the policies by ptype and merge the duplicate data.
        List<CasbinRule> casbinRules = jdbcTemplate.query(getLoadPolicySql(), BeanPropertyRowMapper.newInstance(CasbinRule.class));
        Map<String, List<ArrayList<String>>> policies = casbinRules.parallelStream().distinct()
                .map(CasbinRule::toPolicy)
                .collect(Collectors.toMap(x -> x.get(0), y -> {
                    ArrayList<ArrayList<String>> lists = new ArrayList<>();
                    if (!filterCasbinRule(y, filter)) {
                        // remove the first policy type in the list.
                        y.remove(0);
                        lists.add(y);
                    }
                    return lists;
                }, (oldValue, newValue) -> {
                    oldValue.addAll(newValue);
                    return oldValue;
                }));
        // load grouped policies
        policies.keySet().forEach(
                k -> model.model.get(k.substring(0, 1)).get(k).policy.addAll(policies.get(k))
        );
    }

    /**
     * match the line.
     *
     * @param policy the policy
     * @param filter the filter used to specify which type of policy should be loaded.
     * @return true if the policy is filtered.
     */
    private boolean filterCasbinRule(ArrayList<String> policy, Filter filter) {
        String[] filterSlice = null;
        switch (policy.get(0)) {
            case "p":
                filterSlice = filter.p;
                break;
            case "g":
                filterSlice = filter.g;
                break;
            default:
                break;
        }
        if (filterSlice == null) {
            filterSlice = new String[]{};
        }
        return filterWords(policy, filterSlice);
    }

    /**
     * match the words in the specific line.
     *
     * @return true if the policy is filtered.
     */
    private boolean filterWords(ArrayList<String> policy, String[] filter) {
        boolean skipLine = false;
        int i = 0;
        for (String s : filter) {
            i++;
            if (s.length() > 0 && !s.trim().equals(policy.get(i))) {
                skipLine = true;
                break;
            }
        }
        return skipLine;
    }
}
