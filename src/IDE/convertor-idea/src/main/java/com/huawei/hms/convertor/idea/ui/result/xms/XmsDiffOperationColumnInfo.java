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

public class XmsDiffOperationColumnInfo extends ColumnInfo<XmsDiffItem, Icon> {
    private static final int COLUMN_WIDTH = 80;

    public XmsDiffOperationColumnInfo(String columnTitle) {
        super(columnTitle);
    }

    @Override
    public int getWidth(JTable table) {
        return COLUMN_WIDTH;
    }

    @Nullable
    @Override
    public TableCellRenderer getRenderer(XmsDiffItem xmsDiffItem) {
        DefaultTableCellRenderer renderer = new XmsDiffOperationColumnInfo.ResultConfirmTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setIcon(IconUtil.VIEW);
        return renderer;
    }

    @Nullable
    @Override
    public Icon valueOf(XmsDiffItem xmsDiffItem) {
        return IconUtil.SELECT;
    }

    private static class ResultConfirmTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = -695308459192222222L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return this;
        }

        @Override
        public void setText(String text) {
        }
    }
}
