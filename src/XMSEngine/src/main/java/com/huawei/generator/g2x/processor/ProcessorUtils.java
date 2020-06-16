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

package com.huawei.generator.g2x.processor;

import com.huawei.generator.g2x.po.summary.Summary;
import com.huawei.generator.g2x.processor.module.ParamKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builder for some processor method
 *
 * @since 2020-05-09
 */
public class ProcessorUtils {
    private String pluginPath;

    // null for first time; cut old xmsadapter into oldPath XXX/xmsadapter/
    private String backPath;

    // project base path
    private String targetPath;

    // null for first time; cut old xmsadapter into oldPath XXX/xmsadapter/
    private String oldPath;

    // project base path
    private String newPath;

    // sdk or app
    private boolean thirdSDK;

    // user only or not
    private boolean useOnlyG;

    private GenerateSummary summary;

    // kitName -> Add / Del
    private Map<String, String> kitMap;

    // all kits' dependencies
    private Map<String, Set<String>> allDepMap;

    // strategy
    private List<GeneratorStrategyKind> strategyKindList;

    private Map<ParamKind, String> pathMap;

    private Map<ParamKind, Summary> summaries;

    public ProcessorUtils(Builder builder) {
        this.pluginPath = builder.pluginPath;
        this.backPath = builder.backPath;
        this.targetPath = builder.targetPath;
        this.oldPath = builder.oldPath;
        this.newPath = builder.newPath;
        this.thirdSDK = builder.thirdSDK;
        this.useOnlyG = builder.useOnlyG;
        this.summary = builder.summary;
        this.kitMap = builder.kitMap;
        this.allDepMap = builder.allDepMap;
        this.strategyKindList = builder.strategyKindList;
        this.pathMap = builder.pathMap;
        this.summaries = builder.summaries;
    }

    public GenerateSummary getSummary() {
        return summary;
    }

    public boolean isThirdSDK() {
        return thirdSDK;
    }

    public boolean isUseOnlyG() {
        return useOnlyG;
    }

    public Map<ParamKind, String> getPathMap() {
        return pathMap;
    }

    public Map<ParamKind, Summary> getSummaries() {
        return summaries;
    }

    public List<GeneratorStrategyKind> getStrategyKindList() {
        return strategyKindList;
    }

    public Map<String, Set<String>> getAllDepMap() {
        return allDepMap;
    }

    public Map<String, String> getKitMap() {
        return kitMap;
    }

    public String getBackPath() {
        return backPath;
    }

    public String getNewPath() {
        return newPath;
    }

    public String getOldPath() {
        return oldPath;
    }

    public String getPluginPath() {
        return pluginPath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public static class Builder {
        private String pluginPath = "";

        private String backPath = "";

        private String targetPath = "";

        private String oldPath = "";

        private String newPath = "";

        private boolean thirdSDK = false;

        private boolean useOnlyG = false;

        private GenerateSummary summary = null;

        private Map<String, String> kitMap = new HashMap<>();

        private Map<String, Set<String>> allDepMap = new HashMap<>();

        private List<GeneratorStrategyKind> strategyKindList = new ArrayList<>();

        private Map<ParamKind, String> pathMap = new HashMap<>();

        private Map<ParamKind, Summary> summaries = new HashMap<>();

        public Builder() {
        }

        public Builder(ProcessorUtils processorUtils) {
            this.pluginPath = processorUtils.pluginPath;
            this.backPath = processorUtils.backPath;
            this.targetPath = processorUtils.targetPath;
            this.oldPath = processorUtils.oldPath;
            this.newPath = processorUtils.newPath;
            this.thirdSDK = processorUtils.thirdSDK;
            this.useOnlyG = processorUtils.useOnlyG;
            this.summary = processorUtils.summary;
            this.kitMap = processorUtils.kitMap;
            this.allDepMap = processorUtils.allDepMap;
            this.strategyKindList = processorUtils.strategyKindList;
            this.pathMap = processorUtils.pathMap;
            this.summaries = processorUtils.summaries;
        }

        public Builder setPluginPath(String pluginPath) {
            this.pluginPath = pluginPath;
            return this;
        }

        public Builder setBackPath(String backPath) {
            this.backPath = backPath;
            return this;
        }

        public Builder setTargetPath(String targetPath) {
            this.targetPath = targetPath;
            return this;
        }

        public Builder setOldPath(String oldPath) {
            this.oldPath = oldPath;
            return this;
        }

        public Builder setNewPath(String newPath) {
            this.newPath = newPath;
            return this;
        }

        public Builder setThirdSDK(boolean thirdSDK) {
            this.thirdSDK = thirdSDK;
            return this;
        }

        public Builder setUseOnlyG(boolean useOnlyG) {
            this.useOnlyG = useOnlyG;
            return this;
        }

        public Builder setSummary(GenerateSummary summary) {
            this.summary = summary;
            return this;
        }

        public Builder setKitMap(Map<String, String> kitMap) {
            this.kitMap = kitMap;
            return this;
        }

        public Builder setAllDepMap(Map<String, Set<String>> allDepMap) {
            this.allDepMap = allDepMap;
            return this;
        }

        public Builder setStrategyKindList(List<GeneratorStrategyKind> strategyKindList) {
            this.strategyKindList = strategyKindList;
            return this;
        }

        public Builder setPathMap(Map<ParamKind, String> pathMap) {
            this.pathMap = pathMap;
            return this;
        }

        public Builder setSummaries(Map<ParamKind, Summary> summaries) {
            this.summaries = summaries;
            return this;
        }

        public ProcessorUtils build() {
            return new ProcessorUtils(this);
        }
    }
}
