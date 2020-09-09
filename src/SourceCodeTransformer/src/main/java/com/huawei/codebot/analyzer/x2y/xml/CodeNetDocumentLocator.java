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

import org.dom4j.Comment;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.Text;
import org.xml.sax.Locator;

/**
 * A specific factory derived by {@link DocumentFactory}.
 * <br/>
 * Used to create {@link CodeNetElement} instance.
 *
 * @since 2020-04-22
 */
public class CodeNetDocumentLocator extends DocumentFactory {
    private static final long serialVersionUID = 1L;
    private transient Locator locator;

    public CodeNetDocumentLocator(Locator locator) {
        super();
        this.locator = locator;
    }

    @Override
    public Element createElement(String name) {
        CodeNetElement codeNetElement = new CodeNetElement(name);
        codeNetElement.setLocation(this.locator.getLineNumber(), this.locator.getColumnNumber());
        return codeNetElement;
    }

    @Override
    public Element createElement(QName qname) {
        CodeNetElement codeNetElement = new CodeNetElement(qname);
        codeNetElement.setLocation(this.locator.getLineNumber(), this.locator.getColumnNumber());
        return codeNetElement;
    }

    @Override
    public Text createText(String text) {
        CodeNetText codeNetText = new CodeNetText(text);
        codeNetText.setLocation(this.locator.getLineNumber(), this.locator.getColumnNumber());
        return codeNetText;
    }

    @Override
    public Comment createComment(String text) {
        CodeNetComment codeNetComment = new CodeNetComment(text);
        codeNetComment.setLocation(this.locator.getLineNumber(), this.locator.getColumnNumber());
        return codeNetComment;
    }

    public void setLocator(Locator locator) {
        this.locator = locator;
    }
}
