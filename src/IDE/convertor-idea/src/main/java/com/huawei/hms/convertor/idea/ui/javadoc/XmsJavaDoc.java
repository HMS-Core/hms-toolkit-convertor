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

import com.huawei.generator.ast.custom.XFieldDoc;
import com.huawei.generator.ast.custom.XMethodDoc;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.util.JavaDocUtil;
import com.huawei.inquiry.docs.EntireDoc;
import com.huawei.inquiry.docs.XDocs;
import com.huawei.inquiry.docs.ZDocs;
import com.huawei.inquiry.docs.ZFieldDoc;
import com.huawei.inquiry.docs.ZMethodDoc;
import com.intellij.openapi.project.Project;

import java.util.Set;

/**
 * java doc object when api belong to xms
 *
 * @since 2020-08-04
 */
public class XmsJavaDoc extends JavaDocService {

    private Project project;

    public XmsJavaDoc(JavaDocPanelInfos javaDocPanelInfos, JavaDocToolWindow javaDocToolWindow, Project project) {
        super(javaDocPanelInfos, javaDocToolWindow);
        this.project = project;
    }

    @Override
    public void handleWhenClass() {
        javaDocToolWindow.getLabelUp().setText(UIConstants.JavaDoc.XMS_CLASS);
        javaDocToolWindow.getLabelMiddle().setText(UIConstants.JavaDoc.HMS_CLASS);
        javaDocToolWindow.getLabelDown().setText(UIConstants.JavaDoc.GMS_CLASS);

        DocDisplayInfo classDisplayInfo = JavaDocUtil.getClassInfo(javaDocPanelInfos);
        DocContents classContents = classDisplayInfo.getDocContents();

        javaDocToolWindow.getDocContentUp().setText(JavaDocUtil.setTextDisplay(classContents.getXmsContentTemp()));
        JavaDocUtil.setLabelLink(javaDocToolWindow.getLearnMoreUp(), UIConstants.JavaDoc.LEARN_MORE, classDisplayInfo.getXDocLink());

        javaDocToolWindow.getDocContentMiddle().setText(JavaDocUtil.setTextDisplay(classContents.getHmsContentTemp()));
        JavaDocUtil.setLabelLink(javaDocToolWindow.getLearnMoreMiddle(), UIConstants.JavaDoc.LEARN_MORE, classDisplayInfo.getHDocLink());

        javaDocToolWindow.getDocContentDown().setText(JavaDocUtil.setTextDisplay(classContents.getGmsContentTemp()));
        JavaDocUtil.setLabelLink(javaDocToolWindow.getLearnMoreDown(), UIConstants.JavaDoc.LEARN_MORE, classDisplayInfo.getGDocLink());
    }

    @Override
    public void handleWhenMethod() {
        javaDocToolWindow.getLabelUp().setText(UIConstants.JavaDoc.XMS_METHOD);
        String xMethodinfo = "";
        XMethodDoc xmsMethodDocLink = null;
        Set<XDocs> xMethodSet = javaDocPanelInfos.getEntireDoc().getXDocs();
        if (xMethodSet != null) {
            xMethodinfo = JavaDocUtil.getXMethodInfo(xMethodSet);
            if (!xMethodSet.isEmpty()) {
                xmsMethodDocLink = (XMethodDoc) xMethodSet.toArray()[0];
            }
        }
        String xmsMethodContent = xMethodSet == null ? UIConstants.JavaDoc.NOT_FIND_LABEL : xMethodinfo;
        DocContents methodContents = JavaDocUtil.updataDocContent(javaDocPanelInfos, xmsMethodContent, "", "");
        javaDocToolWindow.getDocContentUp().setText(JavaDocUtil.setTextDisplay(methodContents.getXmsContentTemp()));
        JavaDocUtil.addMethodDocListener(javaDocToolWindow, javaDocToolWindow.getDocContentUp(), javaDocPanelInfos,project);
        JavaDocUtil.setLabelLink(javaDocToolWindow.getLearnMoreUp(), UIConstants.JavaDoc.LEARN_MORE, xmsMethodDocLink);

        // when xms api is method，gms and hms can be a field or method
        setHmsMethodOrField();
        setGmsMethodOrField();
    }

    @Override
    public void handleWhenField() {
        javaDocToolWindow.getLabelUp().setText(UIConstants.JavaDoc.XMS_FILED);
        javaDocToolWindow.getLabelMiddle().setText(UIConstants.JavaDoc.HMS_FILED);
        javaDocToolWindow.getLabelDown().setText(UIConstants.JavaDoc.GMS_FILED);

        String xmsFieldinfo = "";
        XFieldDoc xFieldDocLink = null;
        Set<XDocs> xmsFieldSet = javaDocPanelInfos.getEntireDoc().getXDocs();
        if (xmsFieldSet != null) {
            xmsFieldinfo = JavaDocUtil.getXFieldDoc(xmsFieldSet);
            if (!xmsFieldSet.isEmpty()){
                xFieldDocLink = (XFieldDoc) xmsFieldSet.toArray()[0];
            }
        }

        String hmsFieldinfo = "";
        ZFieldDoc hFieldDocLink = null;
        Set<ZDocs> hmsFieldSet = javaDocPanelInfos.getEntireDoc().getHDocs();
        if (hmsFieldSet != null) {
            hmsFieldinfo =JavaDocUtil.getZFieldDoc(hmsFieldSet);
            if (!hmsFieldSet.isEmpty()){
                hFieldDocLink = (ZFieldDoc) hmsFieldSet.toArray()[0];
            }
        }

        ZFieldDoc gFieldDocLink = null;
        Set<ZDocs> gmsFieldSet = javaDocPanelInfos.getEntireDoc().getGDocs();
        if (gmsFieldSet != null) {
            if (!gmsFieldSet.isEmpty()){
                gFieldDocLink = (ZFieldDoc) gmsFieldSet.toArray()[0];
            }
        }

        String xmsFieldContent = xmsFieldSet == null ? UIConstants.JavaDoc.NOT_FIND_LABEL : xmsFieldinfo;
        String hmsFieldContent = hmsFieldSet == null ? UIConstants.JavaDoc.NOT_FIND_LABEL : hmsFieldinfo;
        String gmsFieldContent = gmsFieldSet == null ? UIConstants.JavaDoc.NOT_FIND_LABEL : HmsConvertorBundle.message("gms_api_details");

        DocContents fieldContents =
            JavaDocUtil.updataDocContent(javaDocPanelInfos, xmsFieldContent, hmsFieldContent, gmsFieldContent);
        javaDocToolWindow.getDocContentUp().setText(JavaDocUtil.setTextDisplay(fieldContents.getXmsContentTemp()));
        JavaDocUtil.setLabelLink(javaDocToolWindow.getLearnMoreUp(), UIConstants.JavaDoc.LEARN_MORE, xFieldDocLink);

        javaDocToolWindow.getDocContentMiddle().setText(JavaDocUtil.setTextDisplay(fieldContents.getHmsContentTemp()));
        JavaDocUtil.setLabelLink(javaDocToolWindow.getLearnMoreMiddle(), UIConstants.JavaDoc.LEARN_MORE, hFieldDocLink);

        javaDocToolWindow.getDocContentDown().setText(JavaDocUtil.setTextDisplay(fieldContents.getGmsContentTemp()));
        JavaDocUtil.setLabelLink(javaDocToolWindow.getLearnMoreDown(), UIConstants.JavaDoc.LEARN_MORE, gFieldDocLink);
    }

    private void setHmsMethodOrField() {
        Set<ZDocs> hmsDocsSet = javaDocPanelInfos.getEntireDoc().getHDocs();
        EntireDoc.EXCEPTIONTYPE exceptionType = javaDocPanelInfos.getEntireDoc().getExceptiontype();
        String hmsMethodContent = "";
        if (hmsDocsSet == null || hmsDocsSet.size() == 0) {
            hmsMethodContent = hmsMethodContent + UIConstants.JavaDoc.NOT_FIND_LABEL;
            if (exceptionType != null) {
                switch (exceptionType) {
                    case HMSNOTSUPPORT:
                        hmsMethodContent = UIConstants.JavaDoc.HMS_NOT_SUPPORT;
                        break;
                    case GMSNOTSUPPORT:
                        hmsMethodContent = UIConstants.JavaDoc.HMS_NOT_SUPPORT;
                        break;
                    case KITNOTSUPPORT:
                        hmsMethodContent = UIConstants.JavaDoc.KIT_NOT_SUPPORT;
                        break;
                    default:
                        break;
                }
            }
            javaDocToolWindow.getLabelMiddle().setText(UIConstants.JavaDoc.HMS_METHOD);
            javaDocToolWindow.getDocContentMiddle().setText(JavaDocUtil.setTextDisplay(hmsMethodContent));
            return;
        }
        ZDocs hmsDocs = (ZDocs) hmsDocsSet.toArray()[0];
        if (hmsDocs.getTypeClass() == ZMethodDoc.class) {
            javaDocToolWindow.getLabelMiddle().setText(UIConstants.JavaDoc.HMS_METHOD);
            javaDocToolWindow.getDocContentMiddle().setText(JavaDocUtil.setTextDisplay(JavaDocUtil.getZMethodInfo(hmsDocsSet)));
            JavaDocUtil.addMethodDocListener(javaDocToolWindow, javaDocToolWindow.getDocContentMiddle(),
                javaDocPanelInfos,project);
            JavaDocUtil.setLabelLink(javaDocToolWindow.getLearnMoreMiddle(), UIConstants.JavaDoc.LEARN_MORE, hmsDocs);
        } else if (hmsDocs.getTypeClass() == ZFieldDoc.class) {
            javaDocToolWindow.getLabelMiddle().setText(UIConstants.JavaDoc.HMS_FILED);
            String hmsFieldContent = "";
            for (int i = 0; i < hmsDocsSet.size(); i++) {
                ZFieldDoc hmsFieldDoc = (ZFieldDoc) hmsDocsSet.toArray()[i];
                hmsFieldContent = hmsFieldDoc.getSignature() + UIConstants.Html.BR + hmsFieldDoc.getDes();
            }
            javaDocToolWindow.getDocContentMiddle().setText(JavaDocUtil.setTextDisplay(hmsFieldContent));
            JavaDocUtil.setLabelLink(javaDocToolWindow.getLearnMoreMiddle(), UIConstants.JavaDoc.LEARN_MORE, hmsDocs);
        }
    }

    private void setGmsMethodOrField() {
        Set<ZDocs> gmsDocsSet = javaDocPanelInfos.getEntireDoc().getGDocs();
        EntireDoc.EXCEPTIONTYPE exceptionType = javaDocPanelInfos.getEntireDoc().getExceptiontype();
        String gmsMethodContent = "";
        if (gmsDocsSet == null || gmsDocsSet.size() ==0) {
            gmsMethodContent = gmsMethodContent + UIConstants.JavaDoc.NOT_FIND_LABEL;
            if (exceptionType != null) {
                switch (exceptionType) {
                    case GMSNOTSUPPORT:
                        gmsMethodContent = UIConstants.JavaDoc.GMS_NOT_SUPPORT;
                        break;
                    case KITNOTSUPPORT:
                        gmsMethodContent = UIConstants.JavaDoc.KIT_NOT_SUPPORT;
                        break;
                    case HMSNOTMATCHGMS:
                        gmsMethodContent = UIConstants.JavaDoc.GMS_NOT_SUPPORT;
                        break;
                    default:
                        break;
                }

            }
            javaDocToolWindow.getLabelDown().setText(UIConstants.JavaDoc.GMS_METHOD);
            javaDocToolWindow.getDocContentDown().setText(JavaDocUtil.setTextDisplay(gmsMethodContent));
            return;
        }
        ZDocs gmsDocs = (ZDocs) gmsDocsSet.toArray()[0];
        if (gmsDocs.getTypeClass() == ZMethodDoc.class) {
            javaDocToolWindow.getLabelDown().setText(UIConstants.JavaDoc.GMS_METHOD);
            gmsMethodContent = HmsConvertorBundle.message("gms_api_details");
            javaDocToolWindow.getDocContentDown().setText(JavaDocUtil.setTextDisplay(gmsMethodContent));
            JavaDocUtil.addMethodDocListener(javaDocToolWindow, javaDocToolWindow.getDocContentDown(),
                javaDocPanelInfos,project);
            JavaDocUtil.setLabelLink(javaDocToolWindow.getLearnMoreDown(), UIConstants.JavaDoc.LEARN_MORE, gmsDocs);
        } else if (gmsDocs.getTypeClass() == ZFieldDoc.class) {
            javaDocToolWindow.getLabelDown().setText(UIConstants.JavaDoc.GMS_FILED);
            gmsMethodContent = HmsConvertorBundle.message("gms_api_details");
            javaDocToolWindow.getDocContentDown().setText(JavaDocUtil.setTextDisplay(gmsMethodContent));
            JavaDocUtil.setLabelLink(javaDocToolWindow.getLearnMoreDown(), UIConstants.JavaDoc.LEARN_MORE, gmsDocs);
        }
    }
}