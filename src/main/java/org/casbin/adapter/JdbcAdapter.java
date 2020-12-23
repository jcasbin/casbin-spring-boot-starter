package org.casbin.adapter;

import lombok.extern.slf4j.Slf4j;
import org.casbin.exception.CasbinAdapterException;
import org.casbin.jcasbin.model.Model;
import org.casbin.spring.boot.autoconfigure.properties.CasbinExceptionProperties;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shy
 * @version V1.1
 * @title: JdbcAdapter
 * @package org.casbin.adapter
 * @description:
 * @date 2020/12/23 16:50
 */
@Slf4j
public class JdbcAdapter implements org.casbin.jcasbin.persist.FilteredAdapter {

    private final static String INIT_TABLE_SQL = "CREATE TABLE IF NOT EXISTS casbin_rule (" +
            "    ptype varchar(255) NOT NULL," +
            "    v0    varchar(255) DEFAULT NULL," +
            "    v1    varchar(255) DEFAULT NULL," +
            "    v2    varchar(255) DEFAULT NULL," +
            "    v3    varchar(255) DEFAULT NULL," +
            "    v4    varchar(255) DEFAULT NULL," +
            "    v5    varchar(255) DEFAULT NULL" +
            ")";
    private final static String DROP_TABLE_SQL = "DROP TABLE IF EXISTS casbin_rule";
    private final static String DELETE_TABLE_CONTENT_SQL = "DELETE FROM casbin_rule";
    private final static String LOAD_POLICY_SQL = "SELECT * FROM casbin_rule";
    private final static String INSERT_POLICY_SQL = "INSERT INTO casbin_rule VALUES(?, ?, ?, ?, ?, ?, ?)";
    private final static String DELETE_POLICY_SQL = "DELETE FROM casbin_rule WHERE ptype = ? ";

    protected JdbcTemplate jdbcTemplate;
    protected CasbinExceptionProperties casbinExceptionProperties;

    private boolean isFiltered = true;

    public JdbcAdapter(JdbcTemplate jdbcTemplate, CasbinExceptionProperties casbinExceptionProperties, boolean autoCreateTable) {
        this.jdbcTemplate = jdbcTemplate;
        this.casbinExceptionProperties = casbinExceptionProperties;
        if (autoCreateTable) {
            initTable();
        }
    }

    /**
     * 筛选器类。
     * Enforcer当前仅接受此筛选器。
     */
    public static class Filter {
        public String[] p = new String[]{"p","1"};
        public String[] g = new String[]{"g","1"};
    }


    protected String getInitTableSql() {
        return INIT_TABLE_SQL;
    }

    protected String getDropTableSql() {
        return DROP_TABLE_SQL;
    }

    protected String getLoadPolicySql() {
        return LOAD_POLICY_SQL;
    }

    protected String getDeleteTableContentSql() {
        return DELETE_TABLE_CONTENT_SQL;
    }

    /**
     * 初始化表结构
     */
    protected void initTable() {
        jdbcTemplate.execute(getInitTableSql());
    }

    /**
     * 删除表
     */
    protected void dropTable() {
        jdbcTemplate.execute(getDropTableSql());
    }

    /**
     * 清空表
     */
    protected void deleteTableContent() {
        jdbcTemplate.execute(getDeleteTableContentSql());
    }

    /**
     * 从存储加载所有策略规则
     * 加载时会合并重复数据
     *
     * @param model the model.
     */
    @Transactional(readOnly = true)
    @Override
    public void loadPolicy(Model model) {
        List<CasbinRule> casbinRules = jdbcTemplate.query(getLoadPolicySql(), BeanPropertyRowMapper.newInstance(CasbinRule.class));
        // 按ptype对策略进行分组,并合并重复数据
        Map<String, List<ArrayList<String>>> policies = casbinRules.parallelStream().distinct()
                .map(CasbinRule::toPolicy)
                .collect(Collectors.toMap(x -> x.get(0), y -> {
                    ArrayList<ArrayList<String>> lists = new ArrayList<>();
                    // 去除list第一项策略类型
                    y.remove(0);
                    lists.add(y);
                    return lists;
                }, (oldValue, newValue) -> {
                    oldValue.addAll(newValue);
                    return oldValue;
                }));
        // 对分组的策略进行加载
        policies.keySet().forEach(
                k -> model.model.get(k.substring(0, 1)).get(k).policy.addAll(policies.get(k))
        );
        isFiltered = false;
    }

    /**
     * 将所有策略规则保存到存储
     * 保存时会合并重复数据
     *
     * @param model the model.
     */
    @Transactional
    @Override
    public void savePolicy(Model model) {
        deleteTableContent();
        List<CasbinRule> casbinRules = CasbinRule.transformToCasbinRule(model);
        int[] rows = jdbcTemplate.batchUpdate(
                INSERT_POLICY_SQL,
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
     * 将策略规则添加到存储
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
        int rows = jdbcTemplate.update(INSERT_POLICY_SQL, rules.toArray());
        if (rows != 1) {
            throw new CasbinAdapterException(String.format("Add policy error, add %d rows, expect %d rows", rows, 1));
        }
    }

    /**
     * 从存储中删除策略规则
     *
     * @param sec   the section, "p" or "g".
     * @param ptype the policy type, "p", "p2", .. or "g", "g2", ..
     * @param rule  the rule, like (sub, obj, act).
     */
    @Transactional
    @Override
    public void removePolicy(String sec, String ptype, List<String> rule) {
        if (rule.isEmpty()) return;
        removeFilteredPolicy(sec, ptype, 0, rule.toArray(new String[0]));
    }

    /**
     * 从存储中删除当前策略指定索引后匹配的数据
     *
     * @param sec         the section, "p" or "g".
     * @param ptype       the policy type, "p", "p2", .. or "g", "g2", ..
     * @param fieldIndex  the policy rule's start index to be matched.
     * @param fieldValues the field values to be matched, value ""
     */
    @Transactional
    @Override
    public void removeFilteredPolicy(String sec, String ptype, int fieldIndex, String... fieldValues) {
        if (fieldValues.length == 0) return;
        List<String> params = new ArrayList<>(Arrays.asList(fieldValues));
        params.add(0, ptype);
        String delSql = DELETE_POLICY_SQL;
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
     * 仅加载与筛选器匹配的策略规则。
     *
     * @param model  the model.
     * @param filter the filter used to specify which type of policy should be loaded.
     * @throws CasbinAdapterException if the file path or the type of the filter is incorrect.
     */
    @Transactional(readOnly = true)
    @Override
    public void loadFilteredPolicy(Model model, Object filter) throws CasbinAdapterException {
        if (filter == null) {
            loadPolicy(model);
            return;
        }
        if (!(filter instanceof Filter)) {
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
     * 如果加载的策略已被筛选，则返回true。
     *
     * @return true if have any filter roles.
     */
    @Override
    public boolean isFiltered() {
        return isFiltered;
    }

    /**
     * 从存储加载与筛选器匹配的策略规则
     * 加载时会合并重复数据
     *
     * @param model  the model.
     * @param filter the filter used to specify which type of policy should be loaded.
     */
    private void loadFilteredPolicyFromJdbc(Model model, Filter filter) {
        List<CasbinRule> casbinRules = jdbcTemplate.query(getLoadPolicySql(), BeanPropertyRowMapper.newInstance(CasbinRule.class));
        Map<String, List<ArrayList<String>>> policies = casbinRules.parallelStream().distinct()
                .map(CasbinRule::toPolicy)
                .collect(Collectors.toMap(x -> x.get(0), y -> {
                    ArrayList<ArrayList<String>> lists = new ArrayList<>();
                    if (!filterCasbinRule(y, filter)) {
                        // 去除list第一项策略类型
                        y.remove(0);
                        lists.add(y);
                    }
                    return lists;
                }, (oldValue, newValue) -> {
                    oldValue.addAll(newValue);
                    return oldValue;
                }));
            // 对分组的策略进行加载
        for (String k : policies.keySet()) {
            model.model.get(k.substring(0, 1)).get(k).policy.addAll(policies.get(k));
        }
//            policies.keySet().forEach(
//                    k -> model.model.get(k.substring(0, 1)).get(k).policy.addAll(policies.get(k))
//            );
            isFiltered = false;
    }

    /**
     * 匹配每条规则
     *
     * @param policy the policy
     * @param filter the filter used to specify which type of policy should be loaded.
     * @return true if the policy is filtered.
     */
    private boolean filterCasbinRule(ArrayList<String> policy, Filter filter) {
        if (filter == null) {
            return false;
        }
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
     * 对规则中的每个字段进行匹配
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
