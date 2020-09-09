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

package com.huawei.inquiry.docs;

import java.util.Set;

/**
 * model class used to IDE.
 *
 * @since 2020-07-28
 */
public class EntireDoc {
    private Set<XDocs> xDocs; // XClassDoc, XMethodDoc, XFieldDoc

    private Set<ZDocs> gDocs; // ZClassDoc, ZMethodDoc, XFieldDoc

    private Set<ZDocs> hDocs; // ZClassDoc, ZMethodDoc, ZFieldDoc

    private STRATEGYTYPE strategyType; // requestSignature's type, namely x or g, or h or normal class

    private EXCEPTIONTYPE exceptiontype; // exception type when not found corresponding api

    public void setStrategyType(STRATEGYTYPE strategyType) {
        this.strategyType = strategyType;
    }

    public void setGDocs(Set<ZDocs> gDocs) {
        this.gDocs = gDocs;
    }

    public void setHDocs(Set<ZDocs> hDocs) {
        this.hDocs = hDocs;
    }

    public void setXDocs(Set<XDocs> xDocs) {
        this.xDocs = xDocs;
    }

    public Set<ZDocs> getGDocs() {
        return gDocs;
    }

    public Set<ZDocs> getHDocs() {
        return hDocs;
    }

    public Set<XDocs> getXDocs() {
        return xDocs;
    }

    public STRATEGYTYPE getStrategyType() {
        return strategyType;
    }

    public void setExceptiontype(EXCEPTIONTYPE exceptiontype) {
        this.exceptiontype = exceptiontype;
    }

    public EXCEPTIONTYPE getExceptiontype() {
        return exceptiontype;
    }

    public enum STRATEGYTYPE {
        G("g"),
        H("h"),
        X("x"),
        OTHER("other");

        private final String value;

        STRATEGYTYPE(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum SCOPETYPE {
        CLASS("CLASS"),
        METHOD("METHOD"),
        FIELD("FIELD");

        private String scopeType;

        SCOPETYPE(String type) {
            this.scopeType = type;
        }
    }

    public enum EXCEPTIONTYPE {
        NOTFOUND("not_found"),
        HMSNOTSUPPORT("hms_not_support"),
        GMSNOTSUPPORT("gms_not_support"),
        KITNOTSUPPORT("kit_not_support"),
        HMSNOTMATCHGMS("hms_not_match_gms");

        private String exceptionType;

        EXCEPTIONTYPE(String type) {
            this.exceptionType = type;
        }
    }
}
