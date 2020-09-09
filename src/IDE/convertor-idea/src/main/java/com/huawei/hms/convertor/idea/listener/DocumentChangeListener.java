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

package com.huawei.hms.convertor.idea.listener;

import com.huawei.hms.convertor.core.event.context.EventType;
import com.huawei.hms.convertor.core.event.context.project.ProjectEvent;
import com.huawei.hms.convertor.core.result.conversion.ChangedCode;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.openapi.EventService;
import com.huawei.hms.convertor.util.FileUtil;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Document change listener
 *
 * @since 2019-07-18
 */
@Slf4j
public class DocumentChangeListener implements DocumentListener {
    private String inspectPath;

    private int oldLineCount;

    private int changeEndLine;

    private Project project;

    public DocumentChangeListener(@NotNull String inspectPath, Project project) {
        this.inspectPath = inspectPath;
        this.project = project;
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        if (event == null) {
            return;
        }
        Document doc = event.getDocument();
        int changeLineCount = doc.getLineCount() - oldLineCount;
        // When the editing operation does not change the line number or
        // changes the last line, no need to maintain the line number.
        if (0 == changeLineCount || changeEndLine == oldLineCount) {
            return;
        }
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(doc);
        if (virtualFile == null) {
            return;
        }
        if ((inspectPath.length() + 1) > virtualFile.getPath().length()) {
            log.warn("Warring virtual File Path length is: {}, inspectPath length: {}", virtualFile.getPath().length(),
                inspectPath.length());
            return;
        }
        String filePath = FileUtil.unifyToUnixFileSeparator(virtualFile.getPath().substring(inspectPath.length() + 1));
        String newContent = event.getNewFragment().toString();
        String oldContent = event.getOldFragment().toString();
        ChangedCode changedCode = new ChangedCode(filePath, changeEndLine, newContent, oldContent, changeLineCount);
        // Submit a document editing event.
        EventService.getInstance()
            .submitProjectEvent(ProjectEvent.<ChangedCode, List<ConversionItem>> of(inspectPath, EventType.EDIT_EVENT,
                changedCode, (message) -> refreshConversionTable(message)));
    }

    public void refreshConversionTable(List<ConversionItem> messages) {
        HmsConvertorUtil.getHmsConvertorToolWindow(project)
            .ifPresent(hmsConvertorToolWindow -> {
                // Refresh the conversion list displayed on the tool window.
                hmsConvertorToolWindow.getSourceConvertorToolWindow().refreshResultTable(messages);
            });
    }

    @Override
    public void beforeDocumentChange(@NotNull DocumentEvent event) {
        if (event == null) {
            return;
        }
        Document doc = event.getDocument();
        changeEndLine = doc.getLineNumber(event.getOffset() + event.getOldLength()) + 1;
        oldLineCount = doc.getLineCount();
    }

}
