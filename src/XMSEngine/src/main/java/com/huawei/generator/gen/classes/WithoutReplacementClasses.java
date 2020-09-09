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

package com.huawei.generator.gen.classes;

import java.util.HashSet;
import java.util.Set;

/**
 * Some classes whose name should not be replaced
 *
 * @since 2020-04-02
 */
public class WithoutReplacementClasses {
    /**
     * A set containing classes that should not be replaced in toX.
     */
    private static final Set<String> WITHOUT_REPLACEMENT_CLASSES = new HashSet<String>() {
        {
            add("com.google.firebase.auth.GoogleAuthCredential");
            add("GoogleAuthCredential");
            add("com.google.firebase.auth.GoogleAuthProvider");
            add("GoogleAuthProvider");
        }
    };

    public static boolean noReplace(String s) {
        return WITHOUT_REPLACEMENT_CLASSES.contains(s);
    }
}
