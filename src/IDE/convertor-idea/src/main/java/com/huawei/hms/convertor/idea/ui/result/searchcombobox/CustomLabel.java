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

import com.huawei.hms.convertor.idea.ui.common.UIConstants;
import com.intellij.util.ui.UIUtil;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;

/**
 * Custom label
 *
 * @since 2019-10-31
 */
public class CustomLabel extends JLabel {
    @Override
    public Color getBackground() {
        if (UIUtil.isUnderDarcula()) {
            return new Color(UIConstants.JavaLabel.BackgroundDarculaColorEnum.R.getValue(), UIConstants.JavaLabel.BackgroundDarculaColorEnum.G.getValue(), UIConstants.JavaLabel.BackgroundDarculaColorEnum.B.getValue());
        } else {
            return new Color(UIConstants.JavaLabel.BackgroundColorEnum.R.getValue(), UIConstants.JavaLabel.BackgroundColorEnum.G.getValue(), UIConstants.JavaLabel.BackgroundColorEnum.B.getValue());
        }
    }

    @Override
    public Color getForeground() {
        if (UIUtil.isUnderDarcula()) {
            return new Color(UIConstants.JavaLabel.ForeGroundColorEnum.R.getValue(), UIConstants.JavaLabel.ForeGroundColorEnum.G.getValue(), UIConstants.JavaLabel.ForeGroundColorEnum.B.getValue());
        } else {
            return super.getForeground();
        }
    }

    @Override
    public Border getBorder() {
        if (UIUtil.isUnderDarcula()) {
            return BorderFactory.createLineBorder(new Color(UIConstants.JavaLabel.BorderColorEnum.R.getValue(), UIConstants.JavaLabel.BorderColorEnum.G.getValue(), UIConstants.JavaLabel.BorderColorEnum.B.getValue()), UIConstants.JavaLabel.BorderColorEnum.THICKNESS.getValue());
        } else {
            return BorderFactory.createLineBorder(new Color(UIConstants.JavaLabel.BorderColorEnum.R.getValue(), UIConstants.JavaLabel.BorderColorEnum.G.getValue(), UIConstants.JavaLabel.BorderColorEnum.B.getValue()));
        }
    }
}
