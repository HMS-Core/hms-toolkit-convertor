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

package com.huawei.inquiry;

/**
 * SignatureStruct is used for multi signatures of gms or hms matching one xms's.
 *
 * @since 2020-08-25
 */
public class SignatureStruct {
    private String signatureName;

    private int signatureIndex;

    public SignatureStruct(String signatureName, int signatureIndex) {
        this.signatureName = signatureName;
        this.signatureIndex = signatureIndex;
    }

    public String getSignatureName() {
        return signatureName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + signatureIndex;
        result = prime * result + ((signatureName == null) ? 0 : signatureName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!(obj instanceof SignatureStruct)) {
            return false;
        }
        SignatureStruct other = (SignatureStruct)obj;
        if (signatureIndex != other.signatureIndex) {
            return false;
        }
        if (signatureName == null) {
            if (other.signatureName != null) {
                return false;
            }
        } else if (!signatureName.equals(other.signatureName)) {
            return false;
        } else {
            return true;
        }
        return true;
    }
}