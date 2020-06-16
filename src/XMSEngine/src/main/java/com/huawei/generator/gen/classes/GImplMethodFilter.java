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

package com.huawei.generator.gen.classes;

import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.utils.XMSUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Filters methods in GImpl that should not be generated.
 *
 * @since 2020-01-03
 */
public class GImplMethodFilter {
    /**
     * A list containing methods that should not be generated/overrided in GImpl.
     */
    private static final List<Method> UNIMPLEMENTABLE_METHODS =
        Arrays.asList(new Method("com.google.android.gms.common.api.ResultCallbacks", "onResult"),
            new Method("com.google.android.gms.common.api.ResolvingResultCallbacks", "onResult"),
            new Method("com.google.android.gms.location.DetectedActivity", "describeContents"),
            new Method("com.google.android.gms.common.api.ResolvingResultCallbacks", "onFailure"),
            new Method("com.google.android.gms.location.ActivityRecognitionClient", "getApiKey"),
            new Method("com.google.android.gms.maps.model.StreetViewPanoramaLocation", "describeContents"),
            new Method("com.google.android.gms.maps.model.PatternItem", "describeContents"),
            new Method("com.google.android.gms.MapView", "onEnterAmbient"),
            new Method("com.google.android.gms.MapView", "onSaveInstanceState"),
            new Method("com.google.android.gms.StreetViewPanoramaView", "onCreate"),
            new Method("com.google.android.gms.StreetViewPanoramaView", "onSaveInstanceState"),
            new Method("com.google.android.gms.awareness.fence.FenceState", "describeContents"),
            new Method("com.google.android.gms.identity.intents.model.CountrySpecification",
                "describeContents"),
            new Method("com.google.android.gms.nearby.messages.Message", "describeContents"),
            new Method("com.google.android.gms.auth.AccountChangeEvent", "describeContents"),
            new Method("com.google.android.gms.auth.AccountChangeEventsRequest", "describeContents"),
            new Method("com.google.android.gms.auth.AccountChangeEventsResponse", "describeContents"),
            new Method("com.google.android.gms.fido.u2f.api.common.RegisterRequest", "describeContents"),
            new Method("com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity",
                "describeContents"),
            new Method("com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialDescriptor",
                "describeContents"),
            new Method("com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialType",
                "describeContents"),
            new Method("com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity",
                "describeContents"),
            new Method("com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters",
                "describeContents"));

    public static boolean isUnimplementable(JClass def, JMapping<JMethod> mapping) {
        if (mapping.g() == null) {
            return false;
        }
        String currentClass = XMSUtils.degenerify(def.gName());
        return UNIMPLEMENTABLE_METHODS.stream().anyMatch(it -> it.isMatch(currentClass, mapping.g().name()));
    }
}
