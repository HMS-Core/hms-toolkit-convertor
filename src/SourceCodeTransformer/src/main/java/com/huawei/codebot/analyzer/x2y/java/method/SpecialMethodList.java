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

package com.huawei.codebot.analyzer.x2y.java.method;

import java.util.HashSet;
import java.util.Set;

class SpecialMethodList {
    static Set<String> methodList = new HashSet<>();

    static {
        methodList.add("com.google.android.gms.ads.reward.RewardedVideoAd.loadAd");
        methodList.add("com.google.android.gms.ads.rewarded.RewardedAd.loadAd");
        methodList.add("com.google.android.gms.auth.api.signin.GoogleSignIn.hasPermissions");
        methodList.add("com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder.requestServerAuthCode");
        methodList.add("com.google.android.gms.common.api.GoogleApiClient.Builder.Builder");
        methodList.add("com.google.android.gms.common.api.GoogleApiClient.connect");
        methodList.add("com.google.android.gms.common.GooglePlayServicesUtil.showErrorDialogFragment");
        methodList.add("com.google.android.gms.fido.Fido.getFido2ApiClient");
        methodList.add("com.google.android.gms.games.achievement.Achievement.getDescription");
        methodList.add("com.google.android.gms.games.achievement.Achievement.getFormattedCurrentSteps");
        methodList.add("com.google.android.gms.games.achievement.Achievement.getFormattedTotalSteps");
        methodList.add("com.google.android.gms.games.achievement.Achievement.getName");
        methodList.add("com.google.android.gms.games.achievement.AchievementEntity.getDescription");
        methodList.add("com.google.android.gms.games.achievement.AchievementEntity.getFormattedCurrentSteps");
        methodList.add("com.google.android.gms.games.achievement.AchievementEntity.getFormattedTotalSteps");
        methodList.add("com.google.android.gms.games.achievement.AchievementEntity.getName");
        methodList.add("com.google.android.gms.games.event.Event.getDescription");
        methodList.add("com.google.android.gms.games.event.Event.getFormattedValue");
        methodList.add("com.google.android.gms.games.event.Event.getName");
        methodList.add("com.google.android.gms.games.event.EventEntity.getDescription");
        methodList.add("com.google.android.gms.games.event.EventEntity.getFormattedValue");
        methodList.add("com.google.android.gms.games.event.EventEntity.getName");
        methodList.add("com.google.android.gms.games.Game.getDescription");
        methodList.add("com.google.android.gms.games.Games.getAchievementsClient");
        methodList.add("com.google.android.gms.games.Games.getEventsClient");
        methodList.add("com.google.android.gms.games.Games.getGamesClient");
        methodList.add("com.google.android.gms.games.Games.getGamesMetadataClient");
        methodList.add("com.google.android.gms.games.Games.getLeaderboardsClient");
        methodList.add("com.google.android.gms.games.Games.getPlayersClient");
        methodList.add("com.google.android.gms.games.Games.getPlayerStatsClient");
        methodList.add("com.google.android.gms.games.Games.getSnapshotsClient");
        methodList.add("com.google.android.gms.games.leaderboard.Leaderboard.getDisplayName");
        methodList.add("com.google.android.gms.games.leaderboard.LeaderboardScore.getDisplayRank");
        methodList.add("com.google.android.gms.games.leaderboard.LeaderboardScore.getDisplayScore");
        methodList.add("com.google.android.gms.games.leaderboard.LeaderboardScore.getScoreHolderDisplayName");
        methodList.add("com.google.android.gms.games.LeaderboardsClient.getLeaderboardIntent");
        methodList.add("com.google.android.gms.games.Player.getDisplayName");
        methodList.add("com.google.android.gms.games.PlayerEntity.getDisplayName");
        methodList.add("com.google.android.gms.games.snapshot.SnapshotMetadata.getDescription");
        methodList.add("com.google.android.gms.games.snapshot.SnapshotMetadataEntity.getDescription");
        methodList.add("com.google.android.gms.games.SnapshotsClient.open");
        methodList.add("com.google.android.gms.tasks.Tasks.call");
        methodList.add("com.google.api.services.drive.DriveRequestInitializer.DriveRequestInitializer");
        methodList.add("com.google.firebase.auth.FirebaseAuth.getInstance");
        methodList.add("com.google.firebase.dynamiclinks.DynamicLink.Builder.buildShortDynamicLink");
        methodList.add("com.google.firebase.dynamiclinks.FirebaseDynamicLinks.getInstance");
        methodList.add("com.google.firebase.functions.FirebaseFunctions.getInstance");
        methodList.add("com.google.firebase.iid.FirebaseInstanceId.getToken");
        methodList.add("com.google.firebase.ml.vision.FirebaseVision.getInstance");
        methodList.add("com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance");
        methodList.add(
            "com.google.firebase.remoteconfig.FirebaseRemoteConfigServerException.FirebaseRemoteConfigServerException");
        methodList.add("com.google.android.gms.common.SignInButton.setStyle");
    }
}
