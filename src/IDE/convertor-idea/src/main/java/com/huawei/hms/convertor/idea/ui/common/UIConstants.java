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

package com.huawei.hms.convertor.idea.ui.common;

import com.intellij.ui.JBColor;

import java.awt.Color;

/**
 * UI constants
 *
 * @since 2020-04-03
 */
public final class UIConstants {
    /**
     * Dummy color
     */
    public static final JBColor DUMMY_COLOR = new JBColor(new Color(123, 132, 204), new Color(123, 132, 204));

    /**
     * Menu action constants
     */
    public interface Action {
        String CONVERTOR_ACTION_ID = "HMSConvertor";
    }

    /**
     * ToolWindow constants
     */
    public interface ToolWindow {
        String TOOL_WINDOW_ID = "HMS Convertor";

        int TAB_SUMMARY_INDEX = 0;

        int TAB_CONVERSION_INDEX = 1;

        int TAB_XMSDIFF_INDEX = 2;
    }

    /**
     * Dialog constants
     */
    public interface Dialog {
        String TITLE_WARNING = "WARNING";
    }

    /**
     * Html constants
     */
    public interface Html {
        String HTML_HEAD = "<html>";

        String HTML_END = "</html>";

        String BR = "<br>";

        String SPACE = "&nbsp;";
    }
}
