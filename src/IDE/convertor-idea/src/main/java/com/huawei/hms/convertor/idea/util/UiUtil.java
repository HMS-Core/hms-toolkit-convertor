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

package com.huawei.hms.convertor.idea.util;

import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;
import com.huawei.hms.convertor.idea.ui.result.conversion.DefectItem;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.util.ui.UIUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.swing.Icon;

/**
 * UI util
 *
 * @since 2019-07-11
 */
public final class UiUtil {
    private UiUtil() {
    }

    /**
     * Show defect tips
     *
     * @param defectItem a defect item
     * @param editor IDE editor
     */
    public static void showDefectTips(@NotNull DefectItem defectItem, @Nullable Editor editor) {
        if (null == editor || !editor.getComponent().isShowing()) {
            return;
        }

        final List<ConversionPointDesc> descriptions = defectItem.getDescriptions();
        final ListPopupStep<ConversionPointDesc> listPopupStep =
            new BaseListPopupStep<ConversionPointDesc>(null, descriptions) {
                @Nullable
                @Override
                public PopupStep onChosen(ConversionPointDesc selectedValue, boolean finalChoice) {
                    final String descUrl = selectedValue.getUrl();
                    if (StringUtil.isEmptyOrSpaces(descUrl)) {
                        return super.onChosen(selectedValue, finalChoice);
                    }

                    UIUtil.invokeLaterIfNeeded(() -> {
                        BrowserUtil.browse(descUrl);
                    });
                    return super.onChosen(selectedValue, finalChoice);
                }

                @Override
                public String getTextFor(ConversionPointDesc value) {
                    final String descText = wrapComment(value.getText(), 120);
                    return StringUtil.isEmptyOrSpaces(descText) ? "" : descText.trim();
                }

                @Override
                public Icon getIconFor(ConversionPointDesc value) {
                    final String descUrl = value.getUrl();
                    return StringUtil.isEmptyOrSpaces(descUrl) ? IconUtil.EMPTY : IconUtil.EXPLORER;
                }
            };

        final ListPopupImpl listPopup = new ListPopupImpl(listPopupStep);
        listPopup.showInBestPositionFor(editor);
    }

    /**
     * insect line wrap for string
     *
     * @param string string to change
     * @param wrapLength wrap length
     */
    public static String wrapComment(String string, int wrapLength) {
        if (string.length() <= wrapLength) {
            return string;
        }
        String comment = "<html>" + string + "</html>";
        StringBuilder sb = new StringBuilder(comment);
        int spaceIndex = -1;
        int replaceCount = 0;
        for (int i = 0; i < comment.length(); i++) {
            if (i % wrapLength == 0 && spaceIndex > -1) {
                sb.replace(spaceIndex, spaceIndex + 1, "<br>");
                replaceCount++;
                spaceIndex = -1;
            }
            if (comment.charAt(i) == ' ') {
                spaceIndex = i + replaceCount * 3;
            }
        }
        return sb.toString();
    }

    /**
     * Show message in statusbar
     *
     * @param project project
     * @param message the message to show in statusbar
     */
    public static void setStatusBarInfo(final Project project, final String message) {
        if (null == project) {
            return;
        }

        ApplicationManager.getApplication()
            .invokeLater(() -> WindowManager.getInstance().getStatusBar(project).setInfo(message));
    }

}
