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
import com.huawei.hms.convertor.idea.util.IconUtil;

import com.intellij.util.ui.ColumnInfo;

import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.Cursor;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Result confirm column info
 *
 * @since 2019-06-25
 */
public class ResultConfirmColumnInfo extends ColumnInfo<DefectItem, Icon> {
    private static final int COLUMN_WIDTH = 50;

    public ResultConfirmColumnInfo(String columnTitle) {
        super(columnTitle);
    }

    @Override
    public int getWidth(JTable table) {
        return COLUMN_WIDTH;
    }

    @Nullable
    @Override
    public TableCellRenderer getRenderer(DefectItem defectItem) {
        DefaultTableCellRenderer renderer = new ResultConfirmTableCellRenderer();
        if (ConvertType.MANUAL.equals(defectItem.getConvertType())) {
            if (defectItem.isConverted()) {
                renderer.setToolTipText("Set to unconverted");
            } else {
                renderer.setToolTipText("Set to converted");
            }
        }

        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setIcon(valueOf(defectItem));
        return renderer;
    }

    @Nullable
    @Override
    public Icon valueOf(DefectItem defectItem) {
        if (ConvertType.MANUAL.equals(defectItem.getConvertType())) {
            return IconUtil.getCovertedIcon(defectItem.isConverted());
        } else {
            return IconUtil.getConfirmIcon(defectItem.isConfirmed(), defectItem.isEnable());
        }
    }

    private static class ResultConfirmTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = -6953084591915057025L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
            // Restore default status
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return this;
        }

        @Override
        public void setText(String text) {
        }
    }
}
