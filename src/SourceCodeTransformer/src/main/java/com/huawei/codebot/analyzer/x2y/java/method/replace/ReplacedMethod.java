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

package com.huawei.codebot.analyzer.x2y.java.method.replace;

import com.huawei.codebot.analyzer.x2y.java.method.MethodChangePattern;

import java.util.List;

/**
 * an entity used to hold information that replaced method needs
 *
 * @since 2020-04-14
 */
public class ReplacedMethod extends MethodChangePattern {
    /**
     * qualified name of old method, e.g. com.google.android.gms.auth.api.signin.GoogleSignIn.getClient
     */
    private String oldMethodName;

    /**
     * qualified name of new method, e.g. com.huawei.hms.support.hwid.HuaweiIdAuthManager.getService
     */
    private String newMethodName;

    /**
     * parameter list of new method
     */
    private List<NewParam> newParams;

    @Override
    public String getOldMethodName() {
        return oldMethodName;
    }

    @Override
    public String getNewMethodName() {
        return newMethodName;
    }

    /**
     * @return qualified name of class to which the old method belongs
     */
    public String findClassFromOldMethod() {
        return findClassFromMethod(getOldMethodName());
    }

    /**
     * @return qualified name of class to which the new method belongs
     */
    public String findClassFromNewMethod() {
        return findClassFromMethod(getNewMethodName());
    }

    /**
     * get qualified name of class according to qualified name of method
     *
     * @param methodFullName qualified name of method
     * @return qualified name of class to which the method belongs
     */
    private String findClassFromMethod(String methodFullName) {
        String[] splitName = methodFullName.split("\\.");
        StringBuilder sb = new StringBuilder(splitName[0]);
        for (int i = 1; i < splitName.length - 1; i++) {
            sb.append(".").append(splitName[i]);
        }
        return sb.toString();
    }

    public void setOldMethodName(String oldMethodName) {
        this.oldMethodName = oldMethodName;
    }

    public void setNewMethodName(String newMethodName) {
        this.newMethodName = newMethodName;
    }

    public List<NewParam> getNewParams() {
        return newParams;
    }

    public void setNewParams(List<NewParam> newParams) {
        this.newParams = newParams;
    }
}
