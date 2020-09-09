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

package com.huawei.hms.convertor.handler;

import com.huawei.hms.convertor.g2h.map.auto.Auto;
import com.huawei.hms.convertor.g2h.map.manual.Manual;
import com.huawei.hms.convertor.json.JClass;

/**
 * abstract processor
 *
 * @since 2020-07-05
 */
public abstract class Handler<T extends Object> implements HandlerRequest<T> {
    private Handler<T> nextHandler;

    private Auto auto;

    private Manual manual;

    private JClass jClass;

    private String version;

    public Auto getAuto() {
        return auto;
    }

    public Manual getManual() {
        return manual;
    }

    public JClass getJClass() {
        return jClass;
    }

    public String getVersion() {
        return version;
    }

    public Handler(Auto auto, Manual manual, JClass jClass, String version) {
        this.auto = auto;
        this.manual = manual;
        this.jClass = jClass;
        this.version = version;
    }

    /**
     * get next processor
     *
     * @return next processor object
     */
    public Handler<T> getNextHandler() {
        return nextHandler;
    }

    /**
     * set next processor
     *
     * @param nextHandler next processor
     */
    public void setNextHandler(Handler<T> nextHandler) {
        this.nextHandler = nextHandler;
    }
}
