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

package com.huawei.hms.convertor.idea.util;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.UIUtil;

import javax.swing.Icon;

/**
 * Icon util
 *
 * @since 2019-06-25
 */
public final class IconUtil {
    /**
     * select icon
     */
    public static final Icon SELECT = load("/icons/select.png");

    /**
     * select icon
     */
    public static final Icon SELECT_DISENABLE = load("/icons/select_disenable.png");

    /**
     * unselect icon
     */
    public static final Icon UNSELECT = load("/icons/unselect.png");

    /**
     * unselect icon
     */
    public static final Icon UNSELECT_DISENABLE = load("/icons/unselect_disenable.png");

    /**
     * manual icon
     */
    public static final Icon MANUAL = load("/icons/manual.png");

    /**
     * manual ok icon
     */
    public static final Icon MANUAL_OK = load("/icons/manualok.png");

    /**
     * empty icon
     */
    public static final Icon EMPTY = load("/icons/wisehubEmpty.png");

    /**
     * explorer icon
     */
    public static final Icon EXPLORER = load("/icons/wisehubExplorer.png");

    /**
     * down icon
     */
    public static final Icon DOWN = load("/icons/wisehubDown.png");

    /**
     * realIntentionBulb icon
     */
    public static final Icon REAL_INTENTION_BULB = load("/icons/wisehubRealIntentionBulb.png");

    /**
     * quickfixBulb icon
     */
    public static final Icon QUICK_FIX_BULB = load("/icons/wisehubQuickfixBulb.png");

    /**
     * convertor icon
     */
    public static final Icon CONVERTOR =
        UIUtil.isUnderDarcula() ? load("/icons/convertor_dark.png") : load("/icons/convertor.png");

    /**
     * inspect icon
     */
    public static final Icon INSPECT =
        UIUtil.isUnderDarcula() ? load("/icons/inspect_dark.png") : load("/icons/inspect.png");

    /**
     * open last icon
     */
    public static final Icon OPEN_LAST =
        UIUtil.isUnderDarcula() ? load("/icons/open_last_dark.png") : load("/icons/open_last.png");

    /**
     * recovery icon
     */
    public static final Icon RECOVERY =
        UIUtil.isUnderDarcula() ? load("/icons/recovery_dark.png") : load("/icons/recovery.png");

    /**
     * save all icon
     */
    public static final Icon SAVEALL =
        UIUtil.isUnderDarcula() ? load("/icons/saveall_dark.png") : load("/icons/saveall_light.png");

    /**
     * notice icon
     */
    public static final Icon NOTICE = load("/icons/notice.png");

    /**
     * HMS first icon
     */
    public static final Icon QUESTION = load("/icons/question.png");

    /**
     * HMS prompt icon
     */
    public static final Icon PROMPT_QUESTION =
        UIUtil.isUnderDarcula() ? load("/icons/question_dark.png") : load("/icons/question.png");

    /**
     * HMS point_light icon
     */
    public static final Icon POINT_ITEM_LIGHT = load("/icons/point_light.png");

    /**
     * HMS point_dark icon
     */
    public static final Icon POINT_ITEM_DARK = load("/icons/point_dark.png");

    /**
     * HMS guide line icon
     */
    public static final Icon GUIDE_LINE = load("/icons/wisehub_guide_line.png");

    /**
     * HMS guide line icon
     */
    public static final Icon GUIDE_LINE_GRAY = load("/icons/wisehub_guide_line_gray.png");

    /**
     * HMS guide line white icon
     */
    public static final Icon RUNNING = load("/icons/wisehub_running.png");

    /**
     * HMS guide line white icon
     */
    public static final Icon STEP_1_FINISH = load("/icons/wisehub_step1_finish.png");

    /**
     * HMS guide line white icon
     */
    public static final Icon WAIT = load("/icons/wisehub_wait.png");

    /**
     * WARN icon
     */
    public static final Icon WARN = load("/icons/item_warn.png");

    /**
     * view icon
     */
    public static final Icon VIEW = load("/icons/wisehub_view.png");

    public static final Icon DETAIL = load("/icons/detail.png");

    /**
     * Get confirm icon
     *
     * @param isConfirmed boolean value, isConfirmed
     * @return confirmed icon
     */
    public static Icon getConfirmIcon(boolean isConfirmed, boolean enable) {
        return isConfirmed ? (enable ? SELECT : SELECT_DISENABLE) : (enable ? UNSELECT : UNSELECT_DISENABLE);
    }

    /**
     * Get converted icon
     *
     * @param isConverted boolean value, isConverted
     * @return converted icon
     */
    public static Icon getCovertedIcon(boolean isConverted) {
        return isConverted ? MANUAL_OK : MANUAL;
    }

    /**
     * loading picture
     *
     * @param path icon path
     * @return Icon Object Instance
     */
    private static Icon load(String path) {
        return IconLoader.findIcon(path, IconUtil.class);
    }

}
