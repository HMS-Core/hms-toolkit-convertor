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

import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.util.FixbotConstants;
import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.project.convert.CodeConvertService;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.core.result.conversion.ConversionPointDesc;
import com.huawei.hms.convertor.core.result.conversion.ConvertType;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.idea.util.StringUtil;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Code convert and revert implementation
 *
 * @since 2020-04-13
 */
@Slf4j
public class CodeConvertServiceImpl implements CodeConvertService {

    private Map<String, Project> projectMap = new HashMap<>();

    @Override
    public Result convert(String projectPath, ConversionItem conversionItem) {
        if (conversionItem.getConvertType().equals(ConvertType.MANUAL)) {
            return Result.ok();
        }
        Project project = projectMap.get(projectPath);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            final String file = projectPath + Constant.UNIX_FILE_SEPARATOR + conversionItem.getFile();
            final VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(file);
            if (virtualFile == null || !virtualFile.exists()) {
                BalloonNotifications.showErrorNotification(
                    "File doesn't exist: " + FileUtil.unifyToUnixFileSeparator(file), project, Constant.PLUGIN_NAME,
                    true);
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
                    for (int i = (lineCount - 1); i < insertLine; i++) {
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
            copyExtraClassFile(projectPath, conversionItem);
        });
        return Result.ok();
    }

    @Override
    public void init(String projectPath) {
        getProject(projectPath);
    }

    @Override
    public Result revert(String projectPath, ConversionItem defectItem) {
        if (defectItem.getConvertType().equals(ConvertType.MANUAL)) {
            return Result.ok();
        }
        Project project = projectMap.get(projectPath);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            final String file = projectPath + Constant.UNIX_FILE_SEPARATOR + defectItem.getFile();
            final VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(file);
            if (virtualFile == null || !virtualFile.exists()) {
                BalloonNotifications.showErrorNotification(
                    "File doesn't exist: " + FileUtil.unifyToUnixFileSeparator(file), project, Constant.PLUGIN_NAME,
                    true);
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

    private void copyExtraClassFile(String projectPath, ConversionItem conversionItem) {
        Set<String> extraClasses = getExtraClasses(conversionItem);
        if (extraClasses.isEmpty()) {
            return;
        }
        String repoId = ConfigCacheService.getInstance()
            .getProjectConfig(projectPath, ConfigKeyConstants.REPO_ID, String.class, "");
        String moduleName = getModuleName(conversionItem.getFile());
        String copySourceDirPath = FileUtil.unifyToUnixFileSeparator(buildCopySourceDirPath(repoId, moduleName));
        String copyTargetDirPath = FileUtil.unifyToUnixFileSeparator(buildCopyTargetDirPath(projectPath, moduleName));

        List<String> excludeList = new ArrayList<>();
        for (String extraClass : extraClasses) {
            String targetClassPath = FileUtil.unifyToUnixFileSeparator(copyTargetDirPath + extraClass);
            File targetFile = new File(targetClassPath);
            if (targetFile.exists()) {
                continue;
            }
            String sourceClassPath = FileUtil.unifyToUnixFileSeparator(copySourceDirPath + extraClass);

            makeDirIncrementally(copyTargetDirPath, FileUtil.unifyToUnixFileSeparator(extraClass));
            boolean copyResult = FileUtil.copyFile(sourceClassPath, targetClassPath);
            if (!copyResult) {
                log.warn("copy failed: {}", extraClass);
                // A notification will be sent when the copy fails.
                BalloonNotifications.showErrorNotification(
                    "File copy failed: " + FileUtil.unifyToUnixFileSeparator(extraClass), projectMap.get(projectPath),
                    Constant.PLUGIN_NAME, true);
                return;
            }

            if (targetClassPath.endsWith(XmsConstants.MY_APP_FILE)) {
                continue;
            }
            excludeList.add(targetClassPath);
        }

        updateExcludePath(projectPath, excludeList);
    }

    private void updateExcludePath(String projectPath, List<String> excludeList) {
        if (CollectionUtils.isEmpty(excludeList)) {
            return;
        }
        List<String> excludesInConfig = ConfigCacheService.getInstance()
            .getProjectConfig(projectPath, ConfigKeyConstants.EXCLUDE_PATH, List.class, new ArrayList());
        excludesInConfig.addAll(excludeList);
        List<String> excludePaths = excludesInConfig.stream().distinct().collect(Collectors.toList());
        ConfigCacheService.getInstance()
            .updateProjectConfig(projectPath, ConfigKeyConstants.EXCLUDE_PATH, JSON.toJSON(excludePaths));
    }

    private String buildCopySourceDirPath(String repoId, String moduleName) {
        StringBuilder copySourceDirPath = new StringBuilder(PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH);
        copySourceDirPath.append(repoId)
            .append(Constant.UNIX_FILE_SEPARATOR)
            .append(FixbotConstants.FIXBOT_DIR)
            .append(Constant.UNIX_FILE_SEPARATOR)
            .append(moduleName)
            .append(ProjectConstants.SourceDir.JAVA_SRC_DIR)
            .append(Constant.UNIX_FILE_SEPARATOR);
        return copySourceDirPath.toString();
    }

    private String buildCopyTargetDirPath(String projectPath, String moduleName) {
        StringBuilder copyTargetDirPath = new StringBuilder(projectPath);
        copyTargetDirPath.append(Constant.UNIX_FILE_SEPARATOR)
            .append(moduleName)
            .append(ProjectConstants.SourceDir.JAVA_SRC_DIR)
            .append(Constant.UNIX_FILE_SEPARATOR);
        return copyTargetDirPath.toString();
    }

    private String getModuleName(String file) {
        return file.split(ProjectConstants.SourceDir.SRC_DIR)[0];
    }

    private void makeDirIncrementally(String makeFrom, String targetPath) {
        String[] dirsAndFile = targetPath.split(Constant.UNIX_FILE_SEPARATOR);
        List<String> dirs = Arrays.stream(dirsAndFile).collect(Collectors.toList());
        dirs.remove(dirs.size() - 1);

        StringBuilder current = new StringBuilder(makeFrom);
        for (String dir : dirs) {
            current.append(dir);
            current.append(Constant.UNIX_FILE_SEPARATOR);
            File currentDir = new File(current.toString());
            if (currentDir.exists()) {
                continue;
            }
            if (!currentDir.mkdir()) {
                log.warn("make dir failed: {}", current);
            }
        }
    }

    private Set<String> getExtraClasses(ConversionItem conversionItem) {
        Set<String> extraClasses = new HashSet<String>();
        List<ConversionPointDesc> descs = conversionItem.getDescriptions();
        if (descs == null || descs.isEmpty()) {
            return extraClasses;
        }
        for (ConversionPointDesc desc : descs) {
            String extraPathStr = desc.getExtraPath();
            if (StringUtil.isEmpty(extraPathStr)) {
                continue;
            }
            String[] extraPaths = extraPathStr.split(FixbotConstants.CONVERSION_EXTRA_PATH_SEPARATOR);
            Arrays.stream(extraPaths)
                .filter(path -> !StringUtil.isEmpty(path))
                .forEach(clazz -> extraClasses.add(clazz));
        }
        return extraClasses;
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
                log.info("Get project, name: {}, path: {}", pjt.getName(), pjt.getBasePath());
            }
        }
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
