// Copyright 2022 The Casdoor Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.casbin.mutiple;

import org.casbin.jcasbin.main.Enforcer;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Component
@Configuration
public class MultipleEnforceConfig {

    // Read the configuration file and store the configuration in a HashMap
    public void multiplesets(HashMap<String, Enforcer> enforcers){
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("config.yml");
        Map<String, Object> obj = yaml.load(inputStream);
        List<Map<String, Object>> configList = (List<Map<String, Object>>) ((Map<String, Object>) obj.get("configs")).get("configList");
        for (Map<String, Object> config : configList) {
            String name = (String) config.get("name");
            String modelPath = (String) config.get("modelPath");
            String policyPath = (String) config.get("policyPath");
            createEnforcer(enforcers, name, modelPath, policyPath);
        }
    }

    // Create Enforcer and store it in the HashMap
    public HashMap<String,Enforcer> createEnforcer(HashMap<String, Enforcer> enforcers, String name, String modelPath, String policyPath) {
        Enforcer enforcer = new Enforcer(modelPath, policyPath);
        enforcers.put(name, enforcer);
        return enforcers;
    }

    // Replace the configuration
    public void replaceEnforcer(HashMap<String, Enforcer> enforcers, String name, Enforcer enforcer) {
        enforcers.put(name, enforcer);
    }

}
