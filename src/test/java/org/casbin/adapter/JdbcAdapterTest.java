package org.casbin.adapter;

import org.casbin.exception.CasbinAdapterException;
import org.casbin.jcasbin.model.Model;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.casbin.jcasbin.main.CoreEnforcer.newModel;

/**
 * @author shy
 * @version V1.0
 * @title: JdbcAdapterTest
 * @package org.casbin.adapter
 * @description: test the loadFilteredPolicy function.
 * @date 2020/12/24 18:10
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class JdbcAdapterTest {

    @Autowired
    private JdbcAdapter jdbcAdapter;

    private Model model;
    /** the result of the loadPolicy function */
    private String loadPolicyResult;

    /**
     * Invoke the loadPolicy function ahead of time then get a result for convenience of comparison.
     */
    @Before
    public void getLoadPolicyResult() {
        init();
        jdbcAdapter.loadPolicy(model);
        loadPolicyResult = model.savePolicyToText();
    }
    /**
     * Test the loadFilteredPolicy function;
     */
    @Test
    public void testLoadFilteredPolicy() {
        JdbcAdapter.Filter filter = new JdbcAdapter.Filter();

        init();

        // define the filter which can match all the policy rules.
        filter.g = new String[]{
                "", "", "domain1"
        };
        filter.p = new String[]{
                "", "domain1"
        };

        // only policy rules that match the filter should be loaded,
        // so the result is different from the loadPolicyResult.
        jdbcAdapter.loadFilteredPolicy(model, filter);
        Assert.assertNotEquals(loadPolicyResult, model.savePolicyToText());

        init();

        // define the filter which can not match all the policy rules.
        filter.g = new String[]{
                "", "", "domain1"
        };
        filter.p = new String[]{
                "", "domain2"
        };

        // there are no policy rules that match the filter,
        // so the result is same as the loadPolicyResult.
        jdbcAdapter.loadFilteredPolicy(model, filter);
        Assert.assertEquals(loadPolicyResult, model.savePolicyToText());
    }

    /**
     * Test the loadFilteredPolicy function with the empty filter;
     */
    @Test
    public void testLoadFilteredPolicyEmptyFilter() {
        init();

        // the filter is null, so the result is same as the loadPolicyResult.
        jdbcAdapter.loadFilteredPolicy(model, null);
        Assert.assertEquals(loadPolicyResult, model.savePolicyToText());

    }

    /**
     * Test the loadFilteredPolicy function with the invalid filter type;
     */
    @Test
    public void testLoadFilteredPolicyInvalidFilterType() {
        init();

        // owing to the invalid filter type,this function should throw a CasbinAdapterException
        Object filter = new Object();
        try {
            jdbcAdapter.loadFilteredPolicy(model, filter);
        } catch (CasbinAdapterException casbinAdapterException) {
            assert true;
        }
    }

    /**
     * Initialize the model
     */
    private void init() {
        model = newModel();
        model.addDef("r", "r", "sub, obj, act");
        model.addDef("p", "p", "sub, obj, act");
        model.addDef("e", "e", "some(where (p.eft == allow))");
        model.addDef("m", "m", "r.sub == p.sub && keyMatch(r.obj, p.obj) && regexMatch(r.act, p.act)");
    }
}