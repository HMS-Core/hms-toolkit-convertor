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

import com.intellij.diff.DiffContext;
import com.intellij.diff.DiffTool;
import com.intellij.diff.FrameDiffTool;
import com.intellij.diff.SuppressiveDiffTool;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.tools.simple.SimpleDiffTool;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * HMS convertor diff tool
 *
 * @since 2019-07-19
 */
public class HmsConvertorDiffTool implements FrameDiffTool, SuppressiveDiffTool {
    @NotNull
    @Override
    public DiffViewer createComponent(@NotNull DiffContext diffContext, @NotNull DiffRequest diffRequest) {
        return new HmsConvertorSimpleDiffViewer(diffContext, diffRequest);
    }

    @Override
    public List<Class<? extends DiffTool>> getSuppressedTools() {
        return Collections.<Class<? extends DiffTool>> singletonList(SimpleDiffTool.class);
    }

    @NotNull
    @Override
    public String getName() {
        return SimpleDiffTool.INSTANCE.getName();
    }

    @Override
    public boolean canShow(@NotNull DiffContext diffContext, @NotNull DiffRequest diffRequest) {
        if (diffRequest == null || diffRequest.getUserData(HmsConvertorDiffUserDataKeys.DEFECT) == null) {
            return false;
        }
        return true;
    }

}
