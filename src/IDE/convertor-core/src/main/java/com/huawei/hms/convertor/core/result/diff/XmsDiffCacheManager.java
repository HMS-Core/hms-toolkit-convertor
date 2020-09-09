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

package com.huawei.hms.convertor.core.result.diff;

import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.alibaba.fastjson.JSON;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Xms diff cache manager
 *
 * @since 2020-03-24
 */
@Setter
@Getter
@Slf4j
public final class XmsDiffCacheManager {
    private static final XmsDiffCacheManager XMS_DIFF_CACHE_MANAGER = new XmsDiffCacheManager();

    private Map<String, XmsDiff> xmsDiffMap = new ConcurrentHashMap<>();

    private XmsDiffCacheManager() {
    }

    /**
     * Get singleton instance of {@code XmsDiffCacheManager}
     *
     * @return The singleton instance of {@code XmsDiffCacheManager}
     */
    public static XmsDiffCacheManager getInstance() {
        return XMS_DIFF_CACHE_MANAGER;
    }

    /**
     * Set xms diff info
     *
     * @param projectBasePath projectBasePath
     * @param xmsDiff xms diff
     */
    public void setXmsDiff(String projectBasePath, XmsDiff xmsDiff) {
        if (StringUtils.isEmpty(projectBasePath) || xmsDiff == null) {
            return;
        }
        xmsDiffMap.put(projectBasePath, xmsDiff);
    }

    /**
     * Save xms diff info
     *
     * @param projectBasePath projectBasePath
     */
    public void saveXmsDiff(String projectBasePath) {
        try {
            XmsDiff xmsDiff = xmsDiffMap.get(projectBasePath);

            String folderName = ConfigCacheService.getInstance()
                .getProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, String.class, "");
            if (xmsDiff == null || !xmsDiff.isHasDiffContent() || StringUtils.isEmpty(folderName)) {
                log.warn("No xms diff content to be saved or folder name empty");
                return;
            }

            String saveFilePath =
                Paths
                    .get(PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH, folderName,
                        ProjectConstants.Result.LAST_XMSDIFF_JSON)
                    .toString();
            if (FileUtil.isInvalidDirectoryPath(saveFilePath)) {
                log.error("Invalid directory path");
                return;
            }

            File saveFile = new File(saveFilePath);
            if (!saveFile.getParentFile().exists()) {
                log.warn("{} does not exist, maybe not analysis yet.",
                    PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH + folderName);
                return;
            }
            if (saveFile.exists()) {
                boolean result = saveFile.delete();
                if (!result) {
                    log.error("Delete file error");
                }
            }
            boolean saveResult = saveFile.createNewFile();
            if (!saveResult) {
                log.error("Create new file error");
            }

            String xmsDiffString = JSON.toJSONString(xmsDiff, true);
            FileUtil.writeFile(saveFilePath, xmsDiffString);
            log.info("Save xms diff success");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Load xms diff info
     *
     * @param projectBasePath project base path
     * @return xms diff info
     */
    public XmsDiff loadXmsDiff(String projectBasePath) {
        String folderName = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, String.class, "");
        String saveFilePath = PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH + folderName + Constant.UNIX_FILE_SEPARATOR
            + ProjectConstants.Result.LAST_XMSDIFF_JSON;
        if (!new File(saveFilePath).exists()) {
            log.info("LastXmsDiff.json doesn't exist");
            return null;
        }

        try {
            String lastXmsDiffString = FileUtil.readToString(saveFilePath, StandardCharsets.UTF_8.toString());
            XmsDiff newXmsDiff = JSON.parseObject(lastXmsDiffString, XmsDiff.class);
            setXmsDiff(projectBasePath, newXmsDiff);
            log.info("Load summary success");
            return newXmsDiff;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
