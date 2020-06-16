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

package com.huawei.codebot.analyzer.x2y.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * An enum of XML label we can process.
 *
 * @since 2020-04-22
 */
public enum LabelType {
    NONE("none"),

    MANIFEST("manifest"),

    USES_PERMISSION("uses-permission"),

    APPLICATION("application"),

    PERMISSION("permission"),

    METADATA("meta-data"),

    SERVICE("service"),

    ACTIVITY("activity"),

    PROVIDER("provider"),

    RECEIVER("receiver"),

    ACTION("action"),

    CATEGORY("category");

    private String value;

    LabelType(String value) {
        this.value = value;
    }

    /**
     * Get a {@link LabelType} by given string value.
     *
     * @param text A string value this enum contains.
     * @return A {@link LabelType} that this value corresponding,
     * if there's no correspond enum element, return {@code null}.
     */
    public static LabelType fromValue(String text) {
        for (LabelType b : LabelType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Get a list contains all enum element.
     *
     * @return A list of {@link LabelType}
     */
    public static List<LabelType> getProcessLabels() {
        List<LabelType> labelTypes = new ArrayList<>();
        labelTypes.add(USES_PERMISSION);
        labelTypes.add(PERMISSION);
        labelTypes.add(METADATA);
        labelTypes.add(SERVICE);
        labelTypes.add(ACTIVITY);
        labelTypes.add(PROVIDER);
        labelTypes.add(RECEIVER);
        labelTypes.add(ACTION);
        labelTypes.add(CATEGORY);
        return labelTypes;
    }

    /**
     * Get a list contains {@link #SERVICE}, {@link #ACTIVITY}, {@link #PROVIDER} and {@link #RECEIVER}.
     * <br/>
     * We name this group of labels as <b>BranchLabels</b>.
     *
     * @return BranchLabels
     */
    public static List<LabelType> getBranchLabels() {
        List<LabelType> labelTypes = new ArrayList<>();
        labelTypes.add(SERVICE);
        labelTypes.add(ACTIVITY);
        labelTypes.add(PROVIDER);
        labelTypes.add(RECEIVER);
        return labelTypes;
    }

    /**
     * Get a list contains {@link #USES_PERMISSION}, {@link #PERMISSION} and {@link #METADATA}.
     * <br/>
     * We name this group of labels as <b>LeafLabels</b>.
     *
     * @return LeafLabels
     */
    public static List<LabelType> getLeafLabels() {
        List<LabelType> labelTypes = new ArrayList<>();
        labelTypes.add(USES_PERMISSION);
        labelTypes.add(PERMISSION);
        labelTypes.add(METADATA);
        return labelTypes;
    }

    /**
     * Get a list contains {@link #MANIFEST} and {@link #APPLICATION}.
     * <br/>
     * We name this group of labels as <b>PositionLabels</b>.
     *
     * @return PositionLabels
     */
    public static List<LabelType> getPositionLabels() {
        List<LabelType> labelTypes = new ArrayList<>();
        labelTypes.add(MANIFEST);
        labelTypes.add(APPLICATION);
        return labelTypes;
    }
}
