package org.casbin;

import org.casbin.jcasbin.main.Enforcer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: EnforcerTest
 * @package org.casbin
 * @description:
 * @date 2019-4-06 17:31
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class EnforcerTest {

    @Autowired
    private Enforcer enforcer;

    @Test
    public void test1() {
        // user rights
        enforcer.addPolicy("alice", "data1", "read");
        enforcer.addPolicy("bob", "data2", "write");
        // role Permissions
        enforcer.addPolicy("data2_admin", "data2", "read");
        enforcer.addPolicy("data2_admin", "data2", "write");
        // grant alice the permissions owned by data2_admin
        enforcer.addGroupingPolicy("alice", "data2_admin");

        // Does bob have read access to data1? It must not be.
        Assert.assertFalse(enforcer.enforce("bob", "data1", "read"));

        // Whether alice has read and write access to data2, it must be possible, otherwise, why is authorization used?
        Assert.assertTrue(enforcer.enforce("alice", "data2", "read"));
        Assert.assertTrue(enforcer.enforce("alice", "data2", "write"));

        // repeat adding P strategy
        enforcer.addPolicy("data2_admin", "data2", "write");
        enforcer.addPolicy("data2_admin", "data2", "write");
        enforcer.addPolicy("data2_admin", "data2", "write");
        // complete storage, built-in duplicate item filtering
        enforcer.savePolicy();
        // verify that duplicates are merged
        Assert.assertEquals(4, enforcer.getNamedPolicy("p").size());
    }

    @Test
    public void test2() {
        enforcer.clearPolicy();

        Assert.assertFalse(enforcer.enforce("bob", "data1", "read"));
        Assert.assertFalse(enforcer.enforce("alice", "data2", "read"));
        Assert.assertFalse(enforcer.enforce("alice", "data2", "write"));

    }
}
