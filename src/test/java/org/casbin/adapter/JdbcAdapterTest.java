package org.casbin.adapter;

import org.casbin.jcasbin.exception.CasbinAdapterException;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.FilteredAdapter;
import org.casbin.jcasbin.persist.file_adapter.FilteredAdapter.Filter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.casbin.jcasbin.main.CoreEnforcer.newModel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author shy
 * @version V1.0
 * @title: JdbcAdapterTest
 * @package org.casbin.adapter
 * @description: test the loadFilteredPolicy function.
 * @date 2020/12/24 18:10
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class JdbcAdapterTest {

    @Resource
    private FilteredAdapter filteredAdapter;

    private Model model;
    /** the result of the loadPolicy function */
    private String loadPolicyResult;

    /**
     * Test the loadFilteredPolicy function;
     */
    @Test
    public void testLoadFilteredPolicy() {
        Filter filter = new Filter();

        List<String> rules;

        init();
        getLoadPolicyResult();

        // define the filter which can match any the policy rules.
        filter.g = new String[]{
                "domain1", "domain2"
        };
        filter.p = new String[]{
                "domain2", "domain3"
        };

        rules = new ArrayList<>(Arrays.asList("domain2", "domain3"));
        this.model.addPolicy("p","p", rules);
        this.filteredAdapter.savePolicy(model);

        // only policy rules that match the filter should be loaded,
        // so the result is different from the loadPolicyResult.
        this.filteredAdapter.loadFilteredPolicy(this.model, filter);
        assertNotEquals(this.loadPolicyResult, this.model.savePolicyToText());

        init();
        getLoadPolicyResult();

        // define the filter which can not match all the policy rules.
        filter.g = new String[]{
                "domain1", "domain5"
        };
        filter.p = new String[]{
                "domain5", "domain6"
        };

        rules = new ArrayList<>(Arrays.asList("domain2", "domain3"));
        this.model.addPolicy("p","p", rules);
        this.filteredAdapter.savePolicy(model);
        // there are no policy rules that match the filter,
        // so the result is same as the loadPolicyResult.
        this.filteredAdapter.loadFilteredPolicy(this.model, filter);
        assertEquals(this.loadPolicyResult, this.model.savePolicyToText());
    }

    /**
     * Test the loadFilteredPolicy function with the empty filter;
     */
    @Test
    public void testLoadFilteredPolicyEmptyFilter() {
        init();
        getLoadPolicyResult();
        init();

        // the filter is null, so the result is same as the loadPolicyResult.
        this.filteredAdapter.loadFilteredPolicy(this.model, null);
        assertEquals(this.loadPolicyResult, this.model.savePolicyToText());
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
            this.filteredAdapter.loadFilteredPolicy(this.model, filter);
        } catch (CasbinAdapterException casbinAdapterException) {
            assert true;
        }
    }

    /**
     * Initialize the model
     */
    private void init() {
        this.model = newModel();
        this.model.addDef("r", "r", "sub, obj, act");
        this.model.addDef("p", "p", "sub, obj, act");
        this.model.addDef("e", "e", "some(where (p.eft == allow))");
        this.model.addDef("m", "m", "r.sub == p.sub && keyMatch(r.obj, p.obj) && regexMatch(r.act, p.act)");
        this.model.addDef("g", "g", "_, _");
    }

    /**
     * Invoke the loadPolicy function ahead of time then get a result for convenience of comparison.
     */
    private void getLoadPolicyResult() {
        this.filteredAdapter.loadPolicy(this.model);
        this.loadPolicyResult = this.model.savePolicyToText();
    }
}