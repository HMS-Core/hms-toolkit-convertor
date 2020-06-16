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
 * A hard-coded util class to indicate whether we should generate G & H instances
 * for a given class. For example, if G is abstract, and contains a package-visible
 * abstract method, then we can make sure that this class will never be inherited or created
 * by app developers, so it's safe not to generate non-wrapper constructors for X.
 *
 * @since 2019-12-16
 */
public class SpecialClasses {
    private static final List<String> ONLY_FOR_WRAPPING = Arrays.asList(
            "org.xms.libraries.places.api.model.AutocompleteSessionToken",
            "org.xms.libraries.places.api.model.AutocompletePrediction.Builder",
            "org.xms.libraries.places.api.net.FindCurrentPlaceRequest.Builder",
            "org.xms.libraries.places.api.model.OpeningHours.Builder",
            "org.xms.libraries.places.api.model.Place.Builder",
            "org.xms.libraries.places.api.model.AddressComponent.Builder",
            "org.xms.libraries.places.api.model.PhotoMetadata.Builder",
            "org.xms.libraries.places.api.net.FetchPhotoRequest.Builder",
            "org.xms.libraries.places.api.net.FetchPlaceRequest.Builder",
            "org.xms.f.messaging.RemoteMessage",   // Builder
            "org.xms.g.gcm.OneoffTask", // Builder
            "org.xms.g.gcm.PeriodicTask", // Builder
            "org.xms.g.gcm.Task", // Builder
            "org.xms.g.identity.intents.UserAddressRequest.Builder",
            "org.xms.libraries.places.api.model.AutocompletePrediction",
            "org.xms.libraries.places.api.model.PhotoMetadata",
            "org.xms.g.ads.formats.NativeAppInstallAd", "org.xms.g.ads.formats.UnifiedNativeAd",
            "org.xms.g.ads.formats.NativeContentAd", "org.xms.g.ads.formats.NativeAd",
            "org.xms.g.maps.model.StreetViewPanoramaCamera", "org.xms.g.maps.model.StreetViewPanoramaOrientation",
            "org.xms.g.tasks.CancellationToken",
            "org.xms.g.tasks.CancellationTokenSource",
            "org.xms.g.tasks.TaskCompletionSource",
            "org.xms.g.tasks.Task",
            "org.xms.g.auth.api.Auth.AuthCredentialsOptions.Builder",
            "org.xms.g.games.leaderboard.ScoreSubmissionData.Result",
            "org.xms.g.common.api.PendingResult",
            "org.xms.g.common.ErrorDialogFragment",
            "org.xms.g.common.SupportErrorDialogFragment");

    private static final List<String> NOT_FOR_USER_INHERITING =
            Arrays.asList("org.xms.g.common.data.DataBuffer",
                    "org.xms.g.games.achievement.Achievement",
                    "org.xms.g.games.org.xms.g.games.org.xms.g.games.Games",
                    "org.xms.g.games.snapshot.SnapshotMetadata",
                    "org.xms.g.games.stats.PlayerStats",
                    "org.xms.g.games.snapshot.SnapshotMetadataChange",
                    "org.xms.g.games.snapshot.SnapshotContents",
                    "org.xms.g.games.event.Event",
                    "org.xms.g.games.Game",
                    "org.xms.g.games.leaderboard.Leaderboard",
                    "org.xms.g.games.leaderboard.LeaderboardScore",
                    "org.xms.g.games.leaderboard.LeaderboardVariant",
                    "org.xms.g.games.Player",
                    "org.xms.g.common.api.PendingResult");

    /**
     * Whether to generate constructors for a given class.
     *
     * @param className the given class name
     * @return whether to generate constructors
     */
    public static boolean isOnlyForWrapping(String className) {
        return ONLY_FOR_WRAPPING.contains(className);
    }

    public static boolean isNotForUserInheriting(String className) {
        return NOT_FOR_USER_INHERITING.contains(className);
    }
}
