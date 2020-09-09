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

package com.huawei.hms.convertor.idea.ui.javadoc;

import com.huawei.inquiry.docs.EntireDoc;

/**
 * java doc base service
 *
 * @since 2020-08-07
 */
public abstract class JavaDocService {
    protected JavaDocPanelInfos javaDocPanelInfos;

    protected JavaDocToolWindow javaDocToolWindow;

    public JavaDocService(JavaDocPanelInfos javaDocPanelInfos, JavaDocToolWindow javaDocToolWindow) {
        this.javaDocPanelInfos = javaDocPanelInfos;
        this.javaDocToolWindow = javaDocToolWindow;
    }

    public void setJavaDocPanel() {
        EntireDoc.SCOPETYPE scopeType = javaDocPanelInfos.getType();
        switch (scopeType) {
            case CLASS:
                handleWhenClass();
                break;
            case METHOD:
                handleWhenMethod();
                break;
            case FIELD:
                handleWhenField();
                break;
            default:
                return;
        }
    }

    /**
     * handle when Class
     */
    public abstract void handleWhenClass();

    /**
     * handle when Method
     */
    public abstract void handleWhenMethod();

    /**
     * handle when Field
     */
    public abstract void handleWhenField();
}
