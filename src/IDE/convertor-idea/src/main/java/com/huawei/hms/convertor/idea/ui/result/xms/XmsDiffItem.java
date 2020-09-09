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

package com.huawei.hms.convertor.idea.ui.result.xms;

import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.alibaba.fastjson.JSON;

import lombok.Getter;

@Getter
public final class XmsDiffItem {
    private String newFileName;

    private String oldFileName;

    private Status status;

    private String xmsTempPath;

    private String originPath;

    public XmsDiffItem(String newName, String oldName, Status status, String xmsTempPathStr, String originPathStr) {
        xmsTempPath = FileUtil.unifyToUnixFileSeparator(xmsTempPathStr);
        originPath = FileUtil.unifyToUnixFileSeparator(originPathStr);
        if (newName != null) {
            newFileName = FileUtil.unifyToUnixFileSeparator(newName);
            if (newFileName.contains(xmsTempPath)) {
                newFileName = newFileName.substring(xmsTempPath.length());
            }
        }
        if (oldName != null) {
            oldFileName = FileUtil.unifyToUnixFileSeparator(oldName);
            if (oldFileName.contains(originPath)) {
                oldFileName = oldFileName.substring(originPath.length());
            }
        }
        this.status = status;
    }

    public String getShowNewFileName() {
        return newFileName;
    }

    public String getNewFileName() {
        if (xmsTempPath != null && newFileName != null && !newFileName.equals(Constant.NA)) {
            return xmsTempPath + newFileName;
        }
        return newFileName;
    }

    public String getShowOldFileName() {
        return oldFileName;
    }

    public String getOldFileName() {
        if (originPath != null && oldFileName != null && !oldFileName.equals(Constant.NA)) {
            return originPath + oldFileName;
        }
        return oldFileName;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    @Getter
    public enum Status {
        MODIFIED("Modified", 0),
        DELETED("Deleted", 1),
        UPDATED("Updated", 2),
        ADD("Add", 3),
        NA(Constant.NA, 4);

        private String statusStr;

        private int index;

        Status(String statusStr, int index) {
            this.statusStr = statusStr;
            this.index = index;
        }
    }
}
