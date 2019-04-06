package org.casbin.adapter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.casbin.jcasbin.model.Model;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: CasbinRule
 * @package org.casbin.adapter
 * @description:
 * @date 2019/4/4 17:16
 */
@Data
@EqualsAndHashCode
class CasbinRule {
    private String ptype;
    private String v0;
    private String v1;
    private String v2;
    private String v3;
    private String v4;
    private String v5;

    public ArrayList<String> toPolicy() {
        ArrayList<String> policy = new ArrayList<>();
        policy.add(ptype);
        if (!StringUtils.isEmpty(v0)) {
            policy.add(v0);
        }
        if (!StringUtils.isEmpty(v1)) {
            policy.add(v1);
        }
        if (!StringUtils.isEmpty(v2)) {
            policy.add(v2);
        }
        if (!StringUtils.isEmpty(v3)) {
            policy.add(v3);
        }
        if (!StringUtils.isEmpty(v4)) {
            policy.add(v4);
        }
        if (!StringUtils.isEmpty(v5)) {
            policy.add(v5);
        }
        return policy;
    }

    /**
     * 将model转换为CasbinRule
     * 转换过程将会合并重复数据
     */
    public static List<CasbinRule> transformToCasbinRule(Model model) {
        Set<CasbinRule> casbinRules = new HashSet<>();
        model.model.values().forEach(x -> x.values().forEach(y -> y.policy.forEach(z -> {
            if (z.isEmpty()) return;
            int size = z.size();
            CasbinRule casbinRule = new CasbinRule();
            casbinRule.setPtype(y.key);
            casbinRule.setV0(z.get(0));
            if (size >= 2) {
                casbinRule.setV1(z.get(1));
            }
            if (size >= 3) {
                casbinRule.setV2(z.get(2));
            }
            if (size >= 4) {
                casbinRule.setV3(z.get(3));
            }
            if (size >= 5) {
                casbinRule.setV4(z.get(4));
            }
            if (size >= 6) {
                casbinRule.setV5(z.get(5));
            }
            casbinRules.add(casbinRule);
        })));
        return new ArrayList<>(casbinRules);
    }
}
