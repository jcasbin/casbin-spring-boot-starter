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
        //用户权限
    	// User rights
        enforcer.addPolicy("alice", "data1", "read");
        enforcer.addPolicy("bob", "data2", "write");
        //角色权限
        // Role Permissions
        enforcer.addPolicy("data2_admin", "data2", "read");
        enforcer.addPolicy("data2_admin", "data2", "write");
        //为alice授予data2_admin拥有的权限
        // Grant alice the permissions owned by data2_admin
        enforcer.addGroupingPolicy("alice", "data2_admin");

        //bob是否对data1具有读取权限,肯定是不能有啦
        // Does bob have read access to data1? It must not be.
        Assert.assertFalse(enforcer.enforce("bob", "data1", "read"));

        //alice是否对data2具有读写取权限,肯定是可以的啦,不然授权是干嘛用的
        // Whether alice has read and write access to data2, it must be possible, otherwise, why is authorization used?
        Assert.assertTrue(enforcer.enforce("alice", "data2", "read"));
        Assert.assertTrue(enforcer.enforce("alice", "data2", "write"));

        //重复添加P策略
        // Repeat adding P strategy
        enforcer.addPolicy("data2_admin", "data2", "write");
        enforcer.addPolicy("data2_admin", "data2", "write");
        enforcer.addPolicy("data2_admin", "data2", "write");
        //完全存储,内置重复项过滤
        // Complete storage, built-in duplicate item filtering
        enforcer.savePolicy();
        //验证重复项是否合并
        // Verify that duplicates are merged
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
