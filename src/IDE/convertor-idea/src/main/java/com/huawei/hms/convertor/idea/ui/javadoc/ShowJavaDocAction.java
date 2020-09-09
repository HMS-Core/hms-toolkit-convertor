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

import com.huawei.hms.convertor.core.bi.enumration.CancelableViewEnum;
import com.huawei.hms.convertor.core.bi.enumration.JavaDocActionEnum;
import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.ui.common.PrivacyStatementChecker;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.huawei.hms.convertor.idea.util.JavaDocUtil;
import com.huawei.hms.convertor.idea.util.ToolWindowUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.inquiry.InquiryEntrances;
import com.huawei.inquiry.docs.EntireDoc;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.impl.compiled.ClsClassImpl;
import com.intellij.psi.impl.compiled.ClsFieldImpl;
import com.intellij.psi.impl.compiled.ClsMethodImpl;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.psi.impl.source.PsiMethodImpl;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * show java doc action
 *
 * @since 2020-07-01
 */
@Slf4j
public class ShowJavaDocAction extends AnAction {
    private String apiInfo = "";

    private JavaDocPanelInfos javaDocPanelInfos = null;

    private EntireDoc entireDoc = null;

    private Map<String, String> dependencyVersionMapForDoc = new HashMap<>();

    private boolean isPrivate;

    private ApiDetailToolWindow apiDetailToolWindow;

    @Override
    public void actionPerformed(AnActionEvent event) {
        if (event == null) {
            return;
        }
        Project project = event.getProject();
        PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        if (project == null || psiElement == null) {
            log.warn("psi element is null!");
            BalloonNotifications.showWarnNotification(HmsConvertorBundle.message("psi_element_is_null"), project,
                Constant.PLUGIN_NAME, true);
            return;
        }

        if (PrivacyStatementChecker.isNotAgreed(project)) {
            // bi report action: trace cancel operation.
            BIReportService.getInstance().traceCancelListener(project.getBasePath(), CancelableViewEnum.PRIVACY);
            return;
        }
        // bi report action: menu click.
        BIReportService.getInstance().traceJavaDocSelection(project.getBasePath(), JavaDocActionEnum.HMS_API_HELPER);

        dependencyVersionMapForDoc = JavaDocUtil.getDependencyVersionMap(project);
        if (psiElement.toString().startsWith(UIConstants.JavaDoc.PSI_CLASS)) {
            getInfosWhenClass(psiElement);
        } else if (psiElement.toString().startsWith(UIConstants.JavaDoc.PSI_METHOD)) {
            getInfosWhenMethod(psiElement);
        } else if (psiElement.toString().startsWith(UIConstants.JavaDoc.PSI_FIELD)) {
            getInfosWhenField(psiElement);
        } else {
            javaDocPanelInfos = new JavaDocPanelInfos(HmsConvertorBundle.message("unknow_api"), null, null, isPrivate);
        }
        isPrivate = false;
        apiDetailToolWindow = ToolWindowUtil.getApiDetailToolWindow(project).get();
        JavaDocUtil.showJavaDocWindow(project, javaDocPanelInfos);
        LafManager.getInstance().addLafManagerListener(new LafManagerListener() {
            @Override
            public void lookAndFeelChanged(LafManager source) {
                apiDetailToolWindow.refreshData();
                apiDetailToolWindow.getJavaDocToolWindow().refreshData(javaDocPanelInfos);
            }
        });
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        String file = anActionEvent.getData(CommonDataKeys.PSI_FILE).getFileType().getName();
        anActionEvent.getPresentation().setEnabledAndVisible(file.equals("JAVA") || file.equals("CLASS"));
    }

    private String getClassInfo(PsiElement psiElement) {
        if (psiElement instanceof ClsClassImpl) {
            return ((ClsClassImpl) psiElement).getQualifiedName();
        }
        if (psiElement instanceof PsiClassImpl) {
            return ((PsiClassImpl) psiElement).getQualifiedName();
        }
        return "";
    }

    private String getMethodInfo(PsiElement psiElement) {
        String classApi = "";
        String method = psiElement.toString().split(":")[1];
        PsiElement psiParentClass = psiElement.getParent();
        List<PsiParameter> psiParameters = new ArrayList<>();

        if (psiParentClass instanceof PsiClassImpl) {
            if (((PsiMethodImpl) psiElement).hasModifierProperty(PsiModifier.PRIVATE)) {
                isPrivate = true;
            }
            classApi = ((PsiClassImpl) psiParentClass).getQualifiedName();
            psiParameters = Arrays.asList(((PsiMethodImpl) psiElement).getParameterList().getParameters());
        }

        if (psiParentClass instanceof ClsClassImpl) {
            if (((ClsMethodImpl) psiElement).hasModifierProperty(PsiModifier.PRIVATE)) {
                isPrivate = true;
            }
            classApi = ((ClsClassImpl) psiParentClass).getQualifiedName();
            psiParameters = Arrays.asList(((ClsMethodImpl) psiElement).getParameterList().getParameters());
        }
        StringBuilder methodInfo = new StringBuilder().append(classApi).append(".").append(method).append("(");

        if (psiParameters.isEmpty()) {
            methodInfo.append(")");
            return methodInfo.toString();
        }

        for (int i = 0; i < psiParameters.size() - 1; i++) {
            methodInfo.append(psiParameters.get(i).getType().getCanonicalText()).append(",");
        }
        methodInfo.append(psiParameters.get(psiParameters.size() - 1).getType().getCanonicalText()).append(")");
        return methodInfo.toString();
    }

    private String getFieldInfo(PsiElement psiElement) {
        String classApi = "";
        String field = psiElement.toString().split(":")[1];
        PsiElement psiParentClass = psiElement.getParent();
        if (psiParentClass instanceof ClsClassImpl) {
            classApi = ((ClsClassImpl) psiParentClass).getQualifiedName();
            if (((ClsFieldImpl) psiElement).hasModifierProperty(PsiModifier.PRIVATE)) {
                isPrivate = true;
            }
        }

        if (psiParentClass instanceof PsiClassImpl) {
            classApi = ((PsiClassImpl) psiParentClass).getQualifiedName();
            if (((PsiFieldImpl) psiElement).hasModifierProperty(PsiModifier.PRIVATE)) {
                isPrivate = true;
            }
        }

        StringBuilder fieldInfo = new StringBuilder().append(classApi).append(".").append(field);
        return fieldInfo.toString();
    }

    private void getInfosWhenClass(PsiElement psiElement) {
        apiInfo = getClassInfo(psiElement);
        entireDoc =
            InquiryEntrances.getInstance(System.getProperty(XmsConstants.KEY_XMS_JAR), dependencyVersionMapForDoc)
                .getDocs(apiInfo, EntireDoc.SCOPETYPE.CLASS);
        javaDocPanelInfos = new JavaDocPanelInfos(apiInfo, entireDoc, EntireDoc.SCOPETYPE.CLASS, isPrivate);
    }

    private void getInfosWhenMethod(PsiElement psiElement) {
        apiInfo = getMethodInfo(psiElement);
        entireDoc =
            InquiryEntrances.getInstance(System.getProperty(XmsConstants.KEY_XMS_JAR), dependencyVersionMapForDoc)
                .getDocs(apiInfo, EntireDoc.SCOPETYPE.METHOD);
        javaDocPanelInfos = new JavaDocPanelInfos(apiInfo, entireDoc, EntireDoc.SCOPETYPE.METHOD, isPrivate);
    }

    private void getInfosWhenField(PsiElement psiElement) {
        apiInfo = getFieldInfo(psiElement);
        entireDoc =
            InquiryEntrances.getInstance(System.getProperty(XmsConstants.KEY_XMS_JAR), dependencyVersionMapForDoc)
                .getDocs(apiInfo, EntireDoc.SCOPETYPE.FIELD);
        javaDocPanelInfos = new JavaDocPanelInfos(apiInfo, entireDoc, EntireDoc.SCOPETYPE.FIELD, isPrivate);
    }

}