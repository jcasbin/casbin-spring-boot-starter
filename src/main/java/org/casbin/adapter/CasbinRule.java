package org.casbin.adapter;

import org.casbin.jcasbin.model.Model;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: CasbinRule
 * @package org.casbin.adapter
 * @description:
 * @date 2019/4/4 17:16
 */
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
     * 
     * Converting model to CasbinRule
     * conversion process will merge duplicate data
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

    public String getPtype() {
        return ptype;
    }

    public void setPtype(String ptype) {
        this.ptype = ptype;
    }

    public String getV0() {
        return v0;
    }

    public void setV0(String v0) {
        this.v0 = v0;
    }

    public String getV1() {
        return v1;
    }

    public void setV1(String v1) {
        this.v1 = v1;
    }

    public String getV2() {
        return v2;
    }

    public void setV2(String v2) {
        this.v2 = v2;
    }

    public String getV3() {
        return v3;
    }

    public void setV3(String v3) {
        this.v3 = v3;
    }

    public String getV4() {
        return v4;
    }

    public void setV4(String v4) {
        this.v4 = v4;
    }

    public String getV5() {
        return v5;
    }

    public void setV5(String v5) {
        this.v5 = v5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CasbinRule that = (CasbinRule) o;
        return Objects.equals(ptype, that.ptype) && Objects.equals(v0, that.v0) && Objects.equals(v1, that.v1) && Objects.equals(v2, that.v2) && Objects.equals(v3, that.v3) && Objects.equals(v4, that.v4) && Objects.equals(v5, that.v5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ptype, v0, v1, v2, v3, v4, v5);
    }
}
