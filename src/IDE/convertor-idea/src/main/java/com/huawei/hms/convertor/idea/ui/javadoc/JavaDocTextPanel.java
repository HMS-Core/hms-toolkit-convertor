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
import com.huawei.hms.convertor.idea.util.JavaDocUtil;

import java.awt.Graphics;

import javax.swing.JTextPane;

import java.util.Locale;

public class JavaDocTextPanel extends JTextPane {

    private String backToApi = "";

    private boolean status = false;

    public JavaDocTextPanel() {
        super();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int windowDisplayCharactersNumber = 0;
        int totalLength = 0;

        windowDisplayCharactersNumber = (getParent().getWidth() - UIConstants.JavaDoc.LEFT_API_MINIMUMLENGTH) / UIConstants.JavaDoc.SINGLE_CHARACTER_SIZE;
        if (status) {
            if (backToApi == null) {
                return;
            }
            totalLength = backToApi.length() + UIConstants.JavaDoc.LEFT_API_MINIMUMLENGTH;
            if (windowDisplayCharactersNumber > UIConstants.JavaDoc.LEFT_API_MINIMUMLENGTH && windowDisplayCharactersNumber< totalLength){
                int apiShowStringLength = windowDisplayCharactersNumber -UIConstants.JavaDoc.LEFT_API_MINIMUMLENGTH;
                int apiStartShow =backToApi.length() - apiShowStringLength;
                String rightApiDisplay = backToApi.substring(apiStartShow, backToApi.length());
                String apiString = String.format(Locale.ROOT, JavaDocUtil.setBackApiFormat(), backToApi,rightApiDisplay);
                setText(apiString);
            }else if (windowDisplayCharactersNumber >= totalLength){
                String apiString = String.format(Locale.ROOT, JavaDocUtil.setAllBackApiFormat(), backToApi,backToApi);
                setText(apiString);
            }
        }
    }

    public void setTextApi(String backToApi, boolean status) {
        this.backToApi = backToApi;
        this.status = status;
    }
}
