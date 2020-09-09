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

import com.intellij.ui.JBColor;
import com.intellij.util.ui.ColumnInfo;

import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.Cursor;
import java.io.Serializable;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Method column info
 *
 * @since 2019/11/28
 */
public class MethodColumnInfo extends ColumnInfo<FixbotApiInfo, String> {
    private int columnIndex;

    private int[] columnWidth = {130, -1};

    public MethodColumnInfo(String columnTitle, int columnIndex) {
        super(columnTitle);
        this.columnIndex = columnIndex;
    }

    @Nullable
    @Override
    public String valueOf(FixbotApiInfo fixbotMethod) {
        return getValue(fixbotMethod, columnIndex);
    }

    @Override
    public int getWidth(JTable table) {
        return columnWidth[columnIndex];
    }

    @Nullable
    @Override
    public Comparator<FixbotApiInfo> getComparator() {
        return new MethodComparator(columnIndex);
    }

    @Nullable
    @Override
    public TableCellRenderer getRenderer(FixbotApiInfo fixbotMethod) {
        DefaultTableCellRenderer renderer = new MethodTableCellRenderer();
        if (!fixbotMethod.isSupport()) {
            renderer.setForeground(JBColor.RED);
        }
        return renderer;
    }

    private String getValue(FixbotApiInfo fixbotMethod, int columnIndex) {
        switch (columnIndex) {
            case MethodTableModel.METHOD_NAME_COLUMN_INDEX:
                return fixbotMethod.getOldNameInDesc();
            case MethodTableModel.SUPPORT_INDEX:
                return String.valueOf(fixbotMethod.isSupport());
            default:
                return "";
        }
    }

    private static class MethodComparator implements Comparator<FixbotApiInfo>, Serializable {
        private static final long serialVersionUID = -6953084591915057227L;

        private int columnIndex;

        public MethodComparator(int columnIndex) {
            this.columnIndex = columnIndex;
        }

        @Override
        public int compare(FixbotApiInfo o1, FixbotApiInfo o2) {
            switch (columnIndex) {
                case MethodTableModel.SUPPORT_INDEX:
                    return Boolean.toString(o1.isSupport()).compareTo(Boolean.toString(o2.isSupport()));

                case MethodTableModel.METHOD_NAME_COLUMN_INDEX:
                    return o1.getOldNameInDesc().compareTo(o2.getOldNameInDesc());

                default:
                    return 0;
            }
        }
    }

    private static class MethodTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = -6953084591915057024L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
            // Restore default status
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (MethodTableModel.METHOD_NAME_COLUMN_INDEX == column) {
                setHorizontalAlignment(SwingConstants.LEFT);
            } else {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
            return this;
        }
    }
}
