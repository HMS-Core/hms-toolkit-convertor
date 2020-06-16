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

package com.huawei.hms.convertor.idea.util;

import io.netty.channel.ConnectTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Proxy service reflection call encapsulation
 *
 * @since 2020-01-15
 */
public final class PlatformReflectInvoker {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformReflectInvoker.class);

    /**
     * Call platform static method
     *
     * @param className The class name to be proxied
     * @param methodName The method name to be proxied
     * @return InvokeResult
     */
    static InvokeResult invokeMethod(String className, String methodName) {
        try {
            Class targetClass = Class.forName(className);
            Method targetMethod = targetClass.getDeclaredMethod(methodName);
            return new InvokeResult(false, targetMethod.invoke(targetClass.newInstance()));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            LOGGER.info("No sdk found");
            return new InvokeResult(true);
        } catch (Exception e) {
            LOGGER.error("Cannot access {}.{}", className, methodName, e);
            return new InvokeResult(false);
        }
    }

    /**
     * Call platform method
     *
     * @param className The class name to be proxied
     * @param methodName The method name to be proxied
     * @param parameterTypes List of delegated method parameter names
     * @param parameters List of parameters to be passed in reflection call
     * @return InvokeResult
     */
    static InvokeResult invokeMethod(String className, String methodName, Class<?>[] parameterTypes,
        List<Object> parameters) {
        try {
            Class targetClass = Class.forName(className);
            Method targetMethod = targetClass.getDeclaredMethod(methodName, parameterTypes);
            return new InvokeResult(false, targetMethod.invoke(targetClass.newInstance(), parameters));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            LOGGER.info("No sdk found");
            return new InvokeResult(true);
        } catch (Exception e) {
            LOGGER.error("Cannot access {}.{}", className, methodName, e);
            if (e instanceof ConnectTimeoutException) {
                throw new NetworkTimeoutException("Connection timeout", e);
            }
            return new InvokeResult(false);
        }
    }

    /**
     * Call platform static method
     *
     * @param className The class name to be proxied
     * @param methodName The method name to be proxied
     * @return InvokeResult
     */
    public static InvokeResult invokeStaticMethod(String className, String methodName) {
        try {
            Class targetClass = Class.forName(className);
            Method targetMethod = targetClass.getDeclaredMethod(methodName);
            return new InvokeResult(false, targetMethod.invoke(null));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            LOGGER.info("No sdk found");
            return new InvokeResult(true);
        } catch (Exception e) {
            LOGGER.error("Cannot access {}.{}", className, methodName, e);
            return new InvokeResult(false);
        }
    }

    public static class NetworkTimeoutException extends RuntimeException {
        public NetworkTimeoutException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }

    /**
     * Reflection call result encapsulation
     */
    public static class InvokeResult {
        private boolean sdkLost;

        private Object returnValue;

        /**
         * Construction method
         *
         * @param sdkLost set to{@code true}if no SDK found，else{@code false}
         */
        InvokeResult(boolean sdkLost) {
            this.sdkLost = sdkLost;
        }

        /**
         * Construction method
         *
         * @param sdkLost set to{@code true}if no SDK found，else{@code false}
         * @param returnValue Reflection call result
         */
        InvokeResult(boolean sdkLost, Object returnValue) {
            this.sdkLost = sdkLost;
            this.returnValue = returnValue;
        }

        public boolean isSdkLost() {
            return sdkLost;
        }

        public Object getReturnValue() {
            return returnValue;
        }
    }
}
