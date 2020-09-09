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
import org.dom4j.tree.DefaultComment;

/**
 * A derived class of {@link DefaultComment}. We add two fields to help our change.
 *
 * @since 2020-05-21
 */
public class CodeNetComment extends DefaultComment {
    private int lineNumber = 0;
    private int columnNumber = 0;

    public CodeNetComment(String text) {
        super(text);
    }

    public CodeNetComment(Element parent, String text) {
        super(parent, text);
    }

    public void setLocation(int lineNum, int colNum) {
        this.lineNumber = lineNum;
        this.columnNumber = colNum;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
}
