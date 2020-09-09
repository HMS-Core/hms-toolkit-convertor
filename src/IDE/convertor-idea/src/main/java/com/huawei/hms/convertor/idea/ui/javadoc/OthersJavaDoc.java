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

import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.CLASS_TYPE;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.FIELD_TYPE;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.GMS_TYPE;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.HMS_TYPE;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.METHOD_TYPE;
import static com.huawei.hms.convertor.idea.ui.common.UIConstants.JavaDoc.XMS_TYPE;

import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.util.JavaDocUtil;

import java.util.Locale;

/**
 * java doc object when api belong to others
 *
 * @since 2020-08-06
 */
public class OthersJavaDoc extends JavaDocService {

    public OthersJavaDoc(JavaDocPanelInfos javaDocPanelInfos, JavaDocToolWindow javaDocToolWindow) {
        super(javaDocPanelInfos, javaDocToolWindow);
    }

    @Override
    public void handleWhenClass() {
        handleWhenType(CLASS_TYPE);
    }

    @Override
    public void handleWhenMethod() {
        handleWhenType(METHOD_TYPE);
    }

    @Override
    public void handleWhenField() {
        handleWhenType(FIELD_TYPE);
    }

    private void handleWhenType(String type) {
        javaDocToolWindow.getLabelUp().setText(String.format(Locale.ROOT, XMS_TYPE, type));
        javaDocToolWindow.getLabelMiddle().setText(String.format(Locale.ROOT, HMS_TYPE, type));
        javaDocToolWindow.getLabelDown().setText(String.format(Locale.ROOT, GMS_TYPE, type));
        javaDocToolWindow.getDocContentUp().setText(JavaDocUtil.setTextDisplay(HmsConvertorBundle.message("not_xms_api")));
        javaDocToolWindow.getDocContentMiddle().setText(JavaDocUtil.setTextDisplay(HmsConvertorBundle.message("not_hms_api")));
        javaDocToolWindow.getDocContentDown().setText(JavaDocUtil.setTextDisplay(HmsConvertorBundle.message("not_gms_api")));
    }
}
