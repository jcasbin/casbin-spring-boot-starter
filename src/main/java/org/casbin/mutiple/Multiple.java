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
import java.util.HashMap;

public class Multiple {
    // Use a HashMap to store multiple configurations
    public HashMap<String, Enforcer> enforcers = new HashMap<>();

    // Object initialization, load the configurations
    public Multiple() {
        MultipleEnforceConfig m = new MultipleEnforceConfig();
        m.multiplesets(enforcers);
    }

    // Get enforcer by name
    public Enforcer getEnforcer(HashMap<String, Enforcer> enforcers, String name) {
        Enforcer enforcer = enforcers.get(name);
        if (enforcer == null) {
            throw new IllegalArgumentException("No enforcer found with name: " + name);
        }
        return enforcer;
    }
}
