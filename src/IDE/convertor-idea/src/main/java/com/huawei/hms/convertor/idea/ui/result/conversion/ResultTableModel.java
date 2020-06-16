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

package com.huawei.hms.convertor.idea.ui.result.conversion;

import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;

import com.intellij.util.ui.ListTableModel;

/**
 * Result record table model class.
 *
 * @since 2019-06-12
 */
public class ResultTableModel extends ListTableModel<DefectItem> {
    /**
     * Number column index
     */
    public static final int NUM_COLUMN_INDEX = 0;

    /**
     * File column index
     */
    public static final int FILE_COLUMN_INDEX = 1;

    /**
     * Line column index
     */
    public static final int LINE_COLUMN_INDEX = 2;

    /**
     * Kie name column index
     */
    public static final int KIT_NAME_COLUMN_INDEX = 3;

    /**
     * Convert type column index
     */
    public static final int CONVERT_TYPE_COLUMN_INDEX = 4;

    /**
     * Defect content column index
     */
    public static final int DEFECT_CONTENT_COLUMN_INDEX = 5;

    /**
     * Description column index
     */
    public static final int DESCRIPTION_COLUMN_INDEX = 6;

    /**
     * Confirm column index
     */
    public static final int CONFIRM_COLUMN_INDEX = 7;

    /**
     * Number column title
     */
    public static final String NUM_COLUMN_TITLE = HmsConvertorBundle.message("number");

    /**
     * File column title
     */
    public static final String FILE_COLUMN_TITLE = HmsConvertorBundle.message("file");

    /**
     * Line column title
     */
    public static final String LINE_COLUMN_TITLE = HmsConvertorBundle.message("line");

    /**
     * Kit name column title
     */
    public static final String KIT_NAME_COLUMN_TITLE = HmsConvertorBundle.message("gms_kits_name");

    /**
     * Convert type column title
     */
    public static final String CONVERT_TYPE_COLUMN_TITLE = HmsConvertorBundle.message("conversion_type");

    /**
     * Defect content column title
     */
    public static final String DEFECT_CONTENT_COLUMN_TITLE = HmsConvertorBundle.message("content");

    /**
     * Description column title
     */
    public static final String DESCRIPTION_COLUMN_TITLE = HmsConvertorBundle.message("description");

    /**
     * Confirm column title
     */
    public static final String CONFIRM_COLUMN_TITLE = "";

    public ResultTableModel() {
        super(new ResultColumnInfo(NUM_COLUMN_TITLE, NUM_COLUMN_INDEX),
            new ResultColumnInfo(FILE_COLUMN_TITLE, FILE_COLUMN_INDEX),
            new ResultColumnInfo(LINE_COLUMN_TITLE, LINE_COLUMN_INDEX),
            new ResultColumnInfo(KIT_NAME_COLUMN_TITLE, KIT_NAME_COLUMN_INDEX),
            new ResultColumnInfo(CONVERT_TYPE_COLUMN_TITLE, CONVERT_TYPE_COLUMN_INDEX),
            new ResultColumnInfo(DEFECT_CONTENT_COLUMN_TITLE, DEFECT_CONTENT_COLUMN_INDEX),
            new ResultColumnInfo(DESCRIPTION_COLUMN_TITLE, DESCRIPTION_COLUMN_INDEX),
            new ResultConfirmColumnInfo(CONFIRM_COLUMN_TITLE));
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
