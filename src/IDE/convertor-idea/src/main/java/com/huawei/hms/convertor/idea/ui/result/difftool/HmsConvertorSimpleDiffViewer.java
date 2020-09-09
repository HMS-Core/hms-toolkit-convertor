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

package com.huawei.hms.convertor.idea.ui.result.difftool;

import com.huawei.hms.convertor.idea.ui.result.conversion.DefectItem;

import com.intellij.diff.DiffContext;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.tools.simple.SimpleDiffViewer;
import com.intellij.diff.util.DiffUserDataKeysEx;
import com.intellij.diff.util.DiffUtil;

import org.jetbrains.annotations.CalledInAwt;
import org.jetbrains.annotations.NotNull;

/**
 * Hms Convertor Simple Diff Viewer
 *
 * @since 2019/12/2
 */
public class HmsConvertorSimpleDiffViewer extends SimpleDiffViewer {
    @NotNull
    private DefectItem myDefectItem;

    public HmsConvertorSimpleDiffViewer(@NotNull DiffContext context, @NotNull DiffRequest request) {
        super(context, request);
        if (null != request) {
            myDefectItem = request.getUserData(HmsConvertorDiffUserDataKeys.DEFECT);
        }
    }

    @Override
    @CalledInAwt
    protected boolean doScrollToChange(@NotNull DiffUserDataKeysEx.ScrollToPolicy scrollToPolicy) {
        doScrollToChange(myDefectItem, false);
        return true;
    }

    private void doScrollToChange(DefectItem defectItem, final boolean animated) {
        int defectStartLine = Math.abs(defectItem.getDefectStartLine());
        int defectEndLine = Math.abs(defectItem.getDefectEndLine());
        defectStartLine = defectStartLine > 0 ? (defectStartLine - 1) : 0;
        defectEndLine = defectEndLine > 0 ? (defectEndLine - 1) : 0;

        int fixStartLine = Math.abs(defectItem.getFixStartLine());
        int fixEndLine = Math.abs(defectItem.getFixEndLine());
        fixStartLine = fixStartLine > 0 ? (fixStartLine - 1) : 0;
        fixEndLine = fixEndLine > 0 ? (fixEndLine - 1) : 0;

        DiffUtil.moveCaret(getEditor1(), defectStartLine);
        DiffUtil.moveCaret(getEditor2(), fixStartLine);

        getSyncScrollSupport().makeVisible(getCurrentSide(), defectStartLine, defectEndLine, fixStartLine, fixEndLine,
            animated);
    }

}
