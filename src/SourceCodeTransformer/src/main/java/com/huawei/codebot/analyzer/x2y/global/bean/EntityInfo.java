/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.codebot.analyzer.x2y.global.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data structure of basic information for code elements(code entities)
 *
 * @since 2019-07-14
 */
public class EntityInfo implements Serializable {
    private static final long serialVersionUID = -5602170660970790422L;

    /**
     * the simple name of the code element
     */
    protected String name;

    /**
     * the package name of the code element
     */
    protected String packageName;

    /**
     * the owner classes of the code element, the order is from inner to outer
     */
    private List<String> ownerClasses = new ArrayList<>();

    /**
     * @return full name of the entity, which includes package name,
     * owner class names (if there exists) and simple name.
     */
    public String getQualifiedName() {
        if (name == null) {
            return null;
        }
        StringBuilder qualifiedName = new StringBuilder(name);
        for (String ownerClass : ownerClasses) {
            qualifiedName.insert(0, ".");
            qualifiedName.insert(0, ownerClass);
        }
        if (packageName != null) {
            qualifiedName.insert(0, ".");
            qualifiedName.insert(0, packageName);
        }
        return qualifiedName.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getOwnerClasses() {
        return ownerClasses;
    }

    public void setOwnerClasses(List<String> ownerClasses) {
        this.ownerClasses = ownerClasses;
    }

}
