package org.casbin;

import org.assertj.core.api.Assert;
import org.casbin.jcasbin.main.Enforcer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: EnforcerTest
 * @package org.casbin
 * @description:
 * @date 2019-4-06 17:31
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
class EnforcerTest {

    @Autowired
    private Enforcer enforcer;

    @Test
    void test1() {
        // user rights
        enforcer.addPolicy("alice", "data1", "read");
        enforcer.addPolicy("bob", "data2", "write");
        // role Permissions
        enforcer.addPolicy("data2_admin", "data2", "read");
        enforcer.addPolicy("data2_admin", "data2", "write");
        // grant alice the permissions owned by data2_admin
        enforcer.addGroupingPolicy("alice", "data2_admin");

        // Does bob have read access to data1? It must not be.
        assertFalse(enforcer.enforce("bob", "data1", "read"));

        // Whether alice has read and write access to data2, it must be possible, otherwise, why is authorization used?
        assertTrue(enforcer.enforce("alice", "data2", "read"));
        assertTrue(enforcer.enforce("alice", "data2", "write"));

        // repeat adding P strategy
        enforcer.addPolicy("data2_admin", "data2", "write");
        enforcer.addPolicy("data2_admin", "data2", "write");
        enforcer.addPolicy("data2_admin", "data2", "write");
        // complete storage, built-in duplicate item filtering
        enforcer.savePolicy();
        // verify that duplicates are merged
        assertEquals(4, enforcer.getNamedPolicy("p").size());
    }

    @Test
    void test2() {
        enforcer.clearPolicy();

        assertFalse(enforcer.enforce("bob", "data1", "read"));
        assertFalse(enforcer.enforce("alice", "data2", "read"));
        assertFalse(enforcer.enforce("alice", "data2", "write"));

    }
}
