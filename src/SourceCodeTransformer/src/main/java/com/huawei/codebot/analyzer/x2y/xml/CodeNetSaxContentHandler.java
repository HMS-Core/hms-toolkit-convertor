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

import org.dom4j.DocumentFactory;
import org.dom4j.ElementHandler;
import org.dom4j.io.SAXContentHandler;
import org.xml.sax.Locator;

/**
 * A derived class of {@link SAXContentHandler}.
 *
 * @since 2020-04-22
 */
public class CodeNetSaxContentHandler extends SAXContentHandler {
    private CodeNetDocumentLocator docFactory = null;

    public CodeNetSaxContentHandler(DocumentFactory docFactory, ElementHandler elementHandler) {
        super(docFactory, elementHandler);
    }

    public void setDocFactory(CodeNetDocumentLocator codeNetDocumentLocator) {
        this.docFactory = codeNetDocumentLocator;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        if (this.docFactory != null) {
            this.docFactory.setLocator(locator);
        }
    }
}
