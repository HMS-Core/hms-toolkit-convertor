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

package com.huawei.generator.json;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.generator.ast.custom.XMethodDoc;
import com.huawei.generator.build.ClassMappingManager;
import com.huawei.generator.gen.AstConstants;
import com.huawei.inquiry.docs.EntireDoc;
import com.huawei.inquiry.utils.DocJsonReader;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * util class for javadoc
 *
 * @since 2020-06-08
 */
public class DocSources {
    private Map<String, XClassDoc> docMap;

    public DocSources() {
        new DocSources(null);
    }

    public DocSources(String pluginPath) {
        // key is kitName, value is version
        Map<String, String> gmsVersionMap = ClassMappingManager.getCurrentVersion();
        if (gmsVersionMap.isEmpty()) {
            docMap = Collections.EMPTY_MAP;
            return;
        }
        docMap = new DocJsonReader(pluginPath, gmsVersionMap).readDocJsonsToMap(EntireDoc.STRATEGYTYPE.X, false);
    }

    // read json and return classDoc
    public XClassDoc getClassDoc(ClassNode classNode) {
        if (docMap == null) {
            return null;
        }
        String classNodeFullName = TypeNode.create(classNode.fullName()).getTypeName();
        XClassDoc retClassDoc = null;
        XClassDoc xImplClassDoc = null;
        for (Map.Entry<String, XClassDoc> map : docMap.entrySet()) {
            String key = map.getKey();
            XClassDoc value = map.getValue();
            if (key.equals(classNodeFullName + "." + AstConstants.INNER_CLASS_NAME)) {
                xImplClassDoc = value;
            }
            if (key.equals(classNodeFullName)) {
                retClassDoc = value;
            }

            if (retClassDoc != null && xImplClassDoc != null) {
                break;
            }
        }

        if (retClassDoc != null && xImplClassDoc != null) {
            retClassDoc.setXImplClassDoc(xImplClassDoc);
        }
        return retClassDoc;
    }

    // add methodDoc's head information into methodNode
    public static XMethodDoc createMethodDocHead(MethodNode methodNode) {
        XClassDoc classDoc = methodNode.parent().getClassDoc();
        if (classDoc == null) {
            return null;
        }

        for (XMethodDoc methodDoc : classDoc.getMethods().values()) {
            if (methodDoc.isSameMethod(methodNode)) {
                List<String> displayInfoList = new LinkedList<>();
                displayInfoList.add("/**");
                String description = methodDoc.getDescriptions().trim();
                if (description.lastIndexOf(".") != description.length() - 1) {
                    description += ".";
                }
                displayInfoList.add(" * " + methodDoc.getXMethodName().trim() + " " + description + "<br/>");
                methodDoc.setDisplayInfoList(displayInfoList);

                // add methodDoc information into methodNode
                methodNode.setMethodDocNode(methodDoc);
                return methodDoc;
            }
        }
        return null;
    }

    // add classDoc's head information into classNode
    public static XClassDoc createClassDocHead(XClassDoc classDoc, ClassNode classNode) {
        List<String> displayInfoList = new LinkedList<>();
        displayInfoList.add("/**");
        String xClassInfo = classDoc.getXClassInfo();
        if (!xClassInfo.isEmpty()) {
            displayInfoList.add(" * " + classDoc.getXClassInfo() + "<br/>");
        }

        classDoc.setDisplayInfoList(displayInfoList);
        // add classDoc information into classNode
        classNode.setClassDoc(classDoc);
        return classDoc;
    }

    // params, exception and return information
    public static List<String> getParamsReturnExceptionInfo(XMethodDoc node) {
        List<String> displayInfoList = new LinkedList<>();
        List<Map<String, String>> params = node.getParams();
        List<Map<String, String>> exceptions = node.getExceptions();
        String ret = node.getRetDescription().trim();
        boolean isRetInfoNeed = !ret.isEmpty() && !ret.equals("void");
        if (!params.isEmpty() || exceptions != null || isRetInfoNeed) {
            displayInfoList.add(" *");
        }

        for (Map<String, String> m : params) {
            displayInfoList.add(" * @param " + m.get("name").trim() + " " + m.get("descriptions").trim());
        }

        if (exceptions != null) {
            for (Map<String, String> m : exceptions) {
                displayInfoList.add(" * @throws " + m.get("name").trim() + " " + m.get("descriptions").trim());
            }
        }

        if (isRetInfoNeed) {
            displayInfoList.add(" * @return " + ret);
        }
        displayInfoList.add(" */");
        return displayInfoList;
    }
}
