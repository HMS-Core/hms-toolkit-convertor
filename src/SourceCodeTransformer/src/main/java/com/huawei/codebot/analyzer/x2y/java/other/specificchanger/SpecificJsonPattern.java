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

package com.huawei.codebot.analyzer.x2y.java.other.specificchanger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A bean used to store changer patterns of specific changer.
 * <br/>
 * It contains different kind of change about specific changer.
 *
 * @since 2020-04-20
 */
public class SpecificJsonPattern {
    private List<ReplaceData> replaceBuilderPatterns = new ArrayList<ReplaceData>();
    private Map<String, String> deleteUrlPatterns = new HashMap<String, String>();
    private Map<String, String> deleteFilePatterns = new HashMap<String, String>();
    private List<ReplaceData> replaceScopePatterns = new ArrayList<ReplaceData>();
    private List<ReplaceData> deleteScopePatterns = new ArrayList<ReplaceData>();

    public SpecificJsonPattern setReplaceBuilderPatterns(List<ReplaceData> replaceBuilderPatterns) {
        this.replaceBuilderPatterns = replaceBuilderPatterns;
        return this;
    }

    public SpecificJsonPattern setDeleteUrlPatterns(Map<String, String> deleteUrlPatterns) {
        this.deleteUrlPatterns = deleteUrlPatterns;
        return this;
    }

    public SpecificJsonPattern setDeleteFilePatterns(Map<String, String> deleteFilePatterns) {
        this.deleteFilePatterns = deleteFilePatterns;
        return this;
    }

    public SpecificJsonPattern setReplaceScopePatterns(List<ReplaceData> replaceScopePatterns) {
        this.replaceScopePatterns = replaceScopePatterns;
        return this;
    }

    public SpecificJsonPattern setDeleteScopePatterns(List<ReplaceData> deleteScopePatterns) {
        this.deleteScopePatterns = deleteScopePatterns;
        return this;
    }

    public List<ReplaceData> getReplaceBuilderPatterns() {
        return replaceBuilderPatterns;
    }

    public Map<String, String> getDeleteUrlPatterns() {
        return deleteUrlPatterns;
    }

    public Map<String, String> getDeleteFilePatterns() {
        return deleteFilePatterns;
    }

    public List<ReplaceData> getReplaceScopePatterns() {
        return replaceScopePatterns;
    }

    public List<ReplaceData> getDeleteScopePatterns() {
        return deleteScopePatterns;
    }
}
