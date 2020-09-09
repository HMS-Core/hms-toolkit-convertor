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

package com.huawei.hms.convertor.idea.i18n;

import com.intellij.CommonBundle;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * I18N
 *
 * @since 2019-06-14
 */
public final class HmsConvertorBundle {
    @NonNls
    private static final String BUNDLE_NAME = "messages.HmsConvertorBundle";

    private static Reference<ResourceBundle> codeStyleCheckBundle;

    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key) {
        return CommonBundle.message(getBundle(), key);
    }

    public static String message4Param(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key,
        @NotNull String params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(codeStyleCheckBundle);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME);
            codeStyleCheckBundle = new SoftReference<>(bundle);
        }

        return bundle;
    }
}
