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

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;

import java.util.ArrayList;
import java.util.List;

/**
 * A derived class of {@link DefaultElement}. We add two fields to help our change.
 *
 * @since 2020-04-22
 */
public class CodeNetElement extends DefaultElement {
    private static final long serialVersionUID = 1L;
    private int lineNumber = 0;
    private int columnNumber = 0;

    CodeNetElement(QName qname) {
        super(qname);
    }

    public CodeNetElement(QName qname, int attrCount) {
        super(qname, attrCount);
    }

    CodeNetElement(String name) {
        super(name);
    }

    public CodeNetElement(String name, Namespace namespace) {
        super(name, namespace);
    }

    public int getColumnNumber() {
        return this.columnNumber;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    /**
     * Set {@link #lineNumber} and {@link #columnNumber} at once.
     *
     * @param lineNum Int value of lineNumber.
     * @param colNum Int value of columnNumber.
     */
    public void setLocation(int lineNum, int colNum) {
        this.lineNumber = lineNum;
        this.columnNumber = colNum;
    }

    /**
     * Get all child elements of this element.
     *
     * @return A list of child element belongs to this element.
     */
    public List<CodeNetElement> getElements() {
        List<Element> elements = elements();
        List<CodeNetElement> ans = new ArrayList<>();
        for (Element element : elements) {
            ans.add((CodeNetElement) element);
        }
        return ans;
    }
}
