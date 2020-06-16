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

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;

/**
 * A derived class of {@link SAXReader}.
 *
 * @since 2020-04-22
 */
public class CodeNetSaxReader extends SAXReader {
    private DocumentFactory documentFactory;
    private Locator locator;

    public CodeNetSaxReader(DocumentFactory documentFactory, Locator locator) {
        super(documentFactory);
        this.locator = locator;
        this.documentFactory = documentFactory;
    }

    @Override
    protected SAXContentHandler createContentHandler(XMLReader reader) {
        return new CodeNetSaxContentHandler(getDocumentFactory(), super.getDispatchHandler());
    }

    @Override
    public Document read(InputSource inputSource) {
        try {
            XMLReader xmlReader = getXMLReader();
            xmlReader = installXMLFilter(xmlReader);
            EntityResolver thatEntityResolver = super.getEntityResolver();
            if (thatEntityResolver == null) {
                thatEntityResolver = createDefaultEntityResolver(inputSource.getSystemId());
                super.setEntityResolver(thatEntityResolver);
            }
            xmlReader.setEntityResolver(thatEntityResolver);
            SAXContentHandler contentHandler = createContentHandler(xmlReader);
            contentHandler.setEntityResolver(thatEntityResolver);
            contentHandler.setInputSource(inputSource);
            boolean internal = isIncludeInternalDTDDeclarations();
            boolean external = isIncludeExternalDTDDeclarations();
            contentHandler.setIncludeInternalDTDDeclarations(internal);
            contentHandler.setIncludeExternalDTDDeclarations(external);
            contentHandler.setMergeAdjacentText(isMergeAdjacentText());
            contentHandler.setStripWhitespaceText(isStripWhitespaceText());
            contentHandler.setIgnoreComments(isIgnoreComments());
            xmlReader.setContentHandler(contentHandler);
            configureReader(xmlReader, contentHandler);
            if (contentHandler instanceof CodeNetSaxContentHandler
                && documentFactory instanceof CodeNetDocumentLocator) {
                ((CodeNetSaxContentHandler) contentHandler).setDocFactory((CodeNetDocumentLocator) documentFactory);
            }
            contentHandler.setDocumentLocator(locator);
            xmlReader.parse(inputSource);
            return contentHandler.getDocument();
        } catch (SAXException | DocumentException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
