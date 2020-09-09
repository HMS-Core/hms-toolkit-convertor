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

import static com.huawei.generator.g2x.processor.XmsPublicUtils.XMS_PATH;

import java.io.File;
import java.io.IOException;

/**
 * Class for XmsPath
 *
 * @since 2019-04-21
 */
public class XmsPath {
    private String modulePath;

    private String xmsPath;

    private String detailPath;

    private String moduleName;

    public XmsPath(String xmsPath) throws IOException {
        File[] files = new File(xmsPath).listFiles();
        if (files != null && files.length > 0) {
            this.detailPath = files[0].getCanonicalPath();
        }

        this.modulePath = xmsPath.replace(XMS_PATH, "");
        this.modulePath = new File(this.modulePath).getParentFile().getParentFile().getCanonicalPath();
        if (this.modulePath.contains(File.separator + "xg") || this.modulePath.contains(File.separator + "xh")
            || this.modulePath.contains(File.separator + "xapi")) {
            this.modulePath = new File(this.modulePath).getParentFile().getCanonicalPath();
        }
        if (modulePath.contains("xmsadapter")) {
            this.moduleName = new File(modulePath).getName();
        } else {
            this.moduleName = "";
            if (xmsPath.contains(File.separator + "xmsg") || xmsPath.contains(File.separator + "xmsgh")) {
                this.modulePath = new File(xmsPath).getParentFile().getParentFile().getParentFile().getCanonicalPath();
            } else {
                this.modulePath = new File(xmsPath).getParentFile().getCanonicalPath();
            }
        }

        this.xmsPath = xmsPath;
    }

    public String getModulePath() {
        return modulePath;
    }

    public String getXmsPath() {
        return xmsPath;
    }

    public String getDetailPath() {
        return detailPath;
    }

    public String getModuleName() {
        return moduleName;
    }
}
