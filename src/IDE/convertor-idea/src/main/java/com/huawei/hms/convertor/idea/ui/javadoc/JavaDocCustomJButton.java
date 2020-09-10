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

package com.huawei.hms.convertor.idea.ui.javadoc;

import com.huawei.hms.convertor.idea.ui.common.UIConstants;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class JavaDocCustomJButton extends JButton {
    @Override
    public Dimension getPreferredSize() {
        Dimension dimension = super.getPreferredSize();
        dimension.height -= UIConstants.JavaDoc.BUTTON_HEIGHT;
        dimension.width = dimension.height;
        return dimension;
    }

    @Override
    protected void paintBorder(Graphics g) {
    }

    @Override
    public void setBackground(Color bg) {
    }

    @Override
    public void setOpaque(boolean isOpaque) {
    }

}
