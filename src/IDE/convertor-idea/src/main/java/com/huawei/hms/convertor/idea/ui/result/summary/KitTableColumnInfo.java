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

import com.intellij.util.ui.ColumnInfo;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Comparator;

import javax.swing.JTable;

/**
 * Kit table column info
 *
 * @since 2019/11/28
 */
public class KitTableColumnInfo extends ColumnInfo<KitItem, String> {
    private int columnIndex;

    private int[] columnWidth = {60, -1, -1, -1};

    public KitTableColumnInfo(String columnTitle, int columnIndex) {
        super(columnTitle);
        this.columnIndex = columnIndex;
    }

    @Override
    public int getWidth(JTable table) {
        return columnWidth[columnIndex];
    }

    @Nullable
    @Override
    public String valueOf(KitItem kitItem) {
        return getValue(kitItem, columnIndex);
    }

    @Nullable
    @Override
    public Comparator<KitItem> getComparator() {
        return new KitComparator(columnIndex);
    }

    /**
     * Get column value
     *
     * @param kitItem a kit item in a row
     * @param columnIndex the column index
     * @return the column value
     */
    private String getValue(KitItem kitItem, int columnIndex) {
        switch (columnIndex) {
            case KitTableModel.NUM_COLUMN_INDEX:
                return String.valueOf(kitItem.getId());

            case KitTableModel.KIT_NAME_COLUMN_INDEX:
                return kitItem.getKitName();

            case KitTableModel.DEPENDENT_METHOD_INDEX:
                return kitItem.getMethodCount() == 0 ? "no" : "yes";

            case KitTableModel.METHOD_COUNT_COLUMN_INDEX:
                return String.valueOf(kitItem.getMethodCount());

            default:
                return "";
        }
    }

    private static class KitComparator implements Comparator<KitItem>, Serializable {
        private static final long serialVersionUID = -6953084591915057225L;

        private int columnIndex;

        public KitComparator(int columnIndex) {
            this.columnIndex = columnIndex;
        }

        @Override
        public int compare(KitItem o1, KitItem o2) {
            switch (columnIndex) {
                case KitTableModel.NUM_COLUMN_INDEX:
                    return o1.getId() - o2.getId();

                case KitTableModel.KIT_NAME_COLUMN_INDEX:
                    return o1.getKitName().compareTo(o2.getKitName());

                case KitTableModel.METHOD_COUNT_COLUMN_INDEX:
                    return o1.getMethodCount() - o2.getMethodCount();

                default:
                    return 0;
            }
        }
    }
}
