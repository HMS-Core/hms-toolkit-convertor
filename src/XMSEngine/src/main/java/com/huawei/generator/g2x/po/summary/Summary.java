/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.generator.g2x.po.summary;

import com.google.gson.annotations.Expose;
import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.generator.g2x.processor.XmsConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Summary class
 *
 * @since 2019-02-20
 */
public class Summary {
    private static final Logger LOGGER = LoggerFactory.getLogger(Summary.class);

    // where xmsadapter is
    @Expose
    public String moduleLocation;

    @Expose
    public boolean app;

    @Expose
    public List<GeneratorStrategyKind> strategy = new LinkedList<>();

    // exclude readme ,build and summary itself.
    // key : relative path begin with xmsadaptor; value : sha256
    @Expose
    public TreeMap<String, String> allFiles = new TreeMap<>();

    @Expose
    private String pluginVersion;

    @Expose
    private Set<String> kitNames = new HashSet<>();

    private String getPluginVersion() {
        return pluginVersion;
    }

    public void setModuleLocation(String moduleLocation) {
        this.moduleLocation = moduleLocation;
    }

    public boolean isApp() {
        return app;
    }

    public void setApp(boolean app) {
        this.app = app;
    }

    public List<GeneratorStrategyKind> getStrategy() {
        return strategy;
    }

    public void setStrategy(List<GeneratorStrategyKind> strategy) {
        this.strategy = strategy;
    }

    public Set<String> getKitNames() {
        return kitNames;
    }

    public void setKitNames(Set<String> kitNames) {
        this.kitNames = kitNames;
    }

    public Set<String> getKits() {
        Set<String> result = new HashSet<>(kitNames);
        result.removeIf(x -> x.equals("util") || x.equals("unKnown"));
        LOGGER.info("get kits from summary {} of {}， strategy is {}", result.toString(), moduleLocation, strategy);
        return result;
    }

    /**
     * compute diff from this to old
     * 1. in this, not in old, add
     * 2. in this, in old, sha different, mod
     * 3. not in this, in old, del
     * 
     * @param oldSummary old summary
     * @param useUpdate use update or not
     * @return diff result
     */
    public Diff diffWithAllFiles(Summary oldSummary, boolean useUpdate) {
        // create diff with time and tool version
        Diff diff = new Diff(XmsConstants.VERSION);

        for (Map.Entry<String, String> entry : allFiles.entrySet()) {
            String file = entry.getKey();
            String sha = entry.getValue();
            if (!oldSummary.allFiles.containsKey(file)) {
                // 1. in this, not old
                // put into relative
                diff.getAddListWithRelativePaths().add(file);

                // put into absolute path
                diff.getAddList().add(moduleLocation + File.separator + file);
            }

            String oldSha = oldSummary.allFiles.get(file);
            if (!sha.equals(oldSha)) {
                // 2. mod
                if (useUpdate) {
                    // for xms-self update
                    diff.getUpdatedMapRelativePaths().put(file, file);
                    diff.getUpdatedMap()
                        .put(moduleLocation + File.separator + file, oldSummary.moduleLocation + File.separator + file);
                } else {
                    // for mod
                    diff.getModMapRelativePaths().put(file, file);
                    diff.getModMap()
                        .put(moduleLocation + File.separator + file, oldSummary.moduleLocation + File.separator + file);
                }
            }
        }

        // 3. not in this, in old
        for (Map.Entry<String, String> entry : oldSummary.allFiles.entrySet()) {
            String file = entry.getKey();
            if (!allFiles.containsKey(file)) {
                diff.getDelList().add(oldSummary.moduleLocation + File.separator + file);
                diff.getDelListRelativePaths().add(file);
            }
        }

        // time
        diff.setCurrentTime(ZonedDateTime.now().toString());

        // kits
        diff.setNewKitList(new LinkedList<>(this.getKits()));
        diff.setLastKitList(new LinkedList<>(oldSummary.getKits()));

        // version
        diff.setCurrentToolVersion(this.getPluginVersion());
        diff.setLastToolVersion(oldSummary.getPluginVersion());

        // location
        diff.setOldXMSLocation(oldSummary.moduleLocation);
        diff.setNewXMSLocation(moduleLocation);
        return diff;
    }
}
