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

import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;

import com.intellij.util.ui.ListTableModel;

/**
 * Kit table model
 *
 * @since 2019/11/28
 */
public class KitTableModel extends ListTableModel<KitItem> {
    static final int NUM_COLUMN_INDEX = 0;

    static final int KIT_NAME_COLUMN_INDEX = 1;

    static final int DEPENDENT_METHOD_INDEX = 2;

    static final int METHOD_COUNT_COLUMN_INDEX = 3;

    private static final String NUM_COLUMN_TITLE = HmsConvertorBundle.message("number");

    private static final String KIT_NAME_COLUMN_TITLE = HmsConvertorBundle.message("gms_kits_name");

    private static final String DEPENDENT_METHOD_TITLE = HmsConvertorBundle.message("Dependent");

    private static final String METHOD_COUNT_COLUMN_TITLE = HmsConvertorBundle.message("called_methods");

    public KitTableModel() {
        super(new KitTableColumnInfo(NUM_COLUMN_TITLE, NUM_COLUMN_INDEX),
            new KitTableColumnInfo(KIT_NAME_COLUMN_TITLE, KIT_NAME_COLUMN_INDEX),
            new KitTableColumnInfo(DEPENDENT_METHOD_TITLE, DEPENDENT_METHOD_INDEX),
            new KitTableColumnInfo(METHOD_COUNT_COLUMN_TITLE, METHOD_COUNT_COLUMN_INDEX));
    }
}
