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

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.mirror.KClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Get methods that should add in the target GImpl and HImpl
 *
 * @since 2020-01-10
 */
public class GHImplMethodSupplement {
    /**
     * A list containing methods that should add in the target GImpl and HImpl.
     */
    private static final List<Method> ADDITIONAL_METHODS =
        Arrays.asList(new Method("org.xms.g.vision.face.LargestFaceFocusingProcessor", "selectFocus"),
            new Method("org.xms.g.vision.face.LargestFaceFocusingProcessor", "receiveDetections"));

    public static List<JMapping<JMethod>> getAdditionalMethods(ClassNode classNode) {
        String xmsName = TypeNode.create(classNode.getJClass().gName()).toX().getTypeName();
        List<String> methodNameList = new ArrayList<>();
        ADDITIONAL_METHODS.forEach(method -> {
            if (method.className.equals(xmsName)) {
                methodNameList.add(method.methodName);
            }
        });
        if (methodNameList.size() == 0) {
            return new ArrayList<>();
        }
        List<JMapping<JMethod>> mappings = new ArrayList<>();
        List<JMapping<JMethod>> wholeMapping = KClassUtils.getGHierarchicalMethodMapping(classNode);
        wholeMapping.forEach(mapping -> {
            if (methodNameList.contains(mapping.g().name())) {
                mappings.add(mapping);
            }
        });
        return mappings;
    }
}
