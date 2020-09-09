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

import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;

import java.util.ArrayList;

import javax.swing.SortOrder;

public class XmsDiffTableModel extends ListTableModel<XmsDiffItem> {
    public static final int NEW_FILE_COLUMN_INDEX = 0;

    public static final int OLD_FILE_COLUMN_INDEX = 1;

    public static final int STATUS_COLUMN_INDEX = 2;

    public static final String NEW_FILE_COLUMN_TITLE = "New File";

    public static final String OLD_FILE_COLUMN_TITLE = "Backup File";

    public static final String STATUS_COLUMN_TITLE = "Status";

    private static final ColumnInfo[] COLUMN_INFOS =
        {new XmsDiffTableColumnInfo(NEW_FILE_COLUMN_TITLE, NEW_FILE_COLUMN_INDEX),
            new XmsDiffTableColumnInfo(OLD_FILE_COLUMN_TITLE, OLD_FILE_COLUMN_INDEX),
            new XmsDiffTableColumnInfo(STATUS_COLUMN_TITLE, STATUS_COLUMN_INDEX)};

    public XmsDiffTableModel() {
        super(COLUMN_INFOS, new ArrayList<>(), STATUS_COLUMN_INDEX, SortOrder.ASCENDING);
    }
}
