/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.generator.utils;

import java.util.Arrays;
import java.util.List;

/**
 * KitInfoConstants
 *
 * @since 2020-05-11
 */
public class KitInfoConstants {
    public static final String HEALTH = "health";

    public static final String ML = "ml";

    public static final List<String> BLACK_LIST_RESOURCES = Arrays.asList("/tables/account_table.json",
        "/tables/ads_table.json", "/tables/analytics_table.json", "/tables/awareness_table.json",
        "/tables/core_table.json", "/tables/drive_table.json", "/tables/drm_table.json", "/tables/dynamic_table.json",
        "/tables/game_table.json", "/tables/health_table.json", "/tables/identity_table.json",
        "/tables/location_table.json", "/tables/maps_table.json", "/tables/ml_table.json", "/tables/nearby_table.json",
        "/tables/panorama_table.json", "/tables/push_table.json", "/tables/safety_table.json",
        "/tables/site_table.json", "/tables/wallet_table.json");
}
