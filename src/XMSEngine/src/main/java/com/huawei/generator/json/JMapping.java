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

package com.huawei.generator.json;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;
import java.util.Objects;

/**
 * Model of G/H mapping for json deserialization.
 *
 * @since 2019-11-12
 */
public class JMapping<T> {
    /**
     * Tag when gms type is matching hms type.
     */
    public static final String STATUS_MATCHING = "matching";

    /**
     * Tag when gms type is exist, but hms type need to be completed manually.
     */
    public static final String STATUS_MANUALLY_ADAPT = "manuallyAdapt";

    /**
     * Tag when gms type is empty, but hms type is exist.
     */
    public static final String STATUS_REDUNDANT = "redundant";

    /**
     * Dummy, treated as manuallyAdapt
     */
    public static final String STATUS_DUMMY = "dummy";

    /**
     * Not supported
     */
    public static final String STATUS_UNSUPPORTED = "notSupport";

    /**
     * Also treated as not supported
     */
    public static final String STATUS_DEVELOPER_MANUAL = "developerManual";

    @SerializedName("h")
    private T h;

    @SerializedName("g")
    private T g;

    @SerializedName("correspondingStatus")
    private String status;

    @SerializedName("tipsInfo")
    private String text = "Documents to be completed";

    @SerializedName("helpUrl")
    private String url = "";

    @SerializedName("hmsVersion")
    private String hmsVersion = "";

    /**
     * A creator method
     * 
     * @param g G
     * @param h H
     * @param status Status
     * @param <T> Type parameter
     * @return The created JMapping
     */
    public static <T> JMapping<T> create(T g, T h, String status) {
        JMapping<T> jMapping = new JMapping<>();
        jMapping.setG(g);
        jMapping.setH(h);
        jMapping.setStatus(status);
        return jMapping;
    }

    public T h() {
        return h;
    }

    public T g() {
        return g;
    }

    public String status() {
        return status;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }

    public boolean isMatching() {
        return lowerCaseEqual(JMapping.STATUS_MATCHING, status);
    }

    /**
     * Although not exactly matched, methods also need to be created
     *
     * @return boolean
     */
    public boolean isDisMatchMethodNeedToBeCreated() {
        return (isManuallyAdapt() || isDummy());
    }

    public boolean isManuallyAdapt() {
        return lowerCaseEqual(JMapping.STATUS_MANUALLY_ADAPT, status);
    }

    public boolean isRedundant() {
        return lowerCaseEqual(JMapping.STATUS_REDUNDANT, status);
    }

    /**
     * Whether matching status is "notSupport".
     *
     * @return whether matching status is unsupported.
     */
    public boolean isUnsupported() {
        return lowerCaseEqual(JMapping.STATUS_UNSUPPORTED, status)
            || lowerCaseEqual(JMapping.STATUS_DEVELOPER_MANUAL, status);
    }

    private boolean isDummy() {
        return STATUS_DUMMY.equals(status());
    }

    private boolean lowerCaseEqual(String str1, String str2) {
        return str1.toLowerCase(Locale.forLanguageTag(str1)).equals(str2.toLowerCase(Locale.forLanguageTag(str2)));
    }

    public void setH(T h) {
        this.h = h;
    }

    public void setG(T g) {
        this.g = g;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHmsVersion() {
        return hmsVersion;
    }

    public void setHmsVersion(String hmsVersion) {
        this.hmsVersion = hmsVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JMapping)) {
            return false;
        }
        JMapping jMapping = (JMapping) obj;
        if (!Objects.equals(h, jMapping.h)) {
            return false;
        }
        if (!Objects.equals(g, jMapping.g)) {
            return false;
        }
        if (status == null) {
            throw new IllegalArgumentException();
        }
        if (!status.equals(jMapping.status)) {
            return false;
        }
        if (!text.equals(jMapping.text)) {
            return false;
        }
        if (!url.equals(jMapping.url)) {
            return false;
        }
        return hmsVersion.equals(jMapping.hmsVersion);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 31 + (h == null ? 0 : h.hashCode());
        result = result * 31 + (g == null ? 0 : g.hashCode());
        result = result * 31 + (status == null ? 0 : status.hashCode());
        result = result * 31 + (text == null ? 0 : text.hashCode());
        result = result * 31 + (url == null ? 0 : url.hashCode());
        result = result * 31 + (hmsVersion == null ? 0 : hmsVersion.hashCode());
        return result;
    }
}
