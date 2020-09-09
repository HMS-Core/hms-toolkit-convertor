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

import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;

import com.intellij.ui.JBColor;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

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

        int ROW_HEIGHT = 25;

        int JUMP_DIALOG_HEIGHT = 100;

        int JUMP_DIALOG_WIDTH = 200;

        interface Summary {
            int SPLITE_PANE_DIVISOR = 3;

            int TEXT_FIELD_WIDTH = 20;

            int TEXT_FIELD_HEIGHT = 20;
        }

        interface HmsConvertor {
            int TABBED_PANE_COUNT_LIMIT = 3;

            int TABBED_PANE_MAX_INDEX = 2;
        }

        interface SourceConvertor {
            int FILE_COMBOBOX_WIDTH = 320;

            int FILE_COMBOBOX_WEIGHTX = 20;

            int KIT_NAME_COMBOBOX_WIDTH = 130;

            int CONVERTION_TYPE_COMBOBOX_WIDTH = 130;

            int SPLIT_PANEL_WIDTH = 120;

            int SPLIT_PANEL_HEIGHT = -1;
        }
    }

    public interface JavaDocToolWindow {
        String JAVADOC_TOOL_WINDOW_ID = "HMS API Helper";
    }

    /**
     * Dialog constants
     */
    public interface Dialog {
        String TITLE_WARNING = "WARNING";

        int SUMMARY_DIALOG_TABLE_WIDTH = 300;

        int SUMMARY_DIALOG_TABLE_HEIGHT = -1;

        int ROW_HEIGHT = 25;

        String WINDOW_ICON_RESOURCE = "/icons/convertor.png";

        interface PolicySetting {
            enum BorderColorEnum {
                R(122),

                G(138),

                B(153);

                private int value;

                BorderColorEnum(int value) {
                    this.value = value;
                }

                public int getValue() {
                    return value;
                }
            }

            enum TitleColorDarculaEnum {
                R(183),

                G(185),

                B(188);

                private int value;

                TitleColorDarculaEnum(int value) {
                    this.value = value;
                }

                public int getValue() {
                    return value;
                }
            }

            enum Row1ColorDarculaEnum {
                R(71),

                G(76),

                B(83);

                private int value;

                Row1ColorDarculaEnum(int value) {
                    this.value = value;
                }

                public int getValue() {
                    return value;
                }
            }

            enum OtherRowColorDarculaEnum {
                R(71),

                G(76),

                B(83);

                private int value;

                OtherRowColorDarculaEnum(int value) {
                    this.value = value;
                }

                public int getValue() {
                    return value;
                }
            }

            enum TitleColorEnum {
                R(49),

                G(51),

                B(53);

                private int value;

                TitleColorEnum(int value) {
                    this.value = value;
                }

                public int getValue() {
                    return value;
                }
            }

            enum Row1ColorEnum {
                R(212),

                G(212),

                B(212);

                private int value;

                Row1ColorEnum(int value) {
                    this.value = value;
                }

                public int getValue() {
                    return value;
                }
            }

            enum OtherRowColorEnum {
                R(221),

                G(221),

                B(221);

                private int value;

                OtherRowColorEnum(int value) {
                    this.value = value;
                }

                public int getValue() {
                    return value;
                }
            }
        }
    }

    /**
     * Html constants
     */
    public interface Html {
        String HTML_HEAD = "<html>";

        String HTML_END = "</html>";

        String BR = "<br>";

        String SPACE = "&nbsp;";

        String GREEN_SPAN_HEAD = "<span style=\"color: #4ea934\">";

        String SPAN_END = "</span>";
    }

    public interface CommonAnalyseResult {
        PDFont FONT = PDType1Font.TIMES_ROMAN;

        float FONT_SIZE = 12f;

        /**
         * determined by KitApiAnalyseTable.COL_WIDTH_4_API and other col.
         *
         * @see KitApiAnalyseTable.COL_WIDTH_4_API
         */
        float MEDIA_BOX_WIDTH = 2740f;

        float MEDIA_BOX_MARGIN = 40f;

        float CONTENT_MARGIN = 16f;

        float ROW_HEIGHT = 24f;
    }

    public interface KitStatisticsTable {
        float COL_WIDTH_4_TOTAL = 90f;

        float COL_WIDTH_4_SDK = 175f;

        float COL_WIDTH_4_SUPPORT_RATE = 110f;

        float COL_WIDTH_4_AUTO_RATE = 70f;

        float HEADER_TABLE_COL_WIDTH_4_KIT = COL_WIDTH_4_TOTAL;

        float HEADER_TABLE_COL_WIDTH_4_TOTAL = COL_WIDTH_4_TOTAL * 2;

        float HEADER_TABLE_COL_WIDTH_4_SDK = COL_WIDTH_4_SDK * 3;

        float HEADER_TABLE_COL_WIDTH_4_SUPPORT = COL_WIDTH_4_SUPPORT_RATE * 2;

        float HEADER_TABLE_COL_WIDTH_4_AUTO = COL_WIDTH_4_AUTO_RATE * 2;

        float LINK_TABLE_CEL_WIDTH_4_KIT = 36f;

        float LINK_TABLE_CEL_HEIGHT_4_KIT = KitApiAnalyseTable.LINK_TABLE_CEL_HEIGHT_4_API;
    }

    public interface KitApiAnalyseTable {
        /**
         * use largest among all GMS/HMS api
         */
        float COL_WIDTH_4_API = 640f;

        float COL_WIDTH_4_COUNT = 80f;

        float COL_WIDTH_4_AUTO = 100f;

        float LINK_TABLE_CEL_WIDTH_4_API = 40f;

        float LINK_TABLE_CEL_HEIGHT_4_API = 8f;
    }

    public interface FeedbackLinkTable {
        float START_X = 730f;

        float COL_WIDTH_4_FEEDBACK = 80f;

        float CEL_WIDTH_4_FEEDBACK = 44f;

        float CEL_HEIGHT_4_FEEDBACK = 8f;
    }

    public interface JavaDoc {
        String PSI_CLASS = "PsiClass";

        String PSI_METHOD = "PsiMethod";

        String PSI_FIELD = "PsiField";

        String LEARN_MORE = "Learn More";

        String XMS_CLASS = "XMS Class";

        String HMS_CLASS = "HMS Class";

        String GMS_CLASS = "GMS Class";

        String XMS_METHOD = "XMS Method";

        String HMS_METHOD = "HMS Method";

        String GMS_METHOD = "GMS Method";

        String XMS_FILED = "XMS Field";

        String HMS_FILED = "HMS Field";

        String GMS_FILED = "GMS Field";

        String XMS_TYPE = "XMS %s";

        String HMS_TYPE = "HMS %s";

        String GMS_TYPE = "GMS %s";

        String CLASS_TYPE = "Class";

        String METHOD_TYPE = "Method";

        String FIELD_TYPE = "Field";

        String NOT_FIND_LABEL = HmsConvertorBundle.message("not_find_api");

        String KIT_NOT_SUPPORT = HmsConvertorBundle.message("kit_not_support");

        String HMS_NOT_SUPPORT = HmsConvertorBundle.message("hms_not_support");

        String GMS_NOT_SUPPORT = HmsConvertorBundle.message("gms_not_support");

        String XMS_NOT_SUPPORT = HmsConvertorBundle.message("xms_not_support");

        String DESCRIPTIONS_IS_EMPTY = HmsConvertorBundle.message("descriptions_is_empty");

        String HUAWEI_SANS = "HuaweiSans";

        String MICROSOFT_YAHEI = "Microsoft YaHei";

        String HUAWEI_SANS_BOLD = "HuaweiSans-Bold";

        String SEARCH_API = "Search API";

        String JAVADOC_TOOLWINDOW = "javaDocToolWindow";

        String JAVADOC_SEARCH_TOOLWINDOW = "javaDocSearchToolWindow";

        String NO_SEARCH_RESULT = "No Search Result";

        String HIGHT_LIGHT_LINK =
            "<a href= '%s' \"\" style=color:#51A2FF;text-decoration:none;word-wrap:break-word;>%s</a>";

        int LEFT_API_MINIMUMLENGTH = 10;

        int SINGLE_CHARACTER_SIZE = 8;

        int BUTTON_HEIGHT = 6;

        int LAYOUT_VERTICAL_GAP = 3;

        int LAYOUT_HORIZONTAL_GAP = 0;

        int AUTO_SIZE = -1;

        int API_CONTENT_WIDTH = 506;

        int LIST_CELL_RENDERER_HEIGHT = 32;

        int LABEL_HEIGHT = 60;

        int LABEL_FONT_SIZE = 14;

        int LIST_WIDTH = 1700;

        int LIST_HEIGHT = 32;

        float API_CONTENT_ALIGNMENT_SIZE = 0.5f;

        JBColor JPANEL_BACKGROUND_COLOR = new JBColor(new Color(240, 240, 240), new Color(51, 56, 64));

        JBColor JLIST_BACKGROUND_COLOR = new JBColor(new Color(242, 242, 242), new Color(60, 63, 65));

        JBColor CONTENT_BACKGROUND_COLOR = new JBColor(new Color(229, 229, 229), new Color(61, 66, 73));

        JBColor FOREGROUND_COLOR = new JBColor(new Color(0.13F, 0.13F, 0.13F, 1.0F), new Color(1.0F, 1.0F, 1.0F, 1.0F));

        JBColor LABEL_COLOR = new JBColor(new Color(0.13F, 0.13F, 0.13F, 1.0F), new Color(0.9F, 0.9F, 0.9F, 1.0F));

        Color LINK_COLOR = new Color(81,162,255);

        Color TEXTFIELD_FOCUS_Gained_COLOR = new Color(0.9F, 0.9F, 0.9F, 1.0F);

        Color TEXTFIELD_FOCUS_LOST_COLOR = new Color(153, 153, 153);

        Color TEXTFIELD_FOCUS_LOST_DARCULA_COLOR = new Color(113, 109, 109);
    }

    public interface JavaLabel {
        enum BackgroundDarculaColorEnum {
            R(59),

            G(63),

            B(68);

            private int value;

            BackgroundDarculaColorEnum(int value) {
                this.value = value;
            }

            public int getValue() {
                return value;
            }
        }

        enum BackgroundColorEnum {
            R(204),

            G(228),

            B(247);

            private int value;

            BackgroundColorEnum(int value) {
                this.value = value;
            }

            public int getValue() {
                return value;
            }
        }

        enum ForeGroundColorEnum {
            R(186),

            G(186),

            B(161);

            private int value;

            ForeGroundColorEnum(int value) {
                this.value = value;
            }

            public int getValue() {
                return value;
            }
        }

        enum BorderColorEnum {
            R(61),

            G(97),

            B(133),

            THICKNESS(2);

            private int value;

            BorderColorEnum(int value) {
                this.value = value;
            }

            public int getValue() {
                return value;
            }
        }
    }

    public interface Util {
        interface AnalyseResultExport {
            interface StatisticsHeaderTable {
                int KIT_COL_COUNT = 1;

                int TOTAL_COL_COUNT = 3;

                int SDK_COL_COUNT = 1;

                int SUPPORT_COL_COUNT = 3;

                int METHOD_AUTO_COL_COUNT = 2;

                int CLASS_AUTO_COL_COUNT = 2;

                int FIELD_AUTO_COL_COUNT = 2;
            }

            interface StatisticsTables {
                int ROW_COUNT_INDEX = 2;

                int TOTAL_TOTLE_COL_COUNT = 7;

                int SDK_TABLE_COL_COUNT = 3;

                int SUPPORT_TABLE_COL_COUNT = 6;

                int AUTO_TABLE_COL_COUNT = 12;
            }

            interface StatisticsLinkTables {
                int ROW_COUNT_INDEX = 2;

                int KIT_LINK_TABLE_COL_COUNT = 1;
            }

            interface AnalyseTables {
                int ROW_COUNT_INDEX = 1;

                int API_TABLE_COL_COUNT = 1;

                int COUNT_TABLE_COL_COUNT = 2;

                int SUPPORT_TABLE_COL_COUNT = 1;

                int AUTO_TABLE_COL_COUNT = 2;
            }
        }

        interface JavaDoc {
            int LABEL_LINK_STYLE_FONT = 16;

            int LABEL_STYLE_FONT = 18;

            int CONTENT_STYLE_FONT = 14;

            int API_CONTENT_STYLE_FONT = 14;

            int CONTENT_SCROLL_PANE_STYLE_WIDTH = -1;

            int CONTENT_SCROLL_PANE_STYLE_HEIGHT = 100;
        }

        interface Ui {
            int DESC_TEXT_WRAP_LENGTH = 120;

            int REPLACE_COUNT_INDEX = 3;
        }
    }
}
