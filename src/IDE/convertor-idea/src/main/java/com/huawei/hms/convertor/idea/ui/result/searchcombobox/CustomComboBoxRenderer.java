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

package com.huawei.hms.convertor.idea.ui.result.searchcombobox;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * Custom comboBox renderer
 *
 * @since 2019-08-06
 */
public class CustomComboBoxRenderer extends DefaultListCellRenderer {
    private JLabel searchLabel;

    public CustomComboBoxRenderer(JLabel filterLabel) {
        this.searchLabel = filterLabel;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        String text = null;
        if (value instanceof String) {
            text = (String) value;
        }
        if (null == text) {
            return this;
        }

        text = HtmlHighlighter.highlightText(text, searchLabel.getText());
        this.setText(text);
        return this;
    }

}
