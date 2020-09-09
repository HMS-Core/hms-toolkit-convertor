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

package com.huawei.hms.convertor.core.engine.fixbot.util;

/**
 * Fixbot engine constants
 *
 * @since 2020-04-02
 */
public interface FixbotConstants {
    String FIXBOT_DIR = "fixbot";

    String CONVERSION_EXTRA_PATH_SEPARATOR = "#";

    String FIXBOT_VMOPTIONS_FILENAME = "/fixbot.vmoptions";

    String CUSTOM_VMOPTIONS_FILENAME = "convertor.vmoptions";

    String DEFAULT_MAX_HEAP_MEMORY_SIZE = "-Xmx1024m";
}
