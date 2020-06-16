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

package com.huawei.codebot.analyzer.x2y.xml;

import java.util.Map;

/**
 * A bean used to store all operation patterns for XML.
 *
 * @since 2020-04-22
 */
public class XmlJsonPattern {
    private Map<String, CommonOperation> xmlChangerJsonTargets = null;
    private Map<LabelType, Map<String, CommonOperation>> xmlChangerCategoryJsonTargets = null;
    private Map<String, LayoutOperation> layoutOperationJsonTargets = null;

    public XmlJsonPattern setXmlChangerJsonTargets(Map<String, CommonOperation> xmlChangerJsonTargets) {
        this.xmlChangerJsonTargets = xmlChangerJsonTargets;
        return this;
    }

    public XmlJsonPattern setXmlChangerCategoryJsonTargets(
            Map<LabelType, Map<String, CommonOperation>> xmlChangerCategoryJsonTargets) {
        this.xmlChangerCategoryJsonTargets = xmlChangerCategoryJsonTargets;
        return this;
    }

    public void setLayoutOperationJsonTargets(Map<String, LayoutOperation> layoutOperationJsonTargets) {
        this.layoutOperationJsonTargets = layoutOperationJsonTargets;
    }

    Map<String, CommonOperation> getXmlChangerJsonTargets() {
        return xmlChangerJsonTargets;
    }

    Map<LabelType, Map<String, CommonOperation>> getXmlChangerCategoryJsonTargets() {
        return xmlChangerCategoryJsonTargets;
    }

    Map<String, LayoutOperation> getLayoutOperationJsonTargets() {
        return layoutOperationJsonTargets;
    }

}
