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

package com.huawei.hms.convertor.idea.ui.result.summary;

import com.huawei.hms.convertor.core.engine.fixbot.model.api.FixbotApiInfo;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;

import com.intellij.util.ui.ListTableModel;

/**
 * Method table model
 *
 * @since 2019/11/28
 */
public class MethodTableModel extends ListTableModel<FixbotApiInfo> {
    private static final long serialVersionUID = 7198474218970510026L;

    public static final int METHOD_NAME_COLUMN_INDEX = 1;

    public static final int SUPPORT_INDEX = 0;

    public static final String METHOD_NAME_COLUMN_TITLE = HmsConvertorBundle.message("method_name");

    public static final String SUPPORT_TITLE = HmsConvertorBundle.message("support");

    public MethodTableModel() {
        super(new MethodColumnInfo(SUPPORT_TITLE, SUPPORT_INDEX),
            new MethodColumnInfo(METHOD_NAME_COLUMN_TITLE, METHOD_NAME_COLUMN_INDEX));
    }
}
