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

import com.huawei.hms.convertor.core.result.conversion.ConvertType;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.UIConstants;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.ColumnInfo;

import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.Cursor;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Result column info
 *
 * @since 2019-06-12
 */
public class ResultColumnInfo extends ColumnInfo<DefectItem, String> {
    private int columnIndex;

    private int[] columnWidth = {60, -1, 80, 110, 110, -1, -1, 80};

    public ResultColumnInfo(String columnTitle, int columnIndex) {
        super(columnTitle);
        this.columnIndex = columnIndex;
    }

    @Nullable
    @Override
    public String valueOf(DefectItem defectItem) {
        return getValue(defectItem, columnIndex);
    }

    @Override
    public int getWidth(JTable table) {
        return columnWidth[columnIndex];
    }

    @Nullable
    @Override
    public Comparator<DefectItem> getComparator() {
        return new DefectComparator(columnIndex);
    }

    @Nullable
    @Override
    public TableCellRenderer getRenderer(DefectItem defectItem) {
        DefaultTableCellRenderer renderer = new ResultTableCellRenderer();
        if (Objects.equals(defectItem.getConvertType(), ConvertType.MANUAL)) {
            if (defectItem.isConverted()) {
                renderer.setForeground(JBColor.GRAY);
            } else {
                renderer.setForeground(JBColor.RED);
            }
        } else if (Objects.equals(defectItem.getConvertType(), ConvertType.DUMMY)) {
            if (defectItem.isConverted()) {
                renderer.setForeground(JBColor.GRAY);
            } else {
                renderer.setForeground(UIConstants.DUMMY_COLOR);
            }
        } else {
            if (defectItem.isConverted()) {
                renderer.setForeground(JBColor.GRAY);
            } else {
                renderer.setForeground(JBColor.BLACK);
            }
            renderer.setToolTipText(HmsConvertorBundle.message("click_notice"));
        }

        return renderer;
    }

    /**
     * Get column value
     *
     * @param defectItem the table row item
     * @param columnIndex the table column index
     * @return string type value, the table cell value
     */
    private static String getValue(DefectItem defectItem, int columnIndex) {
        switch (columnIndex) {
            case ResultTableModel.LINE_COLUMN_INDEX:
                return String.valueOf(defectItem.getDefectStartLine()).replace("-", "") + "-"
                    + String.valueOf(defectItem.getDefectEndLine()).replace("-", "");

            case ResultTableModel.FILE_COLUMN_INDEX:
                return defectItem.getFile();

            case ResultTableModel.KIT_NAME_COLUMN_INDEX:
                if (defectItem.getKitName().startsWith("[")) {
                    return defectItem.getKitName().substring(1, defectItem.getKitName().length() - 1);
                } else {
                    return defectItem.getKitName();
                }

            case ResultTableModel.CONVERT_TYPE_COLUMN_INDEX:
                return defectItem.getConvertType();

            case ResultTableModel.DEFECT_CONTENT_COLUMN_INDEX:
                return defectItem.getDefectContent().trim();

            case ResultTableModel.DESCRIPTION_COLUMN_INDEX:
                return defectItem.getMergedDescription();

            case ResultTableModel.REFERENCE_COLUMN_INDEX:
                return defectItem.getDetail();

            default:
                return "";
        }
    }

    private static class DefectComparator implements Comparator<DefectItem>, Serializable {
        private static final long serialVersionUID = -6953084591915057226L;

        private int columnIndex;

        public DefectComparator(int columnIndex) {
            this.columnIndex = columnIndex;
        }

        @Override
        public int compare(DefectItem o1, DefectItem o2) {
            switch (columnIndex) {
                case ResultTableModel.FILE_COLUMN_INDEX:
                    return o1.getFile().compareToIgnoreCase(o2.getFile());

                case ResultTableModel.LINE_COLUMN_INDEX:
                    return Math.abs(o1.getDefectStartLine()) - Math.abs(o2.getDefectStartLine());

                case ResultTableModel.KIT_NAME_COLUMN_INDEX:
                    return o1.getKitName().compareTo(o2.getKitName());

                case ResultTableModel.CONVERT_TYPE_COLUMN_INDEX:
                    return o1.getConvertType().compareTo(o2.getConvertType());

                default:
                    return 0;
            }
        }
    }

    private static class ResultTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = -6953084591915057024L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
            // Restore default status
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if ((ResultTableModel.FILE_COLUMN_INDEX == column)
                || (ResultTableModel.DEFECT_CONTENT_COLUMN_INDEX == column)
                || (ResultTableModel.DESCRIPTION_COLUMN_INDEX == column)) {
                setHorizontalAlignment(SwingConstants.LEFT);
            } else {
                setHorizontalAlignment(SwingConstants.CENTER);
            }

            if (ResultTableModel.NUM_COLUMN_INDEX == column) {
                setText(String.valueOf(row + 1));
            }
            return this;
        }
    }
}
