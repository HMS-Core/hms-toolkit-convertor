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

import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.TypeNode;

import java.util.Arrays;
import java.util.List;

/**
 * Some special Constructors
 *
 * @since 2020-01-15
 */
public class SpecialConstructors {
    /**
     * Special Constructor for subClass of View to call super(getContext()), depending on hms.
     */
    private static final List<Constructor> SPECIAL_CONSTRUCTOR = Arrays.asList(
        new Constructor("StreetViewPanoramaView",
            Arrays.asList("android.content.Context", "org.xms.g.maps.StreetViewPanoramaOptions")),
        new Constructor("MapView", Arrays.asList("android.content.Context", "org.xms.g.maps.ExtensionMapOptions")));

    public static boolean isSpecialConstructor(MethodNode node) {
        return SPECIAL_CONSTRUCTOR.stream()
            .anyMatch(constructor -> constructor.isMatch(node.name(), node.parameters()));
    }

    private static class Constructor {
        final String methodName;

        final List<String> params;

        Constructor(String methodName, List<String> params) {
            this.methodName = methodName;
            this.params = params;
        }

        boolean isMatch(String methodName, List<TypeNode> params) {
            if (!this.methodName.equals(methodName) || params.size() != this.params.size()) {
                return false;
            }
            for (int i = 0; i < params.size(); i++) {
                if (!params.get(i).getTypeName().equals(this.params.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }
}
