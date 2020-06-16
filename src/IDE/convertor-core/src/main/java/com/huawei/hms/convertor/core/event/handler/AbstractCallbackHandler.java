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

package com.huawei.hms.convertor.core.event.handler;

import com.huawei.hms.convertor.core.event.context.Event;

/**
 * Abstract callback handler
 *
 * @param <D> Event data type
 * @param <M> Callback message type
 * @since 2020-02-29
 */
public abstract class AbstractCallbackHandler<D, M> implements CallbackExecuteHandler<D, M> {
    /**
     * Get callback message
     *
     * @return callback message
     */
    protected abstract M getCallbackMessage();

    /**
     * Run callback
     *
     * @param event Event
     */
    @Override
    public void executeCallback(Event<D, M> event) {
        M message = getCallbackMessage();
        if (message != null) {
            event.getCallback().accept(message);
        }
    }
}
