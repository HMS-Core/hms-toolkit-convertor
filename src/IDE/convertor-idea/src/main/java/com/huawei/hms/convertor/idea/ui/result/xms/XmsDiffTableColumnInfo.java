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

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Comparator;

import javax.swing.JTable;

public class XmsDiffTableColumnInfo extends ColumnInfo<XmsDiffItem, String> {
    private int columnIndex;

    private int[] columnWidth = {-1, -1, 80};

    public XmsDiffTableColumnInfo(String columnTitle, int columnIndex) {
        super(columnTitle);
        this.columnIndex = columnIndex;
    }

    @Nullable
    @Override
    public String valueOf(XmsDiffItem xmsDiffItem) {
        return getValue(xmsDiffItem, columnIndex);
    }

    @Override
    public int getWidth(JTable table) {
        return columnWidth[columnIndex];
    }

    @Nullable
    @Override
    public Comparator<XmsDiffItem> getComparator() {
        return new XmsDiffComparator(columnIndex);
    }

    /**
     * Get column value
     *
     * @param xmsDiffItem a xmsDiffItem item in a row
     * @param columnIndex the column index
     * @return the column value
     */
    private String getValue(XmsDiffItem xmsDiffItem, int columnIndex) {
        switch (columnIndex) {
            case XmsDiffTableModel.NEW_FILE_COLUMN_INDEX:
                return xmsDiffItem.getShowNewFileName();

            case XmsDiffTableModel.OLD_FILE_COLUMN_INDEX:
                return xmsDiffItem.getShowOldFileName();

            case XmsDiffTableModel.STATUS_COLUMN_INDEX:
                return xmsDiffItem.getStatus().getStatusStr();

            default:
                return "";
        }
    }

    private static class XmsDiffComparator implements Comparator<XmsDiffItem>, Serializable {
        private static final long serialVersionUID = -7954084591915058888L;

        private int columnIndex;

        public XmsDiffComparator(int columnIndex) {
            this.columnIndex = columnIndex;
        }

        @Override
        public int compare(XmsDiffItem o1, XmsDiffItem o2) {
            switch (columnIndex) {
                case XmsDiffTableModel.NEW_FILE_COLUMN_INDEX:
                    return o1.getNewFileName().compareTo(o2.getNewFileName());

                case XmsDiffTableModel.OLD_FILE_COLUMN_INDEX:
                    return o1.getOldFileName().compareTo(o2.getOldFileName());

                case XmsDiffTableModel.STATUS_COLUMN_INDEX:
                default:
                    return o1.getStatus().getIndex() - o2.getStatus().getIndex();
            }
        }
    }
}
