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

import com.huawei.hms.convertor.idea.util.ClientUtil;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Notification utility class
 *
 * @since 2018-03-25
 */
public final class BalloonNotifications {
    private static final String PLUGIN_NAME = ClientUtil.getPluginName().get();

    private static final String TITLE = PLUGIN_NAME;

    private static final String DISPLAY_ID = PLUGIN_NAME + " Balloon Notification";

    private static final NotificationGroup BALLOON_GROUP =
        new NotificationGroup(DISPLAY_ID, NotificationDisplayType.BALLOON, true);

    private static final String STICKY_BALLOON_DISPLAY_ID = PLUGIN_NAME + " Sticky Balloon Notification";

    private static final NotificationGroup STICKY_BALLOON_GROUP =
        new NotificationGroup(STICKY_BALLOON_DISPLAY_ID, NotificationDisplayType.STICKY_BALLOON, true);

    private BalloonNotifications() {
    }

    /**
     * Show success notification
     *
     * @param message notification message
     * @param project current project
     * @param title notification title
     * @param sticky true or false
     */
    public static void showSuccessNotification(@NotNull String message, Project project, @Nullable String title,
        boolean sticky) {
        final String newTitle = StringUtil.isEmptyOrSpaces(title) ? TITLE : title;
        NotificationGroup group = sticky ? STICKY_BALLOON_GROUP : BALLOON_GROUP;
        group.createNotification(newTitle, message, NotificationType.INFORMATION, null).notify(project);
    }

    /**
     * Show warning notification
     *
     * @param message notification message
     * @param project current project
     * @param title notification title
     * @param sticky true or false
     */
    public static void showWarnNotification(@NotNull String message, Project project, @Nullable String title,
        boolean sticky) {
        final String newTitle = StringUtil.isEmptyOrSpaces(title) ? TITLE : title;
        NotificationGroup group = sticky ? STICKY_BALLOON_GROUP : BALLOON_GROUP;
        group.createNotification(newTitle, message, NotificationType.WARNING, null).notify(project);
    }

    /**
     * Show success notification
     *
     * @param message notification message
     * @param project current project
     * @param listener notification listener
     * @param title notification title
     * @param sticky true or false
     */
    public static void showWarnNotification(@NotNull String message, Project project, NotificationListener listener,
        @Nullable String title, boolean sticky) {
        final String newTitle = StringUtil.isEmptyOrSpaces(title) ? TITLE : title;
        NotificationGroup group = sticky ? STICKY_BALLOON_GROUP : BALLOON_GROUP;
        group.createNotification(newTitle, message, NotificationType.WARNING, listener).notify(project);
    }

    /**
     * Show error notification
     *
     * @param message notification message
     * @param project current project
     * @param title notification title
     * @param sticky true or false
     */
    public static void showErrorNotification(@NotNull String message, Project project, @Nullable String title,
        boolean sticky) {
        final String newTitle = StringUtil.isEmptyOrSpaces(title) ? TITLE : title;
        NotificationGroup group = sticky ? STICKY_BALLOON_GROUP : BALLOON_GROUP;
        group.createNotification(newTitle, message, NotificationType.ERROR, null).notify(project);
    }

    /**
     * Show success notification
     *
     * @param message notification message
     * @param project current project
     * @param listener notification listener
     * @param title notification title
     * @param sticky true or false
     */
    public static void showSuccessNotification(@NotNull String message, Project project, NotificationListener listener,
        @Nullable String title, boolean sticky) {
        final String newTitle = StringUtil.isEmptyOrSpaces(title) ? TITLE : title;
        NotificationGroup group = sticky ? STICKY_BALLOON_GROUP : BALLOON_GROUP;
        group.createNotification(newTitle, message, NotificationType.INFORMATION, listener).notify(project);
    }

    /**
     * Show success notification
     *
     * @param message notification message
     * @param project current project
     * @param listener notification listener
     * @param title notification title
     * @param sticky true or false
     */
    public static void showErrorNotification(@NotNull String message, Project project, NotificationListener listener,
        @Nullable String title, boolean sticky) {
        final String newTitle = StringUtil.isEmptyOrSpaces(title) ? TITLE : title;
        NotificationGroup group = sticky ? STICKY_BALLOON_GROUP : BALLOON_GROUP;
        group.createNotification(newTitle, message, NotificationType.ERROR, listener).notify(project);
    }
}
