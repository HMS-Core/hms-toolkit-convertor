/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.idea.ui.javadoc;

import com.huawei.hms.convertor.idea.util.JavaDocUtil;
import com.intellij.openapi.project.Project;

import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.GMS_CLASS;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.GMS_FILED;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.GMS_METHOD;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.HMS_CLASS;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.HMS_FILED;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.HMS_METHOD;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.LEARN_MORE;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.XMS_CLASS;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.XMS_METHOD;
import static com.huawei.hms.convertor.idea.util.JavaDocUtil.getClassInfo;
import static com.huawei.hms.convertor.idea.util.JavaDocUtil.getFieldInfo;
import static com.huawei.hms.convertor.idea.util.JavaDocUtil.getMethodInfo;
import static com.huawei.hms.convertor.idea.util.JavaDocUtil.setLabelLink;
import static com.huawei.hms.convertor.idea.util.JavaDocUtil.setXmsContent;

/**
 * java doc object when api belong to gms
 *
 * @since 2020-08-04
 */
public class GmsJavaDoc extends JavaDocService {

    private Project project;

    public GmsJavaDoc(JavaDocPanelInfos javaDocPanelInfos, JavaDocToolWindow javaDocToolWindow,Project project) {
        super(javaDocPanelInfos, javaDocToolWindow);
        this.project = project;
    }

    @Override
    public void handleWhenClass() {
        javaDocToolWindow.getLabelUp().setText(GMS_CLASS);
        javaDocToolWindow.getLabelMiddle().setText(HMS_CLASS);
        javaDocToolWindow.getLabelDown().setText(XMS_CLASS);

        DocDisplayInfo classDisplayInfo = getClassInfo(javaDocPanelInfos);
        DocContents classContents = classDisplayInfo.getDocContents();

        javaDocToolWindow.getDocContentDown().setText(JavaDocUtil.setTextDisplay(classContents.getXmsContentTemp()));
        setLabelLink(javaDocToolWindow.getLearnMoreDown(), LEARN_MORE, classDisplayInfo.getXDocLink());
        javaDocToolWindow.getDocContentMiddle().setText(JavaDocUtil.setTextDisplay(classContents.getHmsContentTemp()));
        setLabelLink(javaDocToolWindow.getLearnMoreMiddle(), LEARN_MORE, classDisplayInfo.getHDocLink());
        javaDocToolWindow.getDocContentUp().setText(JavaDocUtil.setTextDisplay(classContents.getGmsContentTemp()));
        setLabelLink(javaDocToolWindow.getLearnMoreUp(), LEARN_MORE, classDisplayInfo.getGDocLink());
    }

    @Override
    public void handleWhenMethod() {
        javaDocToolWindow.getLabelUp().setText(GMS_METHOD);
        javaDocToolWindow.getLabelMiddle().setText(HMS_METHOD);
        javaDocToolWindow.getLabelDown().setText(XMS_METHOD);

        DocDisplayInfo docDisplayInfo = getMethodInfo(javaDocPanelInfos);
        DocContents methodContents = docDisplayInfo.getDocContents();

        javaDocToolWindow.getDocContentDown().setText(JavaDocUtil.setTextDisplay(methodContents.getXmsContentTemp()));
        JavaDocUtil.addMethodDocListener(javaDocToolWindow, javaDocToolWindow.getDocContentDown(), javaDocPanelInfos,project);
        setLabelLink(javaDocToolWindow.getLearnMoreDown(), LEARN_MORE, docDisplayInfo.getXDocLink());

        javaDocToolWindow.getDocContentMiddle().setText(JavaDocUtil.setTextDisplay(methodContents.getHmsContentTemp()));
        JavaDocUtil.addMethodDocListener(javaDocToolWindow, javaDocToolWindow.getDocContentMiddle(), javaDocPanelInfos,project);
        setLabelLink(javaDocToolWindow.getLearnMoreMiddle(), LEARN_MORE, docDisplayInfo.getHDocLink());

        javaDocToolWindow.getDocContentUp().setText(JavaDocUtil.setTextDisplay(methodContents.getGmsContentTemp()));
        JavaDocUtil.addMethodDocListener(javaDocToolWindow, javaDocToolWindow.getDocContentUp(), javaDocPanelInfos,project);
        setLabelLink(javaDocToolWindow.getLearnMoreUp(), LEARN_MORE, docDisplayInfo.getGDocLink());
    }

    @Override
    public void handleWhenField() {
        javaDocToolWindow.getLabelUp().setText(GMS_FILED);
        javaDocToolWindow.getLabelMiddle().setText(HMS_FILED);

        // when gms api is field，xms can be a field or method
        setXmsContent(javaDocPanelInfos, javaDocToolWindow, javaDocToolWindow.getLabelDown(),
            javaDocToolWindow.getDocContentDown(), javaDocToolWindow.getLearnMoreDown(),project);

        DocDisplayInfo fieldDisplayInfo = getFieldInfo(javaDocPanelInfos);
        DocContents fieldContents = fieldDisplayInfo.getDocContents();

        javaDocToolWindow.getDocContentMiddle().setText(JavaDocUtil.setTextDisplay(fieldContents.getHmsContentTemp()));
        setLabelLink(javaDocToolWindow.getLearnMoreMiddle(), LEARN_MORE, fieldDisplayInfo.getHDocLink());

        javaDocToolWindow.getDocContentUp().setText(JavaDocUtil.setTextDisplay(fieldContents.getGmsContentTemp()));
        setLabelLink(javaDocToolWindow.getLearnMoreUp(), LEARN_MORE, fieldDisplayInfo.getGDocLink());
    }
}
