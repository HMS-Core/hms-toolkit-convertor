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

import com.huawei.generator.g2x.processor.XmsConstants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * diff: the difference from summaryA to summaryB
 * 1. added: in A, not in B
 * 2. updatedMap：special used for xms-self update
 * 3. modMap：in A in B, not the same
 * 4. deleted: in B, not in A
 * 5. others[may be UNKNOWN]: last version, this version
 *
 * @since 2020-02-20
 */

public class Diff {
    private List<String> addList = new LinkedList<>();

    private List<String> addListWithRelativePaths = new LinkedList<>();

    private TreeMap<String, String> updatedMap = new TreeMap<>();

    private TreeMap<String, String> updatedMapRelativePaths = new TreeMap<>();

    private TreeMap<String, String> modMap = new TreeMap<>();

    private TreeMap<String, String> modMapRelativePaths = new TreeMap<>();

    private List<String> delList = new LinkedList<>();

    private List<String> delListRelativePaths = new LinkedList<>();

    private List<String> lastKitList = new LinkedList<>();

    private List<String> newKitList = new LinkedList<>();

    private String lastGeneratedTime = null;

    private String currentTime;

    private String lastToolVersion = null;

    private String currentToolVersion;

    // original xms location last time
    private String oldXMSLocation = null;

    // real xms location generated this time
    private String newXMSLocation = null;

    // User expected xms code location this time
    private String targetXMSLocation = null;

    private String depDescription = null;

    // when generate with repository, key: kit,value: dependencies
    private Map<String, String> addDependecies = new HashMap<>();

    // when generate with repository, key: kit,value: dependencies
    private Map<String, String> delDependecies = new HashMap<>();

    public Diff(String currentToolVersion) {
        this.currentToolVersion = currentToolVersion;
    }

    public boolean hasAdd() {
        return !addList.isEmpty();
    }

    public boolean hasMod() {
        return !modMap.isEmpty();
    }

    public boolean hasDel() {
        return !delList.isEmpty();
    }

    public List<String> getAddList() {
        return addList;
    }

    public void setAddList(List<String> addList) {
        this.addList = addList;
    }

    public TreeMap<String, String> getUpdatedMap() {
        return updatedMap;
    }

    public void setUpdatedMap(TreeMap<String, String> updatedMap) {
        this.updatedMap = updatedMap;
    }

    public TreeMap<String, String> getModMap() {
        return modMap;
    }

    public void setModMap(TreeMap<String, String> modMap) {
        this.modMap = modMap;
    }

    public List<String> getDelList() {
        return delList;
    }

    public void setDelList(List<String> delList) {
        this.delList = delList;
    }

    public List<String> getLastKitList() {
        return lastKitList;
    }

    public void setLastKitList(List<String> lastKitList) {
        this.lastKitList = lastKitList;
    }

    public List<String> getNewKitList() {
        return newKitList;
    }

    public void setNewKitList(List<String> newKitList) {
        this.newKitList = newKitList;
    }

    public String getLastGeneratedTime() {
        return lastGeneratedTime;
    }

    public void setLastGeneratedTime(String lastGeneratedTime) {
        this.lastGeneratedTime = lastGeneratedTime;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getLastToolVersion() {
        return lastToolVersion;
    }

    public void setLastToolVersion(String lastToolVersion) {
        this.lastToolVersion = lastToolVersion;
    }

    public String getCurrentToolVersion() {
        return currentToolVersion;
    }

    public void setCurrentToolVersion(String currentToolVersion) {
        this.currentToolVersion = currentToolVersion;
    }

    public String getOldXMSLocation() {
        return oldXMSLocation;
    }

    public void setOldXMSLocation(String oldXMSLocation) {
        this.oldXMSLocation = oldXMSLocation;
    }

    public String getNewXMSLocation() {
        return newXMSLocation;
    }

    public void setNewXMSLocation(String newXMSLocation) {
        this.newXMSLocation = newXMSLocation;
    }

    public String getTargetXMSLocation() {
        return targetXMSLocation;
    }

    public void setTargetXMSLocation(String targetXMSLocation) {
        this.targetXMSLocation = targetXMSLocation;
    }

    public String getDepDescription() {
        return depDescription;
    }

    public void setDepDescription(String depDescription) {
        this.depDescription = depDescription;
    }

    public boolean hasDiffContent() {
        return !(addList.isEmpty() && delList.isEmpty() && updatedMap.isEmpty() && modMap.isEmpty());
    }

    // updateList should not show to user
    public boolean isChanged() {
        return !(addList.isEmpty() && delList.isEmpty() && modMap.isEmpty());
    }

    public List<String> getAddListWithRelativePaths() {
        return addListWithRelativePaths;
    }

    public void setAddListWithRelativePaths(List<String> addListWithRelativePaths) {
        this.addListWithRelativePaths = addListWithRelativePaths;
    }

    public TreeMap<String, String> getUpdatedMapRelativePaths() {
        return updatedMapRelativePaths;
    }

    public void setUpdatedMapRelativePaths(TreeMap<String, String> updatedMapRelativePaths) {
        this.updatedMapRelativePaths = updatedMapRelativePaths;
    }

    public TreeMap<String, String> getModMapRelativePaths() {
        return modMapRelativePaths;
    }

    public void setModMapRelativePaths(TreeMap<String, String> modMapRelativePaths) {
        this.modMapRelativePaths = modMapRelativePaths;
    }

    public List<String> getDelListRelativePaths() {
        return delListRelativePaths;
    }

    public void setDelListRelativePaths(List<String> delListRelativePaths) {
        this.delListRelativePaths = delListRelativePaths;
    }

    public Map<String, String> getAddDependecies() {
        return addDependecies;
    }

    public void setAddDependecies(Map<String, String> addDependecies) {
        this.addDependecies = addDependecies;
    }

    public Map<String, String> getDelDependecies() {
        return delDependecies;
    }

    public void setDelDependecies(Map<String, String> delDependecies) {
        this.delDependecies = delDependecies;
    }

    public String display() {
        StringBuilder builder = new StringBuilder();
        createFileHeader(builder);
        createOverView(builder);
        if (!(modMap.isEmpty() && delList.isEmpty())) {
            createRepoDescription(builder);
        }
        createNotice(builder);
        createDetails(builder);
        return builder.toString();
    }

    private void createFileHeader(StringBuilder builder) {
        builder.append("# We list the diff info against last generated convertor code as follows.")
            .append(System.lineSeparator())
            .append("# Please check the Dependency Section to add "
                + "the relative dependencies into \"build.gradle\" and Diff Details to solve the conflicts.")
            .append(System.lineSeparator())
            .append(System.lineSeparator());
    }

    private void createOverView(StringBuilder builder) {
        StringBuilder lastKitListStringBuilder = new StringBuilder();
        StringBuilder newKitListStringBuilder = new StringBuilder();
        if (lastKitList == null || lastKitList.size() == 0) {
            lastKitListStringBuilder.append(XmsConstants.NULL_SIGN);
        } else {
            for (int i = 0; i < lastKitList.size(); i++) {
                if (i == lastKitList.size() - 1) {
                    lastKitListStringBuilder.append(lastKitList.get(i));
                } else {
                    lastKitListStringBuilder.append(lastKitList.get(i)).append(", ");
                }
            }
        }
        if (newKitList == null || newKitList.size() == 0) {
            newKitListStringBuilder.append(XmsConstants.NULL_SIGN);
        } else {
            for (int i = 0; i < newKitList.size(); i++) {
                if (i == newKitList.size() - 1) {
                    newKitListStringBuilder.append(newKitList.get(i));
                } else {
                    newKitListStringBuilder.append(newKitList.get(i)).append(", ");
                }
            }
        }
        String toolVersion = (this.currentToolVersion == null || "".equals(this.currentToolVersion))
            ? XmsConstants.NULL_SIGN : this.currentToolVersion;
        String oldLocation = (this.oldXMSLocation == null || "".equals(this.oldXMSLocation)) ? XmsConstants.NULL_SIGN
            : this.oldXMSLocation;
        String newLocation = (this.newXMSLocation == null || "".equals(this.newXMSLocation)) ? XmsConstants.NULL_SIGN
            : this.newXMSLocation;
        String targetLocation = (this.targetXMSLocation == null || "".equals(this.targetXMSLocation))
            ? XmsConstants.NULL_SIGN : this.targetXMSLocation;
        builder.append("====")
            .append(System.lineSeparator())
            .append("Overview:")
            .append(System.lineSeparator())
            .append("1. current tool version: ")
            .append(toolVersion)
            .append(System.lineSeparator())
            .append("2. old kit usages: ")
            .append(lastKitListStringBuilder)
            .append(System.lineSeparator())
            .append("3. new kit usages: ")
            .append(newKitListStringBuilder)
            .append(System.lineSeparator())
            .append("4. old generated code location: ")
            .append(oldLocation)
            .append(System.lineSeparator())
            .append("5. new generated code location: ")
            .append(newLocation)
            .append(System.lineSeparator())
            .append("6. target location: ")
            .append(targetLocation)
            .append(System.lineSeparator())
            .append("7. Diff summary")
            .append(System.lineSeparator())
            .append("Add Files: ")
            .append(this.addList.size())
            .append(System.lineSeparator())
            .append("Mod Files: ")
            .append(this.modMap.size())
            .append(System.lineSeparator())
            .append("Update Files: ")
            .append(this.updatedMap.size())
            .append(System.lineSeparator())
            .append("Del Files: ")
            .append(this.delList.size())
            .append(System.lineSeparator())
            .append(System.lineSeparator());
    }

    private void createRepoDescription(StringBuilder builder) {
        if (depDescription != null && !depDescription.isEmpty()) {
            builder.append("====")
                .append(System.lineSeparator())
                .append("Dependency:")
                .append(System.lineSeparator())
                .append(depDescription)
                .append(System.lineSeparator());
        }
    }

    private void createNotice(StringBuilder builder) {
        builder.append("====")
            .append(System.lineSeparator())
            .append("Diff Details:")
            .append(System.lineSeparator())
            .append("Notice, we detect modifications of our generated code in this conversion.")
            .append(System.lineSeparator())
            .append("Basically, our generated code should not be modified.")
            .append(System.lineSeparator())
            .append(
                "In order to avoid mis-delete, we generate the new code in a temp folder " + "when conflicts detected.")
            .append(System.lineSeparator())
            .append("Please, manually merge the conflict files in [mod files] and [del] files.")
            .append(System.lineSeparator())
            .append(System.lineSeparator())
            .append("# Add files are files new generated, including the new kits user used or "
                + "new classes our plugin provided.")
            .append(System.lineSeparator())
            .append("# Mod files are files modified by user against our original version, user has to "
                + "manually merged these files.")
            .append(System.lineSeparator())
            .append("# Update files are files updated by our plugin in this new version to fix bugs or "
                + "provide new APIs.")
            .append(System.lineSeparator())
            .append("# Delete files are files missing in this new generation, which may not belongs to "
                + "our convertor code.")
            .append(System.lineSeparator())
            .append(System.lineSeparator());
    }

    private void createDetails(StringBuilder builder) {
        if (addList != null && addList.size() != 0) {
            builder.append("Add Files:").append(System.lineSeparator());
            for (String addFile : addList) {
                builder.append(addFile).append(System.lineSeparator()).append(System.lineSeparator());
            }
        }
        if (modMap != null && modMap.size() != 0) {
            builder.append("Mod Files: [oldFile -> newFile]").append(System.lineSeparator());
            for (Map.Entry<String, String> entry : modMap.entrySet()) {
                builder.append("[").append(entry.getValue()).append(" ->").append(System.lineSeparator());
                builder.append(entry.getKey())
                    .append("]")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
            }
        }
        if (updatedMap != null && updatedMap.size() != 0) {
            builder.append("Update Files:[oldFile -> newFile]").append(System.lineSeparator());
            for (Map.Entry<String, String> entry : updatedMap.entrySet()) {
                builder.append("[").append(entry.getValue()).append(" ->").append(System.lineSeparator());
                builder.append(entry.getKey())
                    .append("]")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
            }
        }
        if (delList != null && delList.size() != 0) {
            builder.append("Delete Files:").append(System.lineSeparator());
            for (String delFile : delList) {
                builder.append(delFile).append(System.lineSeparator());
            }
        }
    }
}