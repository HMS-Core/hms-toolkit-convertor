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

package com.huawei.hms.convertor.idea.ui.result.export;

import com.huawei.hms.convertor.idea.util.ArrayUtil;

import lombok.Getter;

@Getter
public class AnalyseResultTable {
    private float colWidth;

    private String[][] tables;

    private AnalyseResultHyperlinkTable hyperlinkTable;

    public AnalyseResultTable(float colWidth, String[][] tables) {
        this.colWidth = colWidth;
        this.tables = ArrayUtil.deepCopy(tables);
    }

    public AnalyseResultTable(float colWidth, String[][] tables, AnalyseResultHyperlinkTable hyperlinkTable) {
        this.colWidth = colWidth;
        this.tables = ArrayUtil.deepCopy(tables);
        this.hyperlinkTable = hyperlinkTable;
    }

    public String[][] getTables() {
        return ArrayUtil.deepCopy(tables);
    }
}
