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

/**
 * Represent a XML node and store information we need when we process XML file.
 *
 * @since 2020-04-22
 */
public class XmlEntity {
    /**
     * XML label name.
     */
    public String labelName = null;
    /**
     * An identifier, usually is androidName property of label.
     */
    public String nameIdentifier = null;
    /**
     * The first line number of start label that this entity represents.
     */
    public int labelStartLine = -1;
    /**
     * The last line number of start label that this entity represents.
     */
    public int labelStartLinesEndPosition = -1 ;
    /**
     * Line number of close label.
     */
    public int labelEndLine = -1;
    /**
     * Content of this entity, include label itself.
     */
    public String labelContent = null;
    /**
     * Parent of this entity.
     */
    public String parentLabelName = null;

    public String getLabelContent() {
        return labelContent;
    }

    public void setLabelContent(String labelContent) {
        this.labelContent = labelContent;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public String getNameIdentifier() {
        return nameIdentifier;
    }

    public void setNameIdentifier(String nameIdentifier) {
        this.nameIdentifier = nameIdentifier;
    }

    public int getLabelStartLine() {
        return labelStartLine;
    }

    public void setLabelStartLine(int labelStartLine) {
        this.labelStartLine = labelStartLine;
    }

    public int getLabelStartLinesEndPosition() {
        return labelStartLinesEndPosition;
    }

    public void setLabelStartLinesEndPosition(int labelStartLinesEndPosition) {
        this.labelStartLinesEndPosition = labelStartLinesEndPosition;
    }

    public int getLabelEndLine() {
        return labelEndLine;
    }

    public void setLabelEndLine(int labelEndLine) {
        this.labelEndLine = labelEndLine;
    }

    public String getParentLabelName() {
        return parentLabelName;
    }

    public void setParentLabelName(String parentLabelName) {
        this.parentLabelName = parentLabelName;
    }
}
