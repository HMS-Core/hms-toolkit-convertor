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

package com.huawei.hms.convertor.idea.spi;

import com.huawei.hms.convertor.core.project.convert.CodeConvertService;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.core.result.conversion.ConvertType;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.Constant;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Code convert and revert implementation
 *
 * @since 2020-04-13
 */
@Slf4j
public class CodeConvertServiceImpl implements CodeConvertService {
    Map<String, Project> projectMap = new HashMap<>();

    @Override
    public Result convert(String projectPath, ConversionItem conversionItem) {
        if (conversionItem.getConvertType().equals(ConvertType.MANUAL)) {
            return Result.ok();
        }
        Project project = projectMap.get(projectPath);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            final String file = projectPath + File.separatorChar + conversionItem.getFile();
            final VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(file);
            if (virtualFile == null || !virtualFile.exists()) {
                BalloonNotifications.showErrorNotification("File doesn't exist: " + file.replace("\\", "/"), project,
                    Constant.PLUGIN_NAME, true);
                return;
            }
            virtualFile.refresh(false, false);
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            final int lineCount = document.getLineCount();

            if (conversionItem.getDefectStartLine() < 0) { // Insert
                final int insertLine = -conversionItem.getDefectStartLine() - 1;
                if (insertLine < lineCount) {
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        int insertOffset1 = document.getLineStartOffset(insertLine);
                        String insertContent1;
                        if (insertLine == lineCount - 1) {
                            insertContent1 = conversionItem.getFixContent();
                        } else {
                            insertContent1 = conversionItem.getFixContent() + Constant.LINE_SEPARATOR;
                        }
                        document.insertString(insertOffset1, insertContent1);

                        postProcessingAfterConvert(project, document);
                    });
                } else {
                    // InsertLine exceed the file
                    final StringBuilder sb = new StringBuilder();
                    for (int i = lineCount - 1; i < insertLine; i++) {
                        sb.append(Constant.LINE_SEPARATOR);
                    }

                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        int insertOffset2 = document.getLineEndOffset(lineCount - 1);
                        String insertContent2 = sb.toString() + conversionItem.getFixContent();
                        conversionItem.setFileTailConvert(true);
                        document.insertString(insertOffset2, insertContent2);

                        postProcessingAfterConvert(project, document);
                    });
                }
            } else if (conversionItem.getFixStartLine() < 0) { // Delete
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    int deleteStartOffset = document.getLineStartOffset(conversionItem.getDefectStartLine() - 1);
                    int deleteEndOffsetTemp = document.getLineEndOffset(conversionItem.getDefectEndLine() - 1) + 1;
                    int deleteEndOffset = deleteEndOffsetTemp > document.getLineEndOffset(lineCount - 1)
                        ? document.getLineEndOffset(lineCount - 1) : deleteEndOffsetTemp;
                    document.deleteString(deleteStartOffset, deleteEndOffset);

                    postProcessingAfterConvert(project, document);
                });
            } else {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    int convertStartOffset = document.getLineStartOffset(conversionItem.getDefectStartLine() - 1);
                    int convertEndOffset = document.getLineEndOffset(conversionItem.getDefectEndLine() - 1);
                    String convertContent = conversionItem.getFixContent();
                    document.replaceString(convertStartOffset, convertEndOffset, convertContent);

                    postProcessingAfterConvert(project, document);
                });
            }
        });
        return Result.ok();
    }

    @Override
    public void init(String projectPath) {
        getProject(projectPath);
    }

    private void getProject(String projectPath) {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects == null) {
            log.error("Can't get current project");
            return;
        }
        for (Project pjt : projects) {
            if (pjt.getBasePath().equals(projectPath)) {
                projectMap.put(projectPath, pjt);
                log.info("Get project, name = {}, path = {}", pjt.getName(), pjt.getBasePath());
            }
        }
    }

    @Override
    public Result revert(String projectPath, ConversionItem defectItem) {
        if (defectItem.getConvertType().equals(ConvertType.MANUAL)) {
            return Result.ok();
        }
        Project project = projectMap.get(projectPath);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            final String file = projectPath + File.separatorChar + defectItem.getFile();
            final VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(file);
            if (virtualFile == null || !virtualFile.exists()) {
                BalloonNotifications.showErrorNotification("File doesn't exist: " + file.replace("\\", "/"), project,
                    Constant.PLUGIN_NAME, true);
                return;
            }
            virtualFile.refresh(false, false);
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            final int lineCount = document.getLineCount();

            if (defectItem.getDefectStartLine() < 0) { // Insert
                final int insertStartLine = -defectItem.getDefectStartLine();
                final int insertEndLine = insertStartLine + (defectItem.getFixEndLine() - defectItem.getFixStartLine());

                WriteCommandAction.runWriteCommandAction(project, () -> {
                    int insertStartOffset = document.getLineStartOffset(insertStartLine - 1);
                    int insertEndOffsetTemp = document.getLineEndOffset(insertEndLine - 1) + 1;
                    int insertEndOffset = insertEndOffsetTemp > document.getLineEndOffset(lineCount - 1)
                        ? document.getLineEndOffset(lineCount - 1) : insertEndOffsetTemp;
                    document.deleteString(insertStartOffset, insertEndOffset);

                    postProcessingAfterRevert(project, document);
                });
            } else if (defectItem.getFixStartLine() < 0) { // Delete
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    int deleteStartOffset = document.getLineStartOffset(defectItem.getDefectStartLine() - 1);
                    String deleteContent = defectItem.getDefectContent() + Constant.LINE_SEPARATOR;
                    document.insertString(deleteStartOffset, deleteContent);

                    postProcessingAfterRevert(project, document);
                });
            } else {
                final int revertStartLine = defectItem.getDefectStartLine();
                final int revertEndLine = revertStartLine + (defectItem.getFixEndLine() - defectItem.getFixStartLine());

                WriteCommandAction.runWriteCommandAction(project, () -> {
                    int revertStartOffset = document.getLineStartOffset(revertStartLine - 1);
                    int revertEndOffset = document.getLineEndOffset(revertEndLine - 1);
                    String revertContent = defectItem.getDefectContent();
                    document.replaceString(revertStartOffset, revertEndOffset, revertContent);

                    postProcessingAfterRevert(project, document);
                });
            }
        });
        return Result.ok();
    }

    private void postProcessingAfterConvert(Project project, Document document) {
        HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
            hmsConvertorToolWindow.getSourceConvertorToolWindow().postProcessingAfterConvert(document);
        });
    }

    private void postProcessingAfterRevert(Project project, Document document) {
        HmsConvertorUtil.getHmsConvertorToolWindow(project).ifPresent(hmsConvertorToolWindow -> {
            hmsConvertorToolWindow.getSourceConvertorToolWindow().postProcessingAfterRevert(document);
        });
    }
}
