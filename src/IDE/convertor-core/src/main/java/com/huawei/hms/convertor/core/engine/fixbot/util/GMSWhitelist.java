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

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * GMS Kits whitelist of TO HMS API policy
 *
 * @since 2020-04-01
 */
@Getter
@AllArgsConstructor
public enum GMSWhitelist {
    ACCOUNT("Account"),
    PUSH("Push"),
    ADS("Ads"),
    ANALYTICS("Analytics"),
    LOCATION("Location"),
    SITE("Site"),
    MAP("Map"),
    GAME("Game"),
    DRIVE("Drive"),
    WALLET("Wallet"),
    HEALTH("Health"),
    ML("ML"),
    AWARENESS("Awareness"),
    NEARBY("Nearby"),
    SAFETYNET("Safetynet"),
    DTM("DTM"),
    IDENTITY("Identity"),
    PANORAMA("Panorama"),
    BASEMENT("Basement"),
    COMMON("Common"),
    FIREBASE("Firebase"),
    IAP("IAP"),
    FIDO("FIDO"),
    OTHER("other");

    private final String name;
}
