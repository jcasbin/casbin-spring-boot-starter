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
        enforcer.addPolicy("alice", "data1", "read");
        enforcer.addPolicy("bob", "data2", "write");
        //角色权限
        enforcer.addPolicy("data2_admin", "data2", "read");
        enforcer.addPolicy("data2_admin", "data2", "write");
        //为alice授予data2_admin拥有的权限
        enforcer.addGroupingPolicy("alice", "data2_admin");

        //bob是否对data1具有读取权限,肯定是不能有啦
        Assert.assertFalse(enforcer.enforce("bob", "data1", "read"));

        //alice是否对data2具有读写取权限,肯定是可以的啦,不然授权是干嘛用的
        Assert.assertTrue(enforcer.enforce("alice", "data2", "read"));
        Assert.assertTrue(enforcer.enforce("alice", "data2", "write"));

        //重复添加P策略
        enforcer.addPolicy("data2_admin", "data2", "write");
        enforcer.addPolicy("data2_admin", "data2", "write");
        enforcer.addPolicy("data2_admin", "data2", "write");
        //完全存储,内置重复项过滤
        enforcer.savePolicy();
        //验证重复项是否合并
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
